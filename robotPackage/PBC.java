package robotPackage;

import java.util.ArrayList;
import java.util.List;

import org.jscience.geography.coordinates.Coordinates;

import com.github.rinde.rinsim.core.model.comm.CommUser;
import com.github.rinde.rinsim.core.model.comm.Message;

import world.ChargingStation;
import world.Package;

public class PBC {
	

	
	Plan currentplan;
	Plan definitivebid;
	ArrayList<Plan> prebids;
	WorldModel worldModel;

	BBC bbc;
	
	CC cc;
	
	boolean ccOnHold = false;

	public PBC(){
		worldModel = bbc.getWorldModel();
		cc = new CC();
		
	}
	
	boolean chargingInPlan = true;
	

	public void done(Goal g){
		getCurrentPlan().remove(g);
		
		bbc.setGoal(getCurrentPlan().getNextgoal());
		
	}
	
	public Plan getCurrentPlan(){
		return currentplan;
	}
	
	public Plan getdefinitivebid(){
		return definitivebid;
	}

	
	
	
	
	public void readMessages(){
		
		ArrayList<Message> messages = worldModel.messages();
		
		for(int i = 0; i<messages.size();i++){
			Message message = messages.get(i);
			MessageContent content = (MessageContent) message.getContents();
			if(content.getType().equals("DefAssignment")){
				
				defAssignment((DefAssignmentMessageContent)content);


			}
		}
		
		
		preAssignment(messages);
		
		callForBids(messages);
		
	}
	
	//The package has been def assigned to the agent so the definitive bid plan becomes the currentPlan
	private void defAssignment(DefAssignmentMessageContent content){
		if(content.assigned){
			currentplan=definitivebid;
			cc.abort();
			
		}
		else{
			ArrayList <Goal> goals = definitivebid.getPlan();
			for(int i=0; i<goals.size();i++){
				if(goals.get(i).type.equals("charging")){
					bbc.deleteChargeReservation(goals.get(i).startWindow, goals.get(i).endWindow);
				}
				
			}
			definitivebid = currentplan;
			ccOnHold = false;
			
		}
		
	}
	
	
	private void preAssignment(ArrayList <Message> messages){
		ArrayList deleteMessages = new ArrayList();
		Plan bestPlan = null;
		double bestPlanValue=-1;
		CommUser sender=null;
		for(int i= 0;i<messages.size();i++){
			Message message = messages.get(i);
			MessageContent content = (MessageContent) message.getContents();
			if(content.getType().equals("PreAssignment")){
				
				PreAssignmentMessageContent preAssignContent = (PreAssignmentMessageContent) content;
				int ID = preAssignContent.getContractID();
				for(int j = 0; j<prebids.size();j++ ){
					Plan plan = prebids.get(j);
					if(plan.getId()==ID){
						long timeLastAction =plan.getBidPackage().getTimeLastAction();
						long delay  = plan.getBidPackage().getDelay();
						long lastTime = timeLastAction+delay;
						long currentTime = worldModel.getTime().getTime();
						if(currentTime>lastTime){
							deleteMessages.add(i);
						}
						else{
							double planValue = plan.getPlanValue();
							if(planValue<bestPlanValue && bestPlanValue>-1){
								bestPlanValue = planValue;
								bestPlan = plan;
								sender = message.getSender();
								deleteMessages.add(i);
								
							}
							else if(bestPlanValue==-1){
								bestPlanValue = planValue;
								bestPlan = plan;
								deleteMessages.add(i);
							}
							
						}
					}
				}
				
			}
			
		}
		for(int i=0;i<deleteMessages.size();i++){
			messages.remove(deleteMessages.get(i));
		}
		
		if(bestPlan != null){
			doDefBid(bestPlan, sender);
		}
	
	}
	
	
	private void doDefBid(Plan plan, CommUser sender){
		ArrayList<Goal> goals = plan.getPlan();
		for(int i =0; i<goals.size();i++){
			Goal goal = goals.get(i);
			String type = goal.type();
			if(type.equals("charging") && worldModel.isReserveChargingStation()){
				Plan finalPlan = reserveSlotCharging(plan);
			}
		}
		
		double bid = plan.getBid();
		
		bbc.sendDefBidMessage(sender, bid, plan.getId());
		
	}
	
	private Plan reserveSlotCharging(Plan plan){
		Plan finalPlan= plan;
		
		return finalPlan;
	}
	
	
	
	private void callForBids(ArrayList<Message> messages){
		
		for(int i= 0;i<messages.size();i++){
			Message message = messages.get(i);
			MessageContent content = (MessageContent) message.getContents();
			if(content.getType().equals("DeliverMessage")){
				
				DeliverPackageMessageContent callForBidContent = (DeliverPackageMessageContent) content;
				int ID = callForBidContent.getContractID();
				Package pack = callForBidContent.getPackageToDel();
				
				
				Plan plan  = new Plan(definitivebid.getPlan());
				
				Plan bidPlan = null;
				
				if(worldModel.isReserveChargingStation()){
					bidPlan = plan.isPossiblePlan(pack);
				}
				else{
					bidPlan = plan.isPossiblePlan(pack);
				}
				
				double bid = bidPlan.getBid();
				bidPlan.setBidPackage(pack);
				
				bbc.sendPreBidMessage(message.getSender(), bid, ID);
				
			}
			
		}
		
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
