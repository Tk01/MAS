package Planning;

import java.util.ArrayList;

import world.InformationHandler;
import Messages.ReturnChargestationMessageContents;

import com.github.rinde.rinsim.core.model.comm.CommUser;
import com.github.rinde.rinsim.core.model.comm.Message;
import com.github.rinde.rinsim.geom.Point;


public class Negotiation {

	private PBC pbc;

	private long delay;

	private Long timeLastAction;

	private JPlan jplan;


	private boolean bidding = false;
	private boolean startedNegotiating = false;

	private ArrayList<CommUser> losers;
	private WorldModel model;


	private ArrayList<ChargeGoal> chargedList;

	
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
	 * start negotiating
	 */
	public void startNegotiation(){
		timeLastAction = model.getTime().getTime();
		startedNegotiating = true;
		//calculate the state the robot will be when it's finished negotiating
		Plan negotiationPlan = new Plan(pbc.getCurrentPlan().calculateGoals(timeLastAction+delay+1000),model);
		long timeEndNegotiation = timeLastAction+delay;
		Point pos = pbc.getCurrentPlan().calculatePosition(timeEndNegotiation);
		long battery = pbc.getCurrentPlan().calculateBattery(timeEndNegotiation);
		chargedList=new ArrayList<ChargeGoal>();
		losers =new ArrayList<CommUser>();
		jplan = new JPlan();
		jplan.setOwnPlan(new ArrayList<Goal>());
		jplan.setOtherPlan( negotiationPlan.getPlan());
		//calculate the minimum value a plan has to have before you accept it
		double minValue = pbc.getCurrentPlan().value(pbc.getCurrentPlan().getPlan(), timeEndNegotiation);
		//send the negotiation start message
		pbc.sendStartNegotiationMessage(pos, negotiationPlan.getPlan(),battery, timeLastAction+delay, minValue);



	}
	/**
	 * process a message
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
		pbc.setWindows(chargeContent.getFreeSlots());
		if(chargeContent.hasSucceeded() && timeLastAction+delay> model.getTime().getEndTime()){
			//check joint plan for the first unreserved reservation and set it as reserved and retry to send a new negotiationbid
			ArrayList<Goal> ownGoals = jplan.getOwnPlan();
			for(int i = 0; i<ownGoals.size();i++){
				if(ownGoals.get(i).type() == GoalTypes.Charging && !((ChargeGoal)ownGoals.get(i)).isReserved() ){
					((ChargeGoal)ownGoals.get(i)).setReserved(true);
					chargedList.add((ChargeGoal)ownGoals.get(i));
					pbc.sendNegotiationBidMessage(jplan, jplan.JPlanAgent);
					return;

				}
			}
			ArrayList<Goal> otherGoals = jplan.getOtherPlan();
			for(int i = 0; i<otherGoals.size();i++){

				if(otherGoals.get(i).type() == GoalTypes.Charging && !((ChargeGoal)otherGoals.get(i)).isReserved() ){
					((ChargeGoal)otherGoals.get(i)).setReserved(true);
					chargedList.add((ChargeGoal)ownGoals.get(i));
					pbc.sendNegotiationBidMessage(jplan, jplan.JPlanAgent);
					return;

				}
			}
		}
		else{
			//if the window you reserved for charging as taken end your bidding.
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
		
		if(pbc.getCurrentPlan().getPlan().size()> 3 && !(bidding || startedNegotiating)){
			long lastTime = messageContent.getEndTime();
			long currentTime = model.getTime().getTime();
			if(currentTime<lastTime){
				//calculate best plausible jointplan
				ArrayList<Goal> otherNegotiationPlan = messageContent.getPlan();
				Point otherPos = messageContent.getPosition();
				long otherBat = messageContent.getBattery();
				long endTime = messageContent.getEndTime();
				double minOtherValue = messageContent.getMinValue();
				JPlan jointPlan = bestJPlan(otherNegotiationPlan, otherPos, otherBat, endTime, minOtherValue);
				if(jointPlan!=null && jointPlan.getOtherPlan()!= null && jointPlan.getOwnPlan()!= null ){
					//if there exist a better plausible jointplan send a negotiationbidmessage
					bidding = true;
					timeLastAction = lastTime - delay; 
					jplan = jointPlan;
					jplan.setJPlanAgent(model.getThisRobot());
					pbc.sendNegotiationBidMessage(jointPlan, message.getSender());
					
				}
			}
			
		}
		model.messages().remove(message);
	}

	/**
	 * Process a bid from another drone
	 * Best bet is the new joint plan
	 * @param message: message with the bid of the other drone
	 */
	private void processNegotiationBid(Message message){
		NegotiationBidMessageContent messageContent = (NegotiationBidMessageContent) message.getContents();
		JPlan receivedJPlan = messageContent.getJointPlan();
		if(startedNegotiating && jplan != null && pbc.getCurrentPlan().value(receivedJPlan.getOtherPlan(),timeLastAction+delay)< pbc.getCurrentPlan().value(this.jplan.getOtherPlan(), timeLastAction+delay)){
			//if the received joint plan is better then the current best received joint plan, store the owner of the current plan and set the new best joint plan with the given joint plan
			if(jplan.JPlanAgent!=null)losers.add(jplan.JPlanAgent);
			jplan = receivedJPlan;
			
		}
		else{
			losers.add(receivedJPlan.JPlanAgent);
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
			//if you received a positive reply set your plan in the joint plan as the current plan.
			this.setNewJointPlan(jplan, false);
		}else{
			//remove the made reservations in the charging station
			for(Goal g:chargedList){
					pbc.sendCancelReservationMessage(g.getStartWindow(), g.getEndWindow());
			}
		}
		jplan =null;
		bidding = false;
		this.model.messages().remove(message);
	}

	/**
	 * When the negotiation ends a message is send to all drones who did a bid. An accept to the best bid and a reject to the other bids.
>>>>>>> origin/master
	 */
	public void sendBestNegMessage(){
			if( jplan.JPlanAgent!= null){
				//inform the agents who bid whether they won the bid or not. 
				
				this.setNewJointPlan(jplan,true);
				pbc.sendNegotiationReplyMessage(jplan.JPlanAgent);
			}
			for(CommUser l:losers){
				pbc.sendNegativeNegotiationReplyMessage(l);
			}
			losers =null;
			jplan = null;
			startedNegotiating = false;

		}




	
	/**
	 * remove the goals around charging from a plan
	 */
	private ChargeGoal removeCharge(ArrayList<Goal> plan){
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
		//drops have to be done by the robots who carry the package
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
	 * divides the goals in all goals between the two agents and select the best combination
	 */
	@SuppressWarnings("unchecked")
	private JPlan getBestPlan(ArrayList<Goal> allGoals,
			ArrayList<Goal> otherGoals,Point otherPos, long otherBat, ArrayList<Goal> ownGoals, Point ownPos, long ownBat,
			ArrayList<Goal> bestOwn, ArrayList<Goal> bestOther, double minOtherValue, ChargeGoal ownCharge, ChargeGoal otherCharge, long endTime, long tempOtherBattery, Point tempOtherPos) {
		if(allGoals.size() ==0){
			
			//If the goals in allGoals are divided, divide the chargeGoals and if the plan is better then the currently best division
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
			ArrayList<Goal> tempBestOwnGoals = Plan.GenerateBestPlan(ownGoals2,ownList , ownCharge, null, pbc.getWindows(), ownPos, ownBat, endTime+1000, model);
			ArrayList<Goal> tempOtherOwnGoals = Plan.GenerateBestPlan(otherGoals2, otherList, otherCharge, null, pbc.getWindows(), otherPos, otherBat, endTime, model);

			if(pbc.getCurrentPlan().value(tempBestOwnGoals, endTime+1000)<pbc.getCurrentPlan().value(bestOwn, endTime+1000) 
					&& Plan.value(tempOtherOwnGoals, tempOtherBattery,tempOtherPos, endTime, model)<=minOtherValue){
				bestOwn=tempBestOwnGoals;
				bestOther = tempOtherOwnGoals;
			}

		

			tempBestOwnGoals = Plan.GenerateBestPlan(ownGoals2, ownList, otherCharge, null, pbc.getWindows(), ownPos, ownBat, endTime+1000, model);
			tempOtherOwnGoals = Plan.GenerateBestPlan(otherGoals2, otherList, ownCharge, null, pbc.getWindows(), otherPos, otherBat, endTime, model);

			if(pbc.getCurrentPlan().value(tempBestOwnGoals, endTime+1000)<pbc.getCurrentPlan().value(bestOwn, endTime+1000) 
					&& Plan.value(tempOtherOwnGoals, tempOtherBattery,tempOtherPos, endTime, model)<=minOtherValue){
				bestOwn=tempBestOwnGoals;
				bestOther = tempOtherOwnGoals;
			}

			JPlan jplan = new JPlan();
			jplan.setOwnPlan(bestOwn);
			jplan.setOtherPlan(bestOther);
			return jplan;
		}else{
			//remove the first two goals from allgoals and see which agent can make best use of them.
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
	  * Checks if it is time to end the bidding and if it is end the bidding
	 */

	public void checkNegotiation() {
		if(timeLastAction!= null && timeLastAction+delay<=model.getTime().getStartTime()){

			if(startedNegotiating){
				sendBestNegMessage();
			}
		}
	}

	/**
	 * Set a new JointPlan as your current plan
	 */
	private void setNewJointPlan(JPlan newPlan,boolean other){
		
		if(pbc.getCurrentPlan() !=null){
			//this part is meant to correct errors made in the calculations of the plan.caulculateGoals()
			@SuppressWarnings("unchecked")
			ArrayList<Goal> testplan = (ArrayList<Goal>) pbc.getCurrentPlan().getPlan().clone();
			removeCharge(testplan);
			ArrayList<Goal> comb = new ArrayList<Goal>();
			comb.addAll(newPlan.getOwnPlan());
			comb.addAll(newPlan.getOtherPlan());
			if(!testplan.isEmpty() && !comb.contains(testplan.get(0))){
				newPlan.getOtherPlan().add(0,testplan.get(0));
			}
		}
		//see if reservations for charging have to be cancelled
		ChargeGoal lostChargeGoal1 = pbc.getCurrentPlan().lostChargeGoal(newPlan.getOwnPlan());
		ChargeGoal lostChargeGoal2 = pbc.getCurrentPlan().lostChargeGoal(newPlan.getOtherPlan());
		if(pbc.getGoal() != null && pbc.getGoal().type() == GoalTypes.Charging && !newPlan.getOwnPlan().contains(pbc.getGoal()) && !newPlan.getOtherPlan().contains(pbc.getGoal()) ){
			InformationHandler.getInformationHandler().setlostcharge();
		}
		if(model.isReserveChargingStation() && lostChargeGoal2!=null && lostChargeGoal1!=null){
			pbc.sendCancelReservationMessage(lostChargeGoal2.getStartWindow(), lostChargeGoal2.getEndWindow());
		}
		//set the correct Plan of the joint plan as the current plan
		if(other){
			pbc.forcefullSetNewPlan(newPlan.getOtherPlan());
		}
		else{
			pbc.forcefullSetNewPlan(newPlan.getOwnPlan());
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