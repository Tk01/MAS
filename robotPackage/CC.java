package robotPackage;

import java.util.ArrayList;

import com.github.rinde.rinsim.core.model.comm.CommUser;
import com.github.rinde.rinsim.core.model.comm.Message;


public class CC {
	
	private PBC pbc;
	
	private ArrayList<JPlan> jplans = new ArrayList<JPlan>();
	
	
	private boolean ongoing = false;
	
	
	public CC(){
		
		
		
	}
	public void startNegotiation(Plan plan){
		ongoing = true;
		pbc.sendStartNegotiationMessage(plan);
		
		
	}
	public void handleMessage(Message message){
		
		MessageContent messageContent = (MessageContent) message.getContents();
		String type = messageContent.getType();
		
		
		if(type.equals("startNegotiation") && jplans.isEmpty() && pbc.currentplan.goals.size()>3 && !ongoing){
			Plan negPlan = ((StartNegotiationMessageContent) messageContent).getPlan();
			JPlan jointPlan = getBestJointPlan(negPlan);
			if(jointPlan!=null){
				ongoing = true;
				jplans.add(jointPlan);
				pbc.sendNegotiationBidMessage(jointPlan, message.getSender());
			}
			
		}
		if(type.equals("negotiationBid")){
			jplans.add(((NegotiationBidMessageContent)messageContent).getJointPlan());
			
			//TODO after timeperiod choose bestJplan
			JPlan bestJplan = setBestJplan();
			
			pbc.sendNegotiationReplyMessage(bestJplan.JPlanAgent);
			ongoing = false;
			
			
		}
		if(type.equals("negotiationReply")){
			if(((NegotiationReplyMessageContent)messageContent).isAccepted()){
				pbc.currentplan = jplans.get(0).getOwnPlan();
			}
			
		}
		
		
	}
	private JPlan setBestJplan() {
		/*
		for(JPlan jplan: jplans){
			if(jplan.get)
		}*/
		return null;
	}
	private JPlan getBestJointPlan(Plan negPlan) {
		
		ArrayList <Goal> negGoals = negPlan.getPlan();
		ArrayList <Goal> ownGoals = pbc.getCurrentPlan().getPlan();
		
		double valueNegPlan = negPlan.value(negPlan.getPlan());
		double valueCurrentPlan = pbc.getCurrentPlan().value(pbc.getCurrentPlan().getPlan());
		
		Plan tempNegPlan = new Plan(negGoals, pbc.worldModel);
		Plan tempOwnPlan = new Plan(ownGoals, pbc.worldModel);
		
		boolean changed = false;
		
		for(int i = 1; i<negGoals.size();i++){
			if(negGoals.get(i).type.equals("pickup") && negGoals.get(i+1).type.equals("drop")){
				for(int j = 1;j<ownGoals.size();j++){
					if(ownGoals.get(j).type.equals("pickup") && ownGoals.get(j+1).type.equals("drop")){
						Plan tempPlan = new Plan(tempOwnPlan.goals,pbc.worldModel);
						tempPlan.remove(ownGoals.get(j));
						tempPlan.remove(ownGoals.get(j+1));
						Plan tryTempOwnPlan = tempPlan.isPossiblePlan(negGoals.get(i),negGoals.get(i+1));
						tempPlan = new Plan(tempNegPlan.goals,tempNegPlan.model);
						tempPlan.remove(ownGoals.get(i));
						tempPlan.remove(ownGoals.get(i+1));
						Plan tryTempNegPlan = tempPlan.isPossiblePlan(negGoals.get(j),negGoals.get(j+1));
						if(tryTempOwnPlan.value(tryTempOwnPlan.goals)>tempOwnPlan.value(tempOwnPlan.goals) && tryTempNegPlan.value(tryTempNegPlan.goals)>tempNegPlan.value(tempNegPlan.goals)){
							tempNegPlan = tryTempNegPlan;
							tempOwnPlan = tryTempOwnPlan;
							changed = true;
						}
					}
					if(ownGoals.get(j).type.equals("pickup") && ownGoals.get(j+2).type.equals("drop")){
						Plan tempPlan = new Plan(tempOwnPlan.goals,pbc.worldModel);
						tempPlan.remove(ownGoals.get(j));
						tempPlan.remove(ownGoals.get(j+2));
						Plan tryTempOwnPlan = tempPlan.isPossiblePlan(negGoals.get(i),negGoals.get(i+1));
						tempPlan = new Plan(tempNegPlan.goals,tempNegPlan.model);
						tempPlan.remove(ownGoals.get(i));
						tempPlan.remove(ownGoals.get(i+1));
						Plan tryTempNegPlan = tempPlan.isPossiblePlan(negGoals.get(j),negGoals.get(j+2));
						if(tryTempOwnPlan.value(tryTempOwnPlan.goals)>tempOwnPlan.value(tempOwnPlan.goals) && tryTempNegPlan.value(tryTempNegPlan.goals)>tempNegPlan.value(tempNegPlan.goals)){
							tempNegPlan = tryTempNegPlan;
							tempOwnPlan = tryTempOwnPlan;
							changed = true;
						}
					}
				}
			}
			if(negGoals.get(i).type.equals("pickup") && negGoals.get(i+2).type.equals("drop")){
				for(int j = 1;j<ownGoals.size();j++){
					if(ownGoals.get(j).type.equals("pickup") && ownGoals.get(j+1).type.equals("drop")){
						Plan tempPlan = new Plan(tempOwnPlan.goals,pbc.worldModel);
						tempPlan.remove(ownGoals.get(j));
						tempPlan.remove(ownGoals.get(j+1));
						Plan tryTempOwnPlan = tempPlan.isPossiblePlan(negGoals.get(i),negGoals.get(i+2));
						tempPlan = new Plan(tempNegPlan.goals,tempNegPlan.model);
						tempPlan.remove(ownGoals.get(i));
						tempPlan.remove(ownGoals.get(i+2));
						Plan tryTempNegPlan = tempPlan.isPossiblePlan(negGoals.get(j),negGoals.get(j+1));
						if(tryTempOwnPlan.value(tryTempOwnPlan.goals)>tempOwnPlan.value(tempOwnPlan.goals) && tryTempNegPlan.value(tryTempNegPlan.goals)>tempNegPlan.value(tempNegPlan.goals)){
							tempNegPlan = tryTempNegPlan;
							tempOwnPlan = tryTempOwnPlan;
							changed = true;
						}
					}
					if(ownGoals.get(j).type.equals("pickup") && ownGoals.get(j+2).type.equals("drop")){
						Plan tempPlan = new Plan(tempOwnPlan.goals,pbc.worldModel);
						tempPlan.remove(ownGoals.get(j));
						tempPlan.remove(ownGoals.get(j+2));
						Plan tryTempOwnPlan = tempPlan.isPossiblePlan(negGoals.get(i),negGoals.get(i+2));
						tempPlan = new Plan(tempNegPlan.goals,tempNegPlan.model);
						tempPlan.remove(ownGoals.get(i));
						tempPlan.remove(ownGoals.get(i+2));
						Plan tryTempNegPlan = tempPlan.isPossiblePlan(negGoals.get(j),negGoals.get(j+2));
						if(tryTempOwnPlan.value(tryTempOwnPlan.goals)>tempOwnPlan.value(tempOwnPlan.goals) && tryTempNegPlan.value(tryTempNegPlan.goals)>tempNegPlan.value(tempNegPlan.goals)){
							tempNegPlan = tryTempNegPlan;
							tempOwnPlan = tryTempOwnPlan;
							changed = true;
						}
					}
				}
			}
			
			
		}
		
		

		if (changed){
			return new JPlan(tempOwnPlan,tempNegPlan);
		}
		else{
			return null;
		}
	}
	

	
	
	public void negotiationAbort(){
		
	}
	
	public void evaluateJPlans(){
		JPlan bestJPlan = jplans.get(0);
		double bestPlanValue = jplans.get(0).getOtherPlan().value(jplans.get(0).getOtherPlan().goals);
		for(int i=1; i<jplans.size();i++){
			double planValue = jplans.get(i).getOtherPlan().value(jplans.get(i).getOtherPlan().goals);
			if(bestPlanValue> planValue){
				bestJPlan = jplans.get(i);
				bestPlanValue = planValue;
			}
		}
		
		pbc.sendConfirmationMessage(bestJPlan);
	}
	
	
	
}
