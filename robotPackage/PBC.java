package robotPackage;

import java.util.ArrayList;
import java.util.List;

import com.github.rinde.rinsim.core.model.comm.CommUser;
import com.github.rinde.rinsim.core.model.comm.Message;

import world.Package;

public class PBC {
	

	
	Plan currentplan;
	Plan definitivebid;
	ArrayList<Plan> prebids;
	WorldModel worldModel;

	BBC bbc;

	public PBC(){
		worldModel = bbc.getWorldModel();
		
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
		
		ArrayList <Message> preAssignMessages = new ArrayList();
		for(int i = 0; i<messages.size();i++){
			Message message = messages.get(i);
			MessageContent content = (MessageContent) message.getContents();
			if(content.getType().equals("PreAssignment")){
				
				preAssignMessages.add(message);
				
			
			}	
		}
		preAssignment(preAssignMessages);
		
	}
	
	//The package has been def assigned to the agent so the definitive bid plan becomes the currentPlan
	private void defAssignment(DefAssignmentMessageContent content){
		if(content.assigned){
			currentplan=definitivebid;
		}
		else{
			definitivebid = currentplan;
		}
		
	}
	
	
	private void preAssignment(ArrayList <Message> preAssignMessages){
		
		for(int i= 0;i<preAssignMessages.size();i++){
			Message message = preAssignMessages.get(i);
			PreAssignmentMessageContent content = (PreAssignmentMessageContent) message.getContents();
			int ID = content.getContractID();
		}
	
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	//A bid will be done when the pack can be fit in the plan and no other task is being bid on which has a better value.
	// when no bid is done, the bid will be -1
	public double doPreBid(Pack pack, CommUser sender){

		
		
		double bid = -1;
		ArrayList<Plan> plans = generatePlans(pack);
		Plan bestPlan=getBestPlan(plans);
		double currentBestValue = knowledgeBase.getProvisionalPlan().getPlanValue();
		double bestPlanValue = bestPlan.getPlanValue();
		if(bestPlan != null&&bestPlanValue > currentBestValue){
			bid = bestPlan.getPlanValue();
		}
		  
		return bid;
		
	}
	
	//Makes a list of plans with the new pack that can be possible to achieve
	private ArrayList<Plan> generatePlans(Package pack){
		ArrayList<Plan> generatedPlans = new ArrayList<Plan>();
		Plan plan = knowledgeBase.getCurrentPlan();
		for(int i=0; i<plan.getPlan().size();i++){
			Plan tempPlan = plan;
			tempPlan.addPackage(pack, i);
			
			if(tempPlan.isPossiblePlan(i)){
				generatedPlans.add(tempPlan);
			}
		}
		return generatedPlans;
		
	}
	
	
	
	private Plan getBestPlan(ArrayList<Plan> plans){
		Plan bestPlan = null;
		double bestValue = -1;
		for(int i=0; i<plans.size();i++){
			double value = plans.get(i).getPlanValue();
			if(value == -1){
				bestValue = value;
				bestPlan = plans.get(i);
			}
			if(value<bestValue){
				bestValue = value;
				bestPlan = plans.get(i);
			}
		}
		return bestPlan;
	}
	public void plan() {
		// TODO Auto-generated method stub
		
	}

	public void plan(Charging charging) {
		// TODO Auto-generated method stub
		
	}
	
	public double checkIfBetterBid(Pack pack, double currentBestBid){
		double bid = -1;
		
		ArrayList<Plan> plans = generatePlans(pack);
		Plan bestPlan=getBestPlan(plans);
		double currentBestValue = knowledgeBase.getProvisionalPlan().getPlanValue();
		double bestPlanValue = bestPlan.getPlanValue();
		if(bestPlan != null&&bestPlanValue > currentBestValue){
			bid = bestPlan.getPlanValue();
		}
		
		if(bid == -1){
			
		}
		
		/* Add check here for negotation during the DynCNET as here the communication with the CC should start*/
		
		if(bid>-1 && currentBestBid < bid ){
			bid = -1;
			
		}
		
		return bid;
	}
	
}
