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

	
	/**
	 * The constructor of the CC
	 * @param pbc: The link to the PBC
	 * @param delay: the delay is the time the negotiation will wait for bids of other agents
	 * @param model: the worldmodel
	 */
	public CC(PBC pbc, long delay, WorldModel model){
		this.pbc = pbc;
		this.delay=delay;
		this.model = model;

	}
	
	/**
	 * This method will initiate the negotiation.
	 * It will calculate a negotiationplan dependent on the current plan and the delay and sends all the needed info in a broadcast message
	 */
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



	/**
	 * Method will process the received negotiation message
	 * Dependent on the type of message different actions will be taken
	 * @param message: the negotiation message
	 */
	public void handleMessage(Message message){
		


		MessageContent messageContent = (MessageContent) message.getContents();
		MessageTypes type = messageContent.getType();


		if(type ==MessageTypes.StartNegotiationMessage ){
			ProcessStartNegotiation(message);

		}
		if(type == MessageTypes.NegotiationBidMessage){
			processNegotiationBid(message);
						
		}
		if(type == MessageTypes.NegotiationReplyMessage){
			processNegotiationReply(message);


		}



	}

	
	/**
	 * If the drone is bidding and reservation of the charging station is needed, the CC needs to process the reservation message.
	 * If the reservation is seuccesfull, the drone can send a bid to the negotiation initiator.
	 * @param chargeContent: the content of the reservation message
	 */
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

	
	/**
	 * This method will process the message to start a negotiation.
	 * It checks if there are packages to negotiate with and if no other negotiation is ongoing.
	 * If this is ok he will check if there is a plan combination which is better for both which the drone can use for negotiation
	 * @param message: the message from the initiator of the negotiation
	 */
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

	/**
	 * Process a bid from another drone
	 * Best bet is the new joint plan
	 * @param message: message with the bid of the other drone
	 */
	private void processNegotiationBid(Message message){
		NegotiationBidMessageContent messageContent = (NegotiationBidMessageContent) message.getContents();

		JPlan receivedJPlan = messageContent.getJointPlan();
		if(startedNegotiating && jplan != null && pbc.getCurrentPlan().value(receivedJPlan.getOtherPlan(),timeLastAction+delay)<pbc.getCurrentPlan().value(this.jplan.getOtherPlan(), timeLastAction+delay)){
			if(jplan.JPlanAgent!=null)losers.add(jplan.JPlanAgent);
			jplan = receivedJPlan;
			
		}
		
		this.model.messages().remove(message);

	}
	
	/**
	 * Process the reply for a bid.
	 * If the bid is accpeted the proposed plans are set up. If not the old plan stays and if a reservation for the charging station has been done it is cancelled.
	 * @param message: the reply message
	 */

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
	
	/**
	 * When the negotiation ends a message is send to all drones who did a bid. An accept to the best bid and a reject to the other bids.
	 */
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




	
	/**
	 * Removes a chargeGoal from the list of goals
	 * @param plan: the plan for which it wants to remove the charging goal
	 * @return: the removed charging goal
	 */
	public ChargeGoal removeCharge(ArrayList<Goal> plan){


		for(int i=0;i<plan.size();i++){
			if(((Goal)plan.get(i)).type() ==GoalTypes.Charging){
				return (ChargeGoal) plan.remove(i);
			}

		}
		return null;
	}





	/**
	 * Sets up the calculation for getting the best joint plan
	 * @param otherPlan: the plan of the other drone
	 * @param otherPos: the position of the other drone at the time of the negotiation end
	 * @param otherBat: the battery life of the drone at the time of the negotiation end
	 * @param endtime: the end time of the negotiation
	 * @param minOtherValue: the value of the other plan
	 * @return: Returns a best joint plan
	 */
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
	
	/**
	 * Does the calculation of the best joint plan in a recursive way.

	 */
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


			
			if(pbc.getCurrentPlan().value(tempBestOwnGoals, endTime+1000)<pbc.getCurrentPlan().value(bestOwn, endTime+1000) && Plan.value(tempOtherOwnGoals, tempOtherBattery,tempOtherPos, endTime, model)<=minOtherValue){
				bestOwn=tempBestOwnGoals;
				bestOther = tempOtherOwnGoals;
			}
		

			tempBestOwnGoals = Plan.GenerateBestPlan(ownGoals2, ownList, otherCharge, bestOwn, pbc.windows, ownPos, ownBat, endTime+1000, model);
			tempOtherOwnGoals = Plan.GenerateBestPlan(otherGoals2, otherList, ownCharge, bestOther, pbc.windows, otherPos, otherBat, endTime, model);


			
			if(pbc.getCurrentPlan().value(tempBestOwnGoals, endTime+1000)<pbc.getCurrentPlan().value(bestOwn, endTime+1000) && Plan.value(tempOtherOwnGoals, tempOtherBattery,tempOtherPos, endTime, model)<=minOtherValue){
				bestOwn=tempBestOwnGoals;
				bestOther = tempOtherOwnGoals;
			}
			

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

	
	/**
	 * This is called after each tick to see if the negotiation time has ended if there is negotiation started.
	 */
	public void checkNegotiation() {

		if(timeLastAction!= null && timeLastAction+delay<=model.getTime().getStartTime()){

			if(startedNegotiating){
				sendBestNegMessage();
			}
		}

	}
	
	/**
	 * Sets a new joint plan.
	 * @param newPlan
	 * @param other
	 */
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
	
	/**
	 * returns if a bid has been done for the negotiation
	 * 
	 */
	public boolean IsBidding() {
		return bidding;
	}
	
	/** 
	 * return if a negotiation has started 
	 * 
	 */
	public boolean IsNegotiating() {
		return startedNegotiating;
	}


}
