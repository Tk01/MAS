package robotPackage;

import java.util.ArrayList;

import world.ReturnChargeContents;

import com.github.rinde.rinsim.core.model.comm.CommUser;
import com.github.rinde.rinsim.core.model.comm.Message;
import com.github.rinde.rinsim.geom.Point;


public class CC {

	private PBC pbc;

	private long delay;

	private Long timeLastAction;

	JPlan jplan;


	boolean bidding = false;
	boolean startedNegotiating = false;

	private ArrayList<CommUser> losers;


	public CC(PBC pbc, long delay){
		this.pbc = pbc;
		this.delay=delay;


	}
	public void startNegotiation(Plan plan){
		timeLastAction = pbc.worldModel.time.getTime();
		startedNegotiating = true;
		Plan negotiationPlan = new Plan(pbc.getCurrentPlan().calculateGoals(timeLastAction+delay+1000),pbc.worldModel);
		losers =new ArrayList<CommUser>();
		jplan = new JPlan();
		jplan.setOwnPlan(new ArrayList<Goal>());
		jplan.setOtherPlan( negotiationPlan.getPlan());
		long currentTime = pbc.worldModel.getTime().getTime();
		long timeEndNegotiation = currentTime+delay;
		Point pos = pbc.currentplan.calculatePosition(timeEndNegotiation);
		long battery = pbc.currentplan.calculateBattery(timeEndNegotiation);
		double minValue = plan.value(plan.getPlan(), timeEndNegotiation);
		pbc.sendStartNegotiationMessage(pos, negotiationPlan.getPlan(),battery, timeLastAction+delay, minValue);



	}

	private Plan getNegotiationPlan(Plan plan){

		long negotiationTime = delay+1000;

		Plan negotiationPlan = plan.getNegotiationPlan(negotiationTime);

		return negotiationPlan;

	}





	public void handleMessage(Message message){
		


		MessageContent messageContent = (MessageContent) message.getContents();
		String type = messageContent.getType();

		//System.out.println(pbc.currentplan.getPlan().size());
		if(pbc.currentplan.getPlan().size()>3){
			//	System.out.println(pbc.currentplan.getPlan().size());
		}

		if(type.equals("StartNegotiation") ){
			ProcessStartNegotiation(message);

		}
		if(type.equals("NegotiationBidMessage")){
			processNegotiationBid(message);
			//jplans.add(((NegotiationBidMessageContent)messageContent).getJointPlan());			
		}
		if(type.equals("NegotiationReply")){
			processNegotiationReply(message);


		}



	}


	public void chargeMessage(ReturnChargeContents chargeContent){
		pbc.windows=chargeContent.getFreeSlots();
		if(chargeContent.hasSucceeded() && timeLastAction+delay> pbc.worldModel.getTime().getEndTime()){
			ArrayList<Goal> ownGoals = jplan.getOwnPlan();
			for(int i = 0; i<ownGoals.size();i++){

				if(ownGoals.get(i).type.equals("charging") && !((ChargeGoal)ownGoals.get(i)).isReserved() ){
					((ChargeGoal)ownGoals.get(i)).setReserved(true);
					pbc.sendNegotiationBidMessage(jplan, jplan.JPlanAgent);
					return;

				}
			}
			ArrayList<Goal> otherGoals = jplan.getOtherPlan();
			for(int i = 0; i<otherGoals.size();i++){

				if(otherGoals.get(i).type.equals("charging") && !((ChargeGoal)otherGoals.get(i)).isReserved() ){
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

		if(pbc.currentplan.getPlan().size()>3 && !(bidding || startedNegotiating)){
			long lastTime = messageContent.getEndTime();
			long currentTime = pbc.worldModel.getTime().getTime();
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
		this.pbc.worldModel.messages().remove(message);

	}

	private void processNegotiationReply(Message message){
		NegotiationReplyMessageContent messageContent = (NegotiationReplyMessageContent) message.getContents();
		if(messageContent.isAccepted() && bidding){
			if(pbc.currentplan !=null){
				@SuppressWarnings("unchecked")
				ArrayList<Goal> testplan = (ArrayList<Goal>) pbc.currentplan.getPlan().clone();
				removeCharge(testplan);
				ArrayList<Goal> comb = new ArrayList<Goal>();
				comb.addAll(jplan.getOwnPlan());
				comb.addAll(jplan.getOtherPlan());
				if(!comb.contains(testplan.get(0))){
					jplan.getOwnPlan().add(0,testplan.get(0));
				}
			}
			ChargeGoal lostChargeGoal1 = pbc.currentplan.lostChargeGoal(jplan.getOwnPlan());
			ChargeGoal lostChargeGoal2 = pbc.currentplan.lostChargeGoal(jplan.getOtherPlan());
			if(pbc.worldModel.isReserveChargingStation() && lostChargeGoal2!=null && lostChargeGoal1!=null){
				
				pbc.bbc.sendCancelReservationMessage(lostChargeGoal2.getStartWindow(), lostChargeGoal2.getEndWindow());
			}
			pbc.currentplan.setPlan(jplan.getOwnPlan());
			pbc.bbc.setGoal(pbc.currentplan.getNextgoal());
		}else{
			for(Goal g:jplan.ownPlan){
				if(g.type().equals("charging")){
					pbc.bbc.deleteChargeReservation(g.startWindow, g.endWindow);
				}
			}
		}
		jplan =null;
		bidding = false;
		this.pbc.worldModel.messages().remove(message);
	}
	public void sendBestNegMessage(){
		
			if( jplan.JPlanAgent!= null){
				if(pbc.currentplan !=null){
					@SuppressWarnings("unchecked")
					ArrayList<Goal> testplan = (ArrayList<Goal>) pbc.currentplan.getPlan().clone();
					removeCharge(testplan);
					ArrayList<Goal> comb = new ArrayList<Goal>();
					comb.addAll(jplan.getOwnPlan());
					comb.addAll(jplan.getOtherPlan());
					if(!comb.contains(testplan.get(0))){
						jplan.getOtherPlan().add(0,testplan.get(0));
					}
				}
				for(CommUser l:losers){
					pbc.sendNegativeNegotiationReplyMessage(l);
				}
				ChargeGoal lostChargeGoal1 = pbc.currentplan.lostChargeGoal(jplan.getOwnPlan());
				ChargeGoal lostChargeGoal2 = pbc.currentplan.lostChargeGoal(jplan.getOtherPlan());
				if(pbc.worldModel.isReserveChargingStation() && lostChargeGoal2!=null && lostChargeGoal1!=null){
					
					pbc.bbc.sendCancelReservationMessage(lostChargeGoal2.getStartWindow(), lostChargeGoal2.getEndWindow());
				}
				pbc.sendNegotiationReplyMessage(jplan.JPlanAgent);
				pbc.setCurrentplan(jplan.getOtherPlan());
				pbc.bbc.setGoal(pbc.currentplan.getNextgoal());
			}
			losers =null;
			jplan = null;
			startedNegotiating = false;

		}




	

	public ChargeGoal removeCharge(ArrayList<Goal> plan){


		for(int i=0;i<plan.size();i++){
			if(((Goal)plan.get(i)).type().equals("charging")){
				return (ChargeGoal) plan.remove(i);
			}

		}
		return null;
	}






	public JPlan bestJPlan( ArrayList<Goal> otherPlan, Point otherPos, long otherBat,long endtime,double minOtherValue ){
		ArrayList<Goal> otherplan2 = (ArrayList<Goal>) otherPlan.clone();
		Point ownPos = pbc.getCurrentPlan().calculatePosition(endtime+1000);
		long ownBat = pbc.getCurrentPlan().calculateBattery(endtime+1000);
		ArrayList<Goal> bestStartingGoals = pbc.getCurrentPlan().calculateGoals(endtime+1000);
		@SuppressWarnings("unchecked")
		ArrayList<Goal> ownPlan= (ArrayList<Goal>) bestStartingGoals.clone();
		ArrayList<Goal> combGoals = new ArrayList<Goal>();
		ChargeGoal ownCharge = removeCharge(ownPlan);
		ChargeGoal otherCharge = removeCharge(otherplan2);
		ArrayList<Goal> otherList = new ArrayList<Goal>();
		ArrayList<Goal> ownList = new ArrayList<Goal>();
		if(otherplan2.size()>0 && otherplan2.get(0).type().equals("drop") ){

			otherList.add(otherplan2.remove(0));
		}
		if(ownPlan.size()>0 && ownPlan.get(0).type().equals("drop") ){

			ownList.add(ownPlan.remove(0));
		}
		combGoals.addAll(ownPlan);
		combGoals.addAll(otherplan2);
		JPlan result = getBestPlan(combGoals,otherList ,otherPos, otherBat,ownList,ownPos,ownBat,bestStartingGoals,null,minOtherValue, ownCharge, otherCharge, endtime);
		


		return result;
	}

	@SuppressWarnings("unchecked")
	private JPlan getBestPlan(ArrayList<Goal> allGoals,
			ArrayList<Goal> otherGoals,Point otherPos, long otherBat, ArrayList<Goal> ownGoals, Point ownPos, long ownBat,
			ArrayList<Goal> bestOwn, ArrayList<Goal> bestOther, double minOtherValue, ChargeGoal ownCharge, ChargeGoal otherCharge, long endTime) {
		if(allGoals.size() ==0){
			Plan ownPlan = new Plan(ownGoals, pbc.worldModel);
			Plan otherPlan = new Plan(otherGoals, pbc.worldModel);
			ArrayList<Goal> ownList =  new ArrayList<Goal>();
			ArrayList<Goal> otherList =  new ArrayList<Goal>();
			ArrayList<Goal> ownGoals2 = (ArrayList<Goal>) ownGoals.clone();
			if(ownGoals2 .size()>0 && ownGoals2.get(0).type().equals("drop") ){

				ownList.add(ownGoals2.remove(0));
			}
			ArrayList<Goal> otherGoals2 = (ArrayList<Goal>) otherGoals.clone();
			if(otherGoals2 .size()>0 && otherGoals2.get(0).type().equals("drop") ){

				otherList.add(otherGoals2.remove(0));
			}
			ArrayList<Goal> tempBestOwnGoals = ownPlan.GenerateBestPlan(ownGoals2,ownList , ownCharge, bestOwn, pbc.windows, ownPos, ownBat, endTime+1000);
			ArrayList<Goal> tempOtherOwnGoals = otherPlan.GenerateBestPlan(otherGoals2, otherList, otherCharge, bestOther, pbc.windows, otherPos, otherBat, endTime);

			Plan tempBestOwnPlan = new Plan(tempBestOwnGoals, pbc.worldModel);
			Plan tempBestOtherPlan = new Plan(tempOtherOwnGoals, pbc.worldModel);

			//if(tempBestOwnPlan.valid(tempBestOwnGoals, pbc.windows, endTime, ownPos, ownBat) && tempBestOtherPlan.valid(tempOtherOwnGoals, pbc.windows, endTime, otherPos, otherBat)){

			if(tempBestOwnPlan.value(tempBestOwnGoals, endTime+1000)<tempBestOwnPlan.value(bestOwn, endTime+1000) && tempBestOtherPlan.value(tempOtherOwnGoals, endTime)<=minOtherValue){
				bestOwn=tempBestOwnGoals;
				bestOther = tempOtherOwnGoals;
			}
			//check otherGoals && ownGoals
			//}

			tempBestOwnGoals = ownPlan.GenerateBestPlan(ownGoals2, ownList, otherCharge, bestOwn, pbc.windows, ownPos, ownBat, endTime+1000);
			tempOtherOwnGoals = ownPlan.GenerateBestPlan(otherGoals2, otherList, ownCharge, bestOther, pbc.windows, otherPos, otherBat, endTime);

			tempBestOwnPlan = new Plan(tempBestOwnGoals, pbc.worldModel);
			tempBestOtherPlan = new Plan(tempOtherOwnGoals, pbc.worldModel);

			//if(tempBestOwnPlan.valid(tempBestOwnGoals, pbc.windows, endTime, ownPos, ownBat) && tempBestOtherPlan.valid(tempOtherOwnGoals, pbc.windows, endTime, otherPos, otherBat)){

			if(tempBestOwnPlan.value(tempBestOwnGoals, endTime+1000)<tempBestOwnPlan.value(bestOwn, endTime+1000) && tempBestOtherPlan.value(tempOtherOwnGoals, endTime)<minOtherValue){
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
			JPlan bestJplan = getBestPlan(allGoalsc,copyOtherGoals,otherPos, otherBat, copyOwnGoals, ownPos, ownBat, bestOwn,bestOther, minOtherValue,ownCharge,otherCharge, endTime);
			ArrayList<Goal>copyOwnGoals2 = (ArrayList<Goal>) ownGoals.clone();
			ArrayList<Goal>copyOtherGoals2 = (ArrayList<Goal>) otherGoals.clone();
			copyOtherGoals2.add(goal);
			copyOtherGoals2.add(goal2);
			return getBestPlan(allGoalsc,copyOtherGoals2,otherPos, otherBat, copyOwnGoals2, ownPos, ownBat, bestJplan.getOwnPlan(),bestJplan.getOtherPlan(), minOtherValue,ownCharge,otherCharge, endTime);
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

		if(timeLastAction!= null && timeLastAction+delay<=pbc.worldModel.getTime().getStartTime()){
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



}
