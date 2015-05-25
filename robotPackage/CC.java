package robotPackage;

import java.util.ArrayList;

import com.github.rinde.rinsim.core.model.comm.CommUser;
import com.github.rinde.rinsim.core.model.comm.Message;


public class CC {
	
	private PBC pbc;
	
	private long delay = 3000;
	
	private Long timeLastAction;
	
	private JPlan jplan = new JPlan();
	
	
	private boolean ongoing = false;
	
	
	public CC(PBC pbc){
		this.pbc = pbc;
		
		
		
	}
	public void startNegotiation(Plan plan){
		
		
		
		timeLastAction = pbc.worldModel.time.getStartTime();
		ongoing = true;
		jplan.setOwnPlan(plan);
		pbc.sendStartNegotiationMessage(plan, timeLastAction+delay);
		
		
		
	}
	
	
	
	public boolean isOngoing() {
		return ongoing;
	}
	public void setOngoing(boolean ongoing) {
		this.ongoing = ongoing;
	}
	public void handleMessage(Message message){
		
		if(ongoing && pbc.worldModel.time.getStartTime()>timeLastAction+delay ){
			ongoing = false;
		}
		
		MessageContent messageContent = (MessageContent) message.getContents();
		String type = messageContent.getType();
		
		System.out.println(pbc.currentplan.goals.size());
		if(pbc.currentplan.goals.size()>3){
			System.out.println(pbc.currentplan.goals.size());
		}
		
		if(type.equals("StartNegotiation") && pbc.currentplan.goals.size()>3 && !ongoing){
			
			long lastTime = ((StartNegotiationMessageContent)messageContent).getEndTime();
			long currentTime = pbc.worldModel.getTime().getTime();
			if(currentTime<lastTime){
			
				Plan negPlan = ((StartNegotiationMessageContent) messageContent).getPlan();
				JPlan jointPlan = bestJPlan(negPlan);
				if(jointPlan!=null){
					ongoing = true;
					
					//jplans.add(jointPlan);
					timeLastAction = pbc.worldModel.time.getStartTime();
					pbc.sendNegotiationBidMessage(jointPlan, message.getSender(), timeLastAction+delay);
				}
			}
			
			
		}
		if(type.equals("negotiationBid")){
			//jplans.add(((NegotiationBidMessageContent)messageContent).getJointPlan());
			
			JPlan receivedJPlan = ((NegotiationBidMessageContent)messageContent).getJointPlan();
			if(receivedJPlan.getOwnPlan().value(receivedJPlan.getOwnPlan().goals)<this.jplan.getOwnPlan().value(this.jplan.getOwnPlan().goals)){
				jplan = receivedJPlan;
			}
			
			
			
			
			
			
			
		}
		if(type.equals("negotiationReply")){
			if(((NegotiationReplyMessageContent)messageContent).isAccepted() && !pbc.negotiating){
				pbc.currentplan = jplan.getOtherPlan();
				ongoing = false;
			}
			if(((NegotiationReplyMessageContent)messageContent).isAccepted() && pbc.negotiating){
				pbc.currentplan = jplan.getOtherPlan();
				ongoing = false;
			}
			
		}
		
		
	}
	
	public void sendBestNegMessage(){
		if(ongoing && pbc.worldModel.time.getStartTime()>timeLastAction+delay ){
			pbc.sendNegotiationReplyMessage(jplan.JPlanAgent);
			ongoing = false;
		}
	}
	
	private JPlan setBestJplan() {
		/*
		for(JPlan jplan: jplans){
			if(jplan.get)
		}*/
		return null;
	}
	
	// This method will filter the goals so no ongoing deliveries are used to find the best plan.
	private ArrayList<Goal> filterGoals(ArrayList<Goal> goals){
		
		for(int i =0; i<goals.size();i++){
			if(goals.get(0).type.equals("pickup") && i>0){
				return goals;
			}
			goals.remove(0);
		}
		
		return goals;
	}

	
	public JPlan bestJPlan(Plan otherPlan){
		Plan ownPlanNoCharging = pbc.currentplan.returnPlanWithoutCharging();
		Plan otherPlanNoCharging = otherPlan.returnPlanWithoutCharging();
		
		ArrayList<Goal> allGoals = ownPlanNoCharging.goals;
		allGoals.addAll(otherPlanNoCharging.goals);
		
		ArrayList<Goal> ownGoals = ownPlanNoCharging.goals;
		ArrayList<Goal> otherGoals = otherPlanNoCharging.goals;
		
		ownGoals = filterGoals((ArrayList<Goal>) ownGoals.clone());
		
		otherGoals = filterGoals((ArrayList<Goal>) otherGoals.clone());
		
		
		
		
		
		JPlan bestJPlan = getBestPlan(allGoals, ownGoals, otherGoals, new ArrayList<Goal>(), new ArrayList<Goal>(), 0, otherPlan.value(otherPlan.goals));
		
		return null;
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
			
			if(bestOwn.size()<5){
				ArrayList<Goal>copyOwn = (ArrayList<Goal>) ownGoals.clone();
			
			
				Plan ownPlan = new Plan(copyOwn, pbc.worldModel);
				ownPlan.isPossiblePlan(allGoals.get(i), allGoals.get(i+1), pbc.windows);
			
			
				jPlan = getBestPlan(allGoals, ownPlan.goals, otherGoals, bestOwn, bestOther, i++, minOtherValue);
			
			}
			if(bestOther.size()<5){
				ArrayList<Goal>copyOther = (ArrayList<Goal>) otherGoals.clone();
			
				Plan otherPlan = new Plan(copyOther, pbc.worldModel); 
				otherPlan.isPossiblePlan(allGoals.get(i), allGoals.get(i+1), pbc.windows);
			
				jPlan = getBestPlan(allGoals, ownGoals, copyOther, jPlan.ownPlan.goals, jPlan.otherPlan.goals, i++, minOtherValue);
			}
			
			return jPlan;
		}
		
			
			
			
			
			
		
		
		
		
	}
	
	
	private Plan getBestPlanForAddedGoals(Plan plan, ArrayList<Goal> goals){
		Plan bestPlan = null;
		
		for(int i=0;i<goals.size();i++){
			bestPlan = plan.isPossiblePlan(goals.get(i), goals.get(i+1), pbc.windows);
		}
		
		
		return bestPlan;
	}
	
	
	public void negotiationAbort(){
		
	}
	/*
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
	*/
	public void checkNegotiation() {
		if(timeLastAction!= null){
			if(pbc.worldModel.time.getEndTime()>timeLastAction+delay && pbc.negotiating){
				
				pbc.returnNegPlan(jplan.ownPlan);
			}
			else if (pbc.worldModel.time.getEndTime()>timeLastAction+delay && !pbc.negotiating){
				ongoing=false;
			}
			if(ongoing && pbc.worldModel.time.getStartTime()>timeLastAction+delay ){
				pbc.sendNegotiationReplyMessage(jplan.JPlanAgent);
				ongoing = false;
			}
			if(pbc.negotiating && pbc.worldModel.time.getStartTime()>timeLastAction+delay ){
				pbc.returnNegPlan(jplan.ownPlan);
				ongoing = false;
			}
		}
		
	}
	
	
	
}
