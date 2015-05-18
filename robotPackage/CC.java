package robotPackage;

import java.util.ArrayList;

import com.github.rinde.rinsim.core.model.comm.CommUser;
import com.github.rinde.rinsim.core.model.comm.Message;


public class CC {
	
	private PBC pbc;
	
	private ArrayList<JPlan> jplans = new ArrayList<JPlan>();
	
	
	
	
	
	public CC(){
		
		
		
	}
	public void startNegotiation(Plan plan){
		
		pbc.sendStartNegotiationMessage(plan);
		
		
	}
	public void handleMessage(Message message){
		
		MessageContent messageContent = (MessageContent) message.getContents();
		String type = messageContent.getType();
		
		
		if(type.equals("startNegotiation") && jplans.isEmpty()){
			Plan negPlan = ((StartNegotiationMessageContent) messageContent).getPlan();
			JPlan jointPlan = getBestJointPlan(negPlan);
			jplans.add(jointPlan);
			pbc.sendNegotiationBidMessage(jointPlan, message.getSender());
			
		}
		if(type.equals("negotiationBid")){
			jplans.add(((NegotiationBidMessageContent)messageContent).getJointPlan());
			
			
		}
		if(type.equals("negotiationReply")){
			if(((NegotiationReplyMessageContent)messageContent).isAccepted()){
				pbc.currentplan = jplans.get(0).getOwnPlan();
			}
			
		}
		
		
	}
	private JPlan getBestJointPlan(Plan negPlan) {
		
		ArrayList <Goal> negGoals = negPlan.getPlan();
		ArrayList <Goal> ownGoals = pbc.getCurrentPlan().getPlan();
		
		double valueNegPlan = negPlan.value(negPlan.getPlan());
		double valueCurrentPlan = pbc.getCurrentPlan().value(pbc.getCurrentPlan().getPlan());
		
		Plan tempNegPlan = new Plan(negGoals, pbc.worldModel);
		Plan tempOwnPlan = new Plan(ownGoals, pbc.worldModel);
		
		
		for(int i = 0; i<negGoals.size();i++){
			for(int j=0; j< ownGoals.size();j++){
				if(negGoals.get(i).type.equals("pickup") && negGoals.get(i+1).type.equals("drop")&& ownGoals.get(j).type.equals("pickup")){
					tempOwnPlan.goals.add(j,negGoals.get(i));
					tempOwnPlan.goals.add(j+1,negGoals.get(i+1));
				}
			}
			
		}
		
		


		return null;
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
