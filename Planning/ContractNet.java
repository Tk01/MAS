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
    private long defTime;
    private Plan definitivebid;
    private CommUser defSender;
    private WorldModel model;
    private boolean chargeGoal;
    
    private Communication comm;
    
    public ContractNet(final WorldModel model, final Communication comm) {
        this.defTime = 0L;
        this.definitivebid = null;
        this.chargeGoal = false;
        this.model = model;
       
       
        this.comm = comm;
    }
    
    private void reserveMessages(final ArrayList<Message> messages) {
    	for(Message message:messages){
			MessageContent content = (MessageContent) message.getContents();
			if(content.getType() == MessageTypes.ReturnChargestationMessage){
				ReturnChargestationMessageContents chargeContent  = ((ReturnChargestationMessageContents) content);
				
				
				if(this.chargeGoal){
					if( chargeContent.hasSucceeded()){
						// if this message is a response from a message created in placeCharge() set definitive bid as current plan
						((ChargeGoal)this.definitivebid.getPlan().get(0)).setReserved(true);
						
						//////////////////////this.SetNewPlan(definitivebid);
					}
					model.setWindows(chargeContent.getFreeSlots());
					this.chargeGoal=false;
					//////////////////////////////definitivebid= null;
					messages.remove(message);
					return;	
				}
				
				if(chargeContent.isReserved() && chargeContent.hasSucceeded()){
					// set the first ChargeGoal as reserved and retry to place a definitive bid
					for(int i =0; i<definitivebid.getPlan().size();i++){
						Goal goal = definitivebid.getPlan().get(i);
						GoalTypes type = goal.type();
						if(type == GoalTypes.Charging && model.isReserveChargingStation() && !((ChargeGoal)goal).isReserved()){
							((ChargeGoal)goal).setReserved(true);
							Plan plan = definitivebid;
							definitivebid= null;
							doDefBid(plan, defSender,defTime );
							messages.remove(message);
							
							return;
						}
					}
				}
				if(chargeContent.isReserved() && !chargeContent.hasSucceeded()){
					//if the reservation has failed, set the definitve bid as null
					definitivebid= null;
					messages.remove(message);
					
					return;
				}
				model.setWindows(chargeContent.getFreeSlots());
			}
			
		}
    	
    }
    
    public void finishContractNet(){
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
		
		ArrayList<Goal> goals = bidPlan.getPlan();
		
		for(int i =0; i<goals.size();i++){
			Goal goal = goals.get(i);
			GoalTypes type = goal.type();
			if(type == GoalTypes.Charging && model.isReserveChargingStation() && !((ChargeGoal)goal).isReserved()){
				//if Chargoal have to be reserved send a reservation message first
				comm.sendReserveMessage(goal.getStartWindow(), goal.getEndWindow());
				return;
			}
		}
    	
    }
    
    //Done
    public void packageWinLoss(ArrayList<Message> messages) {
    	
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
					}
					
				}
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
    public void packageRequests(ArrayList<Message> messages) {
    	if(model.canBid()){
    	
			Plan bestPlan = null;
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
					Plan bidPlan = null;
					Goal pickupGoal = new Goal(pack.getStart(), GoalTypes.Pickup, pack.getPickupTimeWindow());
					Goal dropGoal = new Goal(pack.getEnd(), GoalTypes.Drop, pack.getDeliveryTimeWindow());
					bidPlan = model.getCurrentPlan().isPossiblePlan(pickupGoal,dropGoal,model.getWindows(),callForBidContent.getEndTime()+1000);
					if(bidPlan !=null && bidPlan.getPlan() !=null){
						
						double bidValue = bestPlan.value(bestPlan.getPlan(),callForBidContent.getEndTime()+1000);
						Bid bid = new Bid(sender, pack, bidValue);
						model.addBid(bid);
						doBid(bidPlan, sender,time, pack);
						
						
						
					}
				}
			}
			
    	}
		
    }
    
  
    
    public void placeCharge() {
        if (!this.model.isReserveChargingStation()) {
            this.model.setCurrentGoal((Goal)new ChargeGoal((Point)this.model.getChargingStation().getPosition().get(), new TimeWindow(0L, Long.MAX_VALUE), false));
            return;
        }
        if (this.definitivebid == null) {
            for (final TimeWindow w : this.windows) {
                final long start = this.model.getTime().getTime() + this.model.calcTime(this.model.coordinates(), (Point)this.model.getChargingStation().getPosition().get()) + 2000L;
                if (w.isIn(start)) {
                    double batterydiff = this.model.getMaxBattery() - 2000L - this.model.battery() - this.model.calcTime(this.model.coordinates(), (Point)this.model.getChargingStation().getPosition().get());
                    if (batterydiff / 5.0 == (long)(batterydiff / 5.0)) {
                        batterydiff = (long)(batterydiff / 5.0);
                    }
                    else {
                        batterydiff = (long)(batterydiff / 5.0) + 1L;
                    }
                    final long end = (long)Math.min(start + batterydiff, w.end);
                    final Goal goal = (Goal)new ChargeGoal((Point)this.model.getChargingStation().getPosition().get(), new TimeWindow(start, end), false);
                    final ArrayList<Goal> list = new ArrayList<Goal>();
                    list.add(goal);
                    this.chargeGoal = true;
                    this.definitivebid = new Plan((ArrayList)list, this.model);
                    this.comm.sendReserveMessage(goal.getStartWindow(), goal.getEndWindow());
                }
            }
        }
    }
    

    
    public void SetNewPlan(final Plan plan) {
    	
    	ChargeGoal lostChargeGoal = currentplan.lostChargeGoal(newPlan.getPlan());
		
		if( bbc.getGoal() != null
				&& bbc.getGoal().type() == GoalTypes.Charging 
				&& !newPlan.getPlan().contains(bbc.getGoal())){
			InformationHandler.getInformationHandler().setlostcharge();
		}
		//correct the error mad in Plan.calculateGoals()
		if(this.currentplan !=null){
			@SuppressWarnings("unchecked")
			ArrayList<Goal> testplan = (ArrayList<Goal>) this.currentplan.getPlan().clone();
			testplan.remove(lostChargeGoal);
			if(testplan.size()>0 && !newPlan.getPlan().contains(testplan.get(0))){
				newPlan.getPlan().add(0,testplan.get(0));
			}
		}

		currentplan=newPlan;
		removeUnattainablePackages(getCurrentPlan());
		bbc.setGoal(currentplan.getNextgoal());
		// cancel reservations in chargingstones that aren't in the new plan anymore
		if(model.isReserveChargingStation() && lostChargeGoal != null) bbc.sendCancelReservationMessage(lostChargeGoal.getStartWindow(),lostChargeGoal.getEndWindow());
	}
    
    public void forcefullSetNewPlan(final ArrayList<Goal> list) {
    	this.currentplan.setPlan(otherPlan);
		removeUnattainablePackages(getCurrentPlan());
		bbc.setGoal(currentplan.getNextgoal());
    }
    




	
}