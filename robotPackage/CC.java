package robotPackage;

import java.util.ArrayList;

import com.github.rinde.rinsim.core.model.comm.CommUser;
import com.github.rinde.rinsim.core.model.comm.Message;


public class CC {
	
	private PBC pbc;
	
	private JPlan jplan;
	
	private CommUser jPlanUser;
	
	
	
	public CC(){
		jplan = null;
		jPlanUser = null;
		
	}
	public void startNegotiation(Plan plan){
		
		pbc.sendStartNegotiationMessage(plan);
		
		
	}
	public void handleMessage(Message message){
		
		MessageContent messageContent = (MessageContent) message.getContents();
		String type = messageContent.getType();
		
		
		if(type.equals("startNegotiation") && jplan == null){
			Plan negPlan = ((StartNegotiationMessageContent) messageContent).getPlan();
			JPlan jointPlan = getBestJointPlan(negPlan);
			if(jointPlan != null){
				jplan = jointPlan;
				pbc.sendNegotiationBidMessage(jointPlan, message.getSender());
			}
		}
		if(type.equals("negotiationBid")){
			if(((NegotiationBidMessageContent)messageContent).isAccepted()){
				pbc.currentplan = this.jplan.getOwnPlan();
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
	
	
}
