package robotPackage;

import java.util.ArrayList;

import com.github.rinde.rinsim.core.model.comm.CommUser;
import com.github.rinde.rinsim.core.model.comm.Message;
import com.github.rinde.rinsim.core.model.road.RoadUnits;
import com.github.rinde.rinsim.geom.Point;


public class CC {
	
	private PBC pbc;
	
	private long delay = 3000;
	
	private Long timeLastAction;
	
	private JPlan jplan;
	
	
	 boolean bidding = false;
	 boolean startedNegotiating = false;
	
	
	public CC(PBC pbc){
		this.pbc = pbc;
		
		
		
	}
	public void startNegotiation(Plan plan){
		timeLastAction = pbc.worldModel.time.getStartTime();
		startedNegotiating = true;
		Plan negotiationPlan = getNegotiationPlan(plan);
		jplan = new JPlan();
		jplan.setOwnPlan(negotiationPlan);
		pbc.sendStartNegotiationMessage(negotiationPlan, timeLastAction+delay);
		
		
		
	}
	
	private Plan getNegotiationPlan(Plan plan){
		
		long negotiationTime = delay+1000;
		
		Plan negotiationPlan = plan.getNegotiationPlan(negotiationTime);
		
		return negotiationPlan;
		
	}
	
	
	
	
	
	public void handleMessage(Message message){
		
		
		
		MessageContent messageContent = (MessageContent) message.getContents();
		String type = messageContent.getType();
		
		System.out.println(pbc.currentplan.goals.size());
		if(pbc.currentplan.goals.size()>3){
			System.out.println(pbc.currentplan.goals.size());
		}
		
		if(type.equals("StartNegotiation") ){
			ProcessStartNegotiation(message);			
		}
		if(type.equals("negotiationBid")){
			processNegotiationBid(message);
			//jplans.add(((NegotiationBidMessageContent)messageContent).getJointPlan());			
		}
		if(type.equals("negotiationReply")){
			processNegotiationReply(message);
			
			
		}
		
		
	}
	
	
	
	private void ProcessStartNegotiation(Message message){
		StartNegotiationMessageContent messageContent = (StartNegotiationMessageContent) message.getContents();
		
		if(pbc.currentplan.goals.size()>3 && !(bidding || startedNegotiating)){
			long lastTime = messageContent.getEndTime();
			long currentTime = pbc.worldModel.getTime().getTime();
			if(currentTime<lastTime){
			
				Plan otherNegotiationPlan = ((StartNegotiationMessageContent) messageContent).getPlan();
				Plan ownNegotiationPlan = pbc.currentplan.getNegotiationPlan(lastTime-currentTime);
				JPlan jointPlan = bestJPlan(otherNegotiationPlan, ownNegotiationPlan);
				if(jointPlan!=null){
					bidding = true;
					timeLastAction = lastTime - delay; 
					
					//jplans.add(jointPlan);
					pbc.sendNegotiationBidMessage(jointPlan, message.getSender());
				}
			}
		}
		
	}
	
	private void processNegotiationBid(Message message){
		NegotiationBidMessageContent messageContent = (NegotiationBidMessageContent) message.getContents();
		
		JPlan receivedJPlan = messageContent.getJointPlan();
		if(receivedJPlan.getOwnPlan().value(receivedJPlan.getOwnPlan().goals)<this.jplan.getOwnPlan().value(this.jplan.getOwnPlan().goals)){
			jplan = receivedJPlan;
		}
		
	}
	
	private void processNegotiationReply(Message message){
		NegotiationReplyMessageContent messageContent = (NegotiationReplyMessageContent) message.getContents();
		if(messageContent.isAccepted()){
			pbc.currentplan = jplan.getOtherPlan();
		}
	}
	
	
	
	
	
	
	
	
	
	public void sendBestNegMessage(){
		if(pbc.worldModel.time.getStartTime()>timeLastAction+delay && jplan.JPlanAgent!= null ){
			pbc.sendNegotiationReplyMessage(jplan.JPlanAgent);
			
		}
		startedNegotiating = false;
		
	}
	

	

	
	public JPlan bestJPlan( ArrayList<Goal> otherPlan, Point otherPos, long otherBat,long endtime,double minOtherValue ){
		Point ownPos = pbc.getCurrentPlan().calculatePosition(endtime+1000);
		long ownBat = pbc.getCurrentPlan().calculateBattery(endtime+1000);
		ArrayList<Goal> ownPlan= (ArrayList<Goal>) pbc.getCurrentPlan().calculateGoals(endtime+1000).clone();
		ArrayList<Goal> combGoals = new ArrayList<Goal>();
		ChargeGoal ownCharge = removeCharge(ownPlan);
		ChargeGoal otherCharge = removeCharge(otherPlan);
		ArrayList<Goal> otherList = new ArrayList<Goal>();
		ArrayList<Goal> ownList = new ArrayList<Goal>();
		if(otherPlan.size()>0 && otherPlan.get(0).type().equals("drop") ){

			otherList.add(otherPlan.remove(0));
		}
		if(ownPlan.size()>0 && ownPlan.get(0).type().equals("drop") ){

			ownList.add(ownPlan.remove(0));
		}
		combGoals.addAll(ownPlan);
		combGoals.addAll(otherPlan);
		JPlan result = getBestPlan(combGoals,otherList ,otherPos, otherBat,ownList,ownPos,ownBat,null,minOtherValue);
		return result;
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
			
			
			if(bestOwn.size()<5){
				
			
			
				Plan ownPlan = new Plan(ownGoals, pbc.worldModel);
				ownPlan.isPossiblePlan(allGoals.get(i), allGoals.get(i+1), pbc.windows);
			
			
				jPlan = getBestPlan(allGoals, ownPlan.goals, otherGoals, bestOwn, bestOther, i++, minOtherValue);
			
			}
			if(bestOther.size()<5){
				
			
				Plan otherPlan = new Plan(otherGoals, pbc.worldModel); 
				otherPlan.isPossiblePlan(allGoals.get(i), allGoals.get(i+1),  pbc.windows);
			
				jPlan = getBestPlan(allGoals, ownGoals, otherPlan.goals, jPlan.ownPlan.goals, jPlan.otherPlan.goals, i++, minOtherValue);
			}
			
			jPlan.setOwnPlan(new Plan(bestOwn, pbc.worldModel));
			jPlan.setOtherPlan(new Plan(bestOther, pbc.worldModel));
			
			return jPlan;
		}
		
			
			
			
			
			
		
		
		
		
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
		
		if(timeLastAction!= null && timeLastAction+delay<pbc.worldModel.getTime().getStartTime()){
			if(bidding){
				bidding = false;
				
			}
			if(startedNegotiating){
				sendBestNegMessage();
			}
		}
		
	}
	
	
	
}
