package robotPackage;

import java.util.ArrayList;

import com.github.rinde.rinsim.core.model.comm.CommUser;
import com.github.rinde.rinsim.core.model.comm.Message;


public class CC {
	
	private PBC pbc;
	
	private JPlan jplans;
	
	
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
		
		
		if(type.equals("startNegotiation") && jplans==null && pbc.currentplan.goals.size()>3 && !ongoing){
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

	
	public void bestJPlan(Plan otherPlan){
		Plan ownPlanNoCharging = pbc.currentplan.returnPlanWithoutCharging();
		Plan otherPlanNoCharging = otherPlan.returnPlanWithoutCharging();
		
		ArrayList<Goal> allGoals = ownPlanNoCharging.goals;
		allGoals.addAll(otherPlanNoCharging.goals);
		
		ArrayList<Goal> ownGoals = ownPlanNoCharging.goals;
		ArrayList<Goal> otherGoals = otherPlanNoCharging.goals;
		
		
		
		
		
		JPlan bestJPlan = getBestPlan(allGoals, ownGoals, otherGoals, new ArrayList<Goal>(), new ArrayList<Goal>(), 0, otherPlan.value(otherPlan.goals));
	}
	
	private JPlan getBestPlan(ArrayList<Goal> allGoals,
			ArrayList<Goal> ownGoals, ArrayList<Goal> otherGoals,
			ArrayList<Goal> bestOwn, ArrayList<Goal> bestOther, int i,
			double minOtherValue) {
		
		if(i+1==allGoals.size()){
			Plan bestOwnPlan;
			Plan bestOtherPlan;
			
			if(pbc.currentplan.value(ownGoals)<pbc.currentplan.value(bestOwn) && pbc.currentplan.value(bestOther)<=minOtherValue){
				bestOwnPlan = new Plan(ownGoals, pbc.worldModel);
				bestOtherPlan = new Plan(otherGoals, pbc.worldModel);
				
			}
			else{
				bestOwnPlan = new Plan(bestOwn, pbc.worldModel);
				bestOtherPlan = new Plan(bestOther, pbc.worldModel);
				
			}
			JPlan bestJPlan = new JPlan();
			bestJPlan.ownPlan=bestOwnPlan;
			bestJPlan.otherPlan=bestOtherPlan;
			return bestJPlan;
		}
		else{
			
			JPlan jPlan = new JPlan();
			jPlan.setOwnPlan(new Plan(bestOwn, pbc.worldModel));
			jPlan.setOtherPlan(new Plan(bestOther, pbc.worldModel));
			
			if(ownGoals.size()<5){
				ArrayList<Goal>copyOwn = (ArrayList<Goal>) ownGoals.clone();
			
			
				Plan ownPlan = new Plan(copyOwn, pbc.worldModel);
				ownPlan.isPossiblePlan(allGoals.get(i), allGoals.get(i+1));
			
			
				jPlan = getBestPlan(allGoals, ownPlan.goals, otherGoals, bestOwn, bestOther, i++, minOtherValue);
			
			}
			if(otherGoals.size()<5){
				ArrayList<Goal>copyOther = (ArrayList<Goal>) otherGoals.clone();
			
				Plan otherPlan = new Plan(copyOther, pbc.worldModel); 
				otherPlan.isPossiblePlan(allGoals.get(i), allGoals.get(i+1));
			
				jPlan = getBestPlan(allGoals, ownGoals, copyOther, jPlan.ownPlan.goals, jPlan.otherPlan.goals, i++, minOtherValue);
			}
			
			return jPlan;
		}
		
			
			
			
			
			
		
		
		
		
	}
	
	
	private Plan getBestPlanForAddedGoals(Plan plan, ArrayList<Goal> goals){
		Plan bestPlan = null;
		
		for(int i=0;i<goals.size();i++){
			bestPlan = plan.isPossiblePlan(goals.get(i), goals.get(i+1));
		}
		
		
		return bestPlan;
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
