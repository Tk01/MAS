package robotPackage;

import java.util.ArrayList;

import world.ReturnChargestationMessageContents;

import com.github.rinde.rinsim.core.model.comm.CommUser;
import com.github.rinde.rinsim.core.model.comm.Message;
import com.github.rinde.rinsim.geom.Point;


public class CC {

	private PBC pbc;

	private long delay;

	private Long timeLastAction;

	private JPlan jplan;


	private boolean bidding = false;
	private boolean startedNegotiating = false;

	private ArrayList<CommUser> losers;
	private WorldModel model;

	public CC(PBC pbc, long delay, WorldModel model){
		this.pbc = pbc;
		this.delay=delay;
		this.model = model;

	}
	public void startNegotiation(){
		timeLastAction = model.getTime().getTime();
		startedNegotiating = true;
		Plan negotiationPlan = new Plan(pbc.getCurrentPlan().calculateGoals(timeLastAction+delay+1000),model);
		losers =new ArrayList<CommUser>();
		jplan = new JPlan();
		jplan.setOwnPlan(new ArrayList<Goal>());
		jplan.setOtherPlan( negotiationPlan.getPlan());
		long currentTime = model.getTime().getTime();
		long timeEndNegotiation = currentTime+delay;
		Point pos = pbc.getCurrentPlan().calculatePosition(timeEndNegotiation);
		long battery = pbc.getCurrentPlan().calculateBattery(timeEndNegotiation);
		double minValue = pbc.getCurrentPlan().value(pbc.getCurrentPlan().getPlan(), timeEndNegotiation);
		pbc.sendStartNegotiationMessage(pos, negotiationPlan.getPlan(),battery, timeLastAction+delay, minValue);



	}

//	private Plan getNegotiationPlan(Plan plan){
//
//		long negotiationTime = delay+1000;
//
//		Plan negotiationPlan = plan.getNegotiationPlan(negotiationTime);
//
//		return negotiationPlan;
//
//	}





	public void handleMessage(Message message){
		


		MessageContent messageContent = (MessageContent) message.getContents();
		MessageTypes type = messageContent.getType();

		//System.out.println(pbc.getCurrentPlan().getPlan().size());
		if(pbc.getCurrentPlan().getPlan().size()>3){
			//	System.out.println(pbc.getCurrentPlan().getPlan().size());
		}

		if(type ==MessageTypes.StartNegotiationMessage ){
			ProcessStartNegotiation(message);

		}
		if(type == MessageTypes.NegotiationBidMessage){
			processNegotiationBid(message);
			//jplans.add(((NegotiationBidMessageContent)messageContent).getJointPlan());			
		}
		if(type == MessageTypes.NegotiationReplyMessage){
			processNegotiationReply(message);


		}



	}


	public void chargeMessage(ReturnChargestationMessageContents chargeContent){
		pbc.windows=chargeContent.getFreeSlots();
		if(chargeContent.hasSucceeded() && timeLastAction+delay> model.getTime().getEndTime()){
			ArrayList<Goal> ownGoals = jplan.getOwnPlan();
			for(int i = 0; i<ownGoals.size();i++){

				if(ownGoals.get(i).type() == GoalTypes.Charging && !((ChargeGoal)ownGoals.get(i)).isReserved() ){
					((ChargeGoal)ownGoals.get(i)).setReserved(true);
					pbc.sendNegotiationBidMessage(jplan, jplan.JPlanAgent);
					return;

				}
			}
			ArrayList<Goal> otherGoals = jplan.getOtherPlan();
			for(int i = 0; i<otherGoals.size();i++){

				if(otherGoals.get(i).type() == GoalTypes.Charging && !((ChargeGoal)otherGoals.get(i)).isReserved() ){
					((ChargeGoal)otherGoals.get(i)).setReserved(true);
					pbc.sendNegotiationBidMessage(jplan, jplan.JPlanAgent);
					return;

				}
			}
			pbc.sendNegotiationBidMessage(jplan, jplan.JPlanAgent);
		}
		else{
			bidding= false;
			jplan = null;
		}
	}


	private void ProcessStartNegotiation(Message message){
		StartNegotiationMessageContent messageContent = (StartNegotiationMessageContent) message.getContents();

		if(pbc.getCurrentPlan().getPlan().size()>3 && !(bidding || startedNegotiating)){
			long lastTime = messageContent.getEndTime();
			long currentTime = model.getTime().getTime();
			if(currentTime<lastTime){

				ArrayList<Goal> otherNegotiationPlan = messageContent.getPlan();
				Point otherPos = messageContent.getPosition();
				long otherBat = messageContent.getBattery();
				long endTime = messageContent.getEndTime();
				double minOtherValue = messageContent.getMinValue();




				JPlan jointPlan = bestJPlan(otherNegotiationPlan, otherPos, otherBat, endTime, minOtherValue);
				/*jointPlan.setJPlanAgent(message.getSender());*/

				
				if(jointPlan!=null && jointPlan.getOtherPlan()!= null && jointPlan.getOwnPlan()!= null ){
					
					bidding = true;
					timeLastAction = lastTime - delay; 
					jplan = jointPlan;
					jplan.setJPlanAgent(message.getSender());
					pbc.sendNegotiationBidMessage(jointPlan, message.getSender());
				}
			}

		}

	}


	private void processNegotiationBid(Message message){
		NegotiationBidMessageContent messageContent = (NegotiationBidMessageContent) message.getContents();

		JPlan receivedJPlan = messageContent.getJointPlan();
		if(startedNegotiating && jplan != null && pbc.getCurrentPlan().value(receivedJPlan.getOtherPlan(),timeLastAction+delay)<pbc.getCurrentPlan().value(this.jplan.getOtherPlan(), timeLastAction+delay)){
			if(jplan.JPlanAgent!=null)losers.add(jplan.JPlanAgent);
			jplan = receivedJPlan;
			
		}
		this.model.messages().remove(message);

	}

	private void processNegotiationReply(Message message){
		NegotiationReplyMessageContent messageContent = (NegotiationReplyMessageContent) message.getContents();
		if(messageContent.isAccepted() && bidding){
			this.setNewJointPlan(jplan, false);
		}else{
			for(Goal g:jplan.ownPlan){
				if(g.type()==GoalTypes.Charging){
					pbc.sendCancelReservationMessage(g.getStartWindow(), g.getEndWindow());
				}
			}
		}
		jplan =null;
		bidding = false;
		this.model.messages().remove(message);
	}
	public void sendBestNegMessage(){
			if( jplan.JPlanAgent!= null){
				for(CommUser l:losers){
					pbc.sendNegativeNegotiationReplyMessage(l);
				}
				this.setNewJointPlan(jplan,true);
				pbc.sendNegotiationReplyMessage(jplan.JPlanAgent);
			}
			losers =null;
			jplan = null;
			startedNegotiating = false;

		}




	

	public ChargeGoal removeCharge(ArrayList<Goal> plan){


		for(int i=0;i<plan.size();i++){
			if(((Goal)plan.get(i)).type() ==GoalTypes.Charging){
				return (ChargeGoal) plan.remove(i);
			}

		}
		return null;
	}






	@SuppressWarnings("unchecked")
	public JPlan bestJPlan( ArrayList<Goal> otherPlan, Point otherPos, long otherBat,long endtime,double minOtherValue ){
		ArrayList<Goal> otherplan2 = (ArrayList<Goal>) otherPlan.clone();
		Point ownPos = pbc.getCurrentPlan().calculatePosition(endtime+1000);
		long ownBat = pbc.getCurrentPlan().calculateBattery(endtime+1000);
		ArrayList<Goal> bestStartingGoals = pbc.getCurrentPlan().calculateGoals(endtime+1000);
		ArrayList<Goal> ownPlan= (ArrayList<Goal>) bestStartingGoals.clone();
		ArrayList<Goal> combGoals = new ArrayList<Goal>();
		ChargeGoal ownCharge = removeCharge(ownPlan);
		ChargeGoal otherCharge = removeCharge(otherplan2);
		ArrayList<Goal> otherList = new ArrayList<Goal>();
		ArrayList<Goal> ownList = new ArrayList<Goal>();
		if(otherplan2.size()>0 && otherplan2.get(0).type() == GoalTypes.Drop ){

			otherList.add(otherplan2.remove(0));
		}
		if(ownPlan.size()>0 && ownPlan.get(0).type() == GoalTypes.Drop ){

			ownList.add(ownPlan.remove(0));
		}
		combGoals.addAll(ownPlan);
		combGoals.addAll(otherplan2);
		JPlan result = getBestPlan(combGoals,otherList ,otherPos, otherBat,ownList,ownPos,ownBat,bestStartingGoals,null,minOtherValue, ownCharge, otherCharge, endtime, otherBat,otherPos);
		


		return result;
	}

	@SuppressWarnings("unchecked")
	private JPlan getBestPlan(ArrayList<Goal> allGoals,
			ArrayList<Goal> otherGoals,Point otherPos, long otherBat, ArrayList<Goal> ownGoals, Point ownPos, long ownBat,
			ArrayList<Goal> bestOwn, ArrayList<Goal> bestOther, double minOtherValue, ChargeGoal ownCharge, ChargeGoal otherCharge, long endTime, long tempOtherBattery, Point tempOtherPos) {
		if(allGoals.size() ==0){
			ArrayList<Goal> ownList =  new ArrayList<Goal>();
			ArrayList<Goal> otherList =  new ArrayList<Goal>();
			ArrayList<Goal> ownGoals2 = (ArrayList<Goal>) ownGoals.clone();
			if(ownGoals2 .size()>0 && ownGoals2.get(0).type() == GoalTypes.Drop ){
				ownList.add(ownGoals2.remove(0));
			}
			ArrayList<Goal> otherGoals2 = (ArrayList<Goal>) otherGoals.clone();
			if(otherGoals2 .size()>0 && otherGoals2.get(0).type() == GoalTypes.Drop ){
				otherList.add(otherGoals2.remove(0));
			}
			ArrayList<Goal> tempBestOwnGoals = Plan.GenerateBestPlan(ownGoals2,ownList , ownCharge, bestOwn, pbc.windows, ownPos, ownBat, endTime+1000, model);
			ArrayList<Goal> tempOtherOwnGoals = Plan.GenerateBestPlan(otherGoals2, otherList, otherCharge, bestOther, pbc.windows, otherPos, otherBat, endTime, model);


			//if(tempBestOwnPlan.valid(tempBestOwnGoals, pbc.windows, endTime, ownPos, ownBat) && tempBestOtherPlan.valid(tempOtherOwnGoals, pbc.windows, endTime, otherPos, otherBat)){

			if(pbc.getCurrentPlan().value(tempBestOwnGoals, endTime+1000)<pbc.getCurrentPlan().value(bestOwn, endTime+1000) && Plan.value(tempOtherOwnGoals, tempOtherBattery,tempOtherPos, endTime, model)<=minOtherValue){
				bestOwn=tempBestOwnGoals;
				bestOther = tempOtherOwnGoals;
			}
			//check otherGoals && ownGoals
			//}

			tempBestOwnGoals = Plan.GenerateBestPlan(ownGoals2, ownList, otherCharge, bestOwn, pbc.windows, ownPos, ownBat, endTime+1000, model);
			tempOtherOwnGoals = Plan.GenerateBestPlan(otherGoals2, otherList, ownCharge, bestOther, pbc.windows, otherPos, otherBat, endTime, model);


			//if(tempBestOwnPlan.valid(tempBestOwnGoals, pbc.windows, endTime, ownPos, ownBat) && tempBestOtherPlan.valid(tempOtherOwnGoals, pbc.windows, endTime, otherPos, otherBat)){

			if(pbc.getCurrentPlan().value(tempBestOwnGoals, endTime+1000)<pbc.getCurrentPlan().value(bestOwn, endTime+1000) && Plan.value(tempOtherOwnGoals, tempOtherBattery,tempOtherPos, endTime, model)<=minOtherValue){
				bestOwn=tempBestOwnGoals;
				bestOther = tempOtherOwnGoals;
			}
			//check otherGoals && ownGoals
			//}

			JPlan jplan = new JPlan();
			jplan.setOwnPlan(bestOwn);
			jplan.setOtherPlan(bestOther);
			return jplan;
		}else{
			ArrayList<Goal> allGoalsc = (ArrayList<Goal>) allGoals.clone();
			Goal goal = allGoalsc.remove(0);
			Goal goal2 = allGoalsc.remove(0);
			ArrayList<Goal>copyOtherGoals = (ArrayList<Goal>) otherGoals.clone();
			ArrayList<Goal>copyOwnGoals = (ArrayList<Goal>) ownGoals.clone();
			copyOwnGoals.add(goal);
			copyOwnGoals.add(goal2);
			JPlan bestJplan = getBestPlan(allGoalsc,copyOtherGoals,otherPos, otherBat, copyOwnGoals, ownPos, ownBat, bestOwn,bestOther, minOtherValue,ownCharge,otherCharge, endTime, tempOtherBattery, tempOtherPos);
			ArrayList<Goal>copyOwnGoals2 = (ArrayList<Goal>) ownGoals.clone();
			ArrayList<Goal>copyOtherGoals2 = (ArrayList<Goal>) otherGoals.clone();
			copyOtherGoals2.add(goal);
			copyOtherGoals2.add(goal2);
			return getBestPlan(allGoalsc,copyOtherGoals2,otherPos, otherBat, copyOwnGoals2, ownPos, ownBat, bestJplan.getOwnPlan(),bestJplan.getOtherPlan(), minOtherValue,ownCharge,otherCharge, endTime, tempOtherBattery, tempOtherPos);
		}





	}

	
	/*
	public void evaluateJPlans(){
		JPlan bestJPlan = jplans.get(0);
		double bestPlanValue = jplans.get(0).getOtherPlan()pbc.getCurrentPlan().value(jplans.get(0).getOtherPlan().goals);
		for(int i=1; i<jplans.size();i++){
			double planValue = jplans.get(i).getOtherPlan()pbc.getCurrentPlan().value(jplans.get(i).getOtherPlan().goals);
			if(bestPlanValue> planValue){
				bestJPlan = jplans.get(i);
				bestPlanValue = planValue;
			}
		}

		pbc.sendConfirmationMessage(bestJPlan);
	}
	 */
	public void checkNegotiation() {

		if(timeLastAction!= null && timeLastAction+delay<=model.getTime().getStartTime()){
//			if(bidding){
//				jplan =null;
//				bidding = false;
//
//			}
			if(startedNegotiating){
				sendBestNegMessage();
			}
		}

	}
	private void setNewJointPlan(JPlan newPlan,boolean other){
		if(pbc.getCurrentPlan() !=null){
			@SuppressWarnings("unchecked")
			ArrayList<Goal> testplan = (ArrayList<Goal>) pbc.getCurrentPlan().getPlan().clone();
			removeCharge(testplan);
			ArrayList<Goal> comb = new ArrayList<Goal>();
			comb.addAll(jplan.getOwnPlan());
			comb.addAll(jplan.getOtherPlan());
			if(!comb.contains(testplan.get(0))){
				jplan.getOtherPlan().add(0,testplan.get(0));
			}
		}
		
		ChargeGoal lostChargeGoal1 = pbc.getCurrentPlan().lostChargeGoal(jplan.getOwnPlan());
		ChargeGoal lostChargeGoal2 = pbc.getCurrentPlan().lostChargeGoal(jplan.getOtherPlan());
		if(model.isReserveChargingStation() && lostChargeGoal2!=null && lostChargeGoal1!=null){
			pbc.sendCancelReservationMessage(lostChargeGoal2.getStartWindow(), lostChargeGoal2.getEndWindow());
		}
		if(other)pbc.forcefullSetNewPlan(jplan.getOtherPlan());
		else{
			if(other)pbc.forcefullSetNewPlan(jplan.getOwnPlan());
		}
	}
	public boolean IsBidding() {
		return bidding;
	}
	public boolean IsNegotiating() {
		return startedNegotiating;
	}


}
