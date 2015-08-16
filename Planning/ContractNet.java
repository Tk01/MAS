package Planning;

import com.github.rinde.rinsim.util.*;

import worldInterface.*;

import com.github.rinde.rinsim.core.model.comm.*;

import Messages.*;
import WorldModel.*;
import world.*;
import world.Package;

import com.github.rinde.rinsim.geom.*;

import java.util.*;

public class ContractNet
{

    private Bid winningBid;

    private WorldModel model;
    
    private Communication comm;
    
    public ContractNet(final WorldModel model, final Communication comm) {

        this.winningBid = null;
        this.model = model;
       
       
        this.comm = comm;
    }
    
    public boolean reserveMessages() {
    	ArrayList<Message> messages = model.messages();
    	for(Message message:messages){
			MessageContent content = (MessageContent) message.getContents();
			if(content.getType() == MessageTypes.ReturnChargestationMessage){
				ReturnChargestationMessageContents chargeContent  = ((ReturnChargestationMessageContents) content);
				
				
				if(chargeContent.hasSucceeded()){
					
					chargeReservationSuccess();
					
					model.setWindows(chargeContent.getFreeSlots());
					model.setChargingGoal(true);
					
					messages.remove(message);
					
					return true;	
				}
								
				model.setWindows(chargeContent.getFreeSlots());
			}
			
		}
		return false;
    	
    }
    
    
    private void chargeReservationSuccess(){
    	
    	Package pack = winningBid.getPackageToDeliver();
		
		Goal pickupGoal = new Goal(pack.getStart(), GoalTypes.Pickup, pack.getPickupTimeWindow());
		Goal dropGoal = new Goal(pack.getEnd(), GoalTypes.Drop, pack.getDeliveryTimeWindow());
		Long time = model.getTime().getTime();
		Plan bidPlan = model.getCurrentPlan().isPossiblePlan(pickupGoal,dropGoal,model.getWindows(),time);
		
		model.setCurrentPlan(bidPlan);
		
		
		comm.sendPackageReplyMessage(winningBid.getBidSender(), true);
		
		ArrayList<Bid> wins = model.getWins();
		
		for (Bid bid : wins) {
			comm.sendPackageReplyMessage(bid.getBidSender(), false);
			
		}
		wins.clear();
		winningBid=null;
    	
		
    	
    }
    
    public void finishContractNet(){
    	//winningbid not null means a winningbid has been found but reservation of charge station is done and waiting answer
    	if(winningBid == null){
	    	ArrayList<Bid> wins = model.getWins();
	    	Bid bestBid = null;
	    	
	    	
			for (Bid bid : wins) {
				if(bestBid== null){
					bestBid = bid;
				}
				else if(bid.getValue()<bestBid.getValue()){
					bestBid = bid;
				}
				
			}
			
			
			
			Package pack = bestBid.getPackageToDeliver();
			
			Goal pickupGoal = new Goal(pack.getStart(), GoalTypes.Pickup, pack.getPickupTimeWindow());
			Goal dropGoal = new Goal(pack.getEnd(), GoalTypes.Drop, pack.getDeliveryTimeWindow());
			Long time = model.getTime().getTime();
			Plan bidPlan = model.getCurrentPlan().isPossiblePlan(pickupGoal,dropGoal,model.getWindows(),time);
			
			wins.remove(bestBid);
			
			winningBid = bestBid;
			
			
			
			if(model.isChargeReservationNeeded( )){
				chargeReservation(bestBid, bidPlan);
				return;
			}
			else{
				comm.sendPackageReplyMessage(bestBid.getBidSender(), true);
				for (Bid bid : wins) {
					comm.sendPackageReplyMessage(bid.getBidSender(), false);
					wins.remove(bid);
				}
				
			}
			
    	}
    	else{
    		
    		
    		
    	}
    	
    }
    
    private void chargeReservation(Bid bid, Plan plan){
    	
    	ArrayList<Goal> currentGoals = model.getCurrentPlan().getPlan();
		long startCurrentCharge = -1;
		long endCurrentCharge = -1;
		
		for(int i =0; i<currentGoals.size();i++){
			Goal goal = currentGoals.get(i);
			GoalTypes type = goal.type();
			if(type == GoalTypes.Charging){
				startCurrentCharge = ((ChargeGoal)goal).getStartWindow();
				endCurrentCharge = ((ChargeGoal)goal).getEndWindow();
				
			}
		}
    
    	ArrayList<Bid> wins = model.getWins();
    
    	ArrayList<Goal> bidGoals = plan.getPlan();
		
		
		for(int i =0; i<bidGoals.size();i++){
			Goal goal = bidGoals.get(i);
			GoalTypes type = goal.type();
			if(type == GoalTypes.Charging){
				
				//if Chargoal have to be reserved send a reservation message first
				if(startCurrentCharge == -1){
					comm.sendReserveMessage(goal.getStartWindow(), goal.getEndWindow());
				}
				//If there is an existing chargeGoal this needs to be cancelled
				else{
					comm.sendReserveMessage(goal.getStartWindow(), goal.getEndWindow());
				}
				return;
			}
		}
		
		if(!model.isChargingGoal() && startCurrentCharge==-1){
			comm.sendPackageReplyMessage(bestBid.getBidSender(), true);
			for (Bid bid : wins) {
				comm.sendPackageReplyMessage(bid.getBidSender(), false);
				wins.remove(bid);
			}
		}
    }
    
    //Done
    public void packageWinLoss() {
    	ArrayList<Message> messages = model.messages();
    	for(int i= 0;i<messages.size();i++){
			Message message = messages.get(i);
			MessageContent content = (MessageContent) message.getContents();
			if(content.getType() == MessageTypes.DefAssignmentMessage){
				DefAssignmentMessageContent deliverPackageContent = (DefAssignmentMessageContent) content;
				ArrayList<Bid> bids = model.getBids();
				for (Bid bid : bids) {
					if(bid.getBidSender() == deliverPackageContent.getUser()){
						if(deliverPackageContent.assigned){
							model.addWin(bid);
							bids.remove(bid);
							
						}
						else{
							bids.remove(bid);
						}
					}
					
				}
			}
    	}
    	
    	removeWinLossMessages();
    	
    	
    }
    
    private void removeWinLossMessages(){
    	ArrayList<Message> messages = model.messages();
    	for(int i= 0;i<messages.size();i++){
			Message message = messages.get(i);
			MessageContent content = (MessageContent) message.getContents();
			if(content.getType() == MessageTypes.DefAssignmentMessage){
				messages.remove(message);
			}
    	}
    }
    
    //Done
    private void doBid(final Plan plan, final CommUser sender, final long bidreturntime, Package pack) {
    	
    	
    
		
		
		Plan finalPlan = plan;
		

		double oldValue = model.getCurrentPlan().value(model.getCurrentPlan().getPlan(), bidreturntime);
		double newValue = finalPlan.value(finalPlan.getPlan(), bidreturntime);
		double bidValue = newValue - oldValue;

		comm.sendDefBidMessage(sender, bidValue);
    }
    
    //Done
    public void packageRequests() {
    	ArrayList<Message> messages = model.messages();
    	if(model.canBid()){
    	
			
			CommUser sender = null;
			long time =-1;
			Package pack = null;
			
			//look up for the best possible goals to be added to the plan
			for(int i= 0;i<messages.size();i++){
				Message message = messages.get(i);
				MessageContent content = (MessageContent) message.getContents();
				if(content.getType() == MessageTypes.DeliverMessage){
					DeliverPackageMessageContent callForBidContent = (DeliverPackageMessageContent) content;
					pack = callForBidContent.getPackageToDel();
					
					Goal pickupGoal = new Goal(pack.getStart(), GoalTypes.Pickup, pack.getPickupTimeWindow());
					Goal dropGoal = new Goal(pack.getEnd(), GoalTypes.Drop, pack.getDeliveryTimeWindow());
					Plan bidPlan = model.getCurrentPlan().isPossiblePlan(pickupGoal,dropGoal,model.getWindows(),callForBidContent.getEndTime()+1000);
					if(bidPlan !=null && bidPlan.getPlan() !=null){
						
						double bidValue = bidPlan.value(bidPlan.getPlan(),callForBidContent.getEndTime()+1000);
						Bid bid = new Bid(sender, pack, bidValue);
						model.addBid(bid);
						doBid(bidPlan, sender,time, pack);
						
						
						
					}
				}
			}
			
    	}
		
    }
    
  
    
   
    
   

	public void finishCNetAfterNegotiation(Bid negotiationBid) {
		ArrayList<Bid> wins = model.getWins();
		
		if(negotiationBid == winningBid){
			if(model.isChargeReservationNeeded()){
				Package pack = winningBid.getPackageToDeliver();
				Plan Plan = null;
				Goal pickupGoal = new Goal(pack.getStart(), GoalTypes.Pickup, pack.getPickupTimeWindow());
				Goal dropGoal = new Goal(pack.getEnd(), GoalTypes.Drop, pack.getDeliveryTimeWindow());
				Plan = model.getCurrentPlan().isPossiblePlan(pickupGoal,dropGoal,model.getWindows(),0);
				chargeReservation(winningBid, Plan );
			}
			else{
				comm.sendPackageReplyMessage(winningBid.getBidSender(), true);
				
				wins = model.getWins();
				
				for (Bid bid : wins) {
					comm.sendPackageReplyMessage(bid.getBidSender(), false);
					wins.remove(bid);
				}
			}
			
			
		}
		else{
			comm.sendPackageReplyMessage(negotiationBid.getBidSender(), true);
			wins.remove(negotiationBid);
			
			comm.sendPackageReplyMessage(winningBid.getBidSender(), false);
			
			for (Bid bid : wins) {
				comm.sendPackageReplyMessage(bid.getBidSender(), false);
				wins.remove(bid);
			}
			
			
			
		}
		winningBid=null;
		
		
	}
    




	
}