package Planning;

import java.util.ArrayList;

import world.InformationHandler;
import worldInterface.Communication;
import Messages.MessageContent;
import Messages.MessageTypes;
import Messages.NegotiationBidMessageContent;
import Messages.NegotiationReplyMessageContent;
import Messages.ReturnChargestationMessageContents;
import Messages.StartNegotiationMessageContent;
import WorldModel.Bid;
import WorldModel.ChargeGoal;
import WorldModel.Goal;
import WorldModel.GoalTypes;
import WorldModel.JPlan;
import WorldModel.Plan;
import WorldModel.WorldModel;

import com.github.rinde.rinsim.core.model.comm.CommUser;
import com.github.rinde.rinsim.core.model.comm.Message;
import com.github.rinde.rinsim.geom.Point;
import com.github.rinde.rinsim.util.TimeWindow;


public class Negotiation {
	
	private Communication communication;
	private long negExtend;
	//NIEUWE METHODE TODO
	public void processNegotiationReply(){
		for(int i =0;i< model.messages().size();i++){
			Message message = model.messages().get(i);
			MessageContent messageContent = (MessageContent) message.getContents();
			MessageTypes type = messageContent.getType();
			if(type == MessageTypes.NegotiationReplyMessage){
				if(((NegotiationReplyMessageContent )messageContent).isAccepted()){
					this.setNewJointPlan(jplan, false);
				}
				model.setNegotiationOngoing(false);
				jplan =null;
				timeLastAction =null;
				this.model.messages().remove(message);
				i--;
			}
		}
	}
	public void processNegotiationbid(){
		
		
		for(int i =0;i< model.messages().size();i++){
			Message message = model.messages().get(i);
			MessageContent messageContent = (MessageContent) message.getContents();
			MessageTypes type = messageContent.getType();
			if(type == MessageTypes.NegotiationBidMessage){
				NegotiationBidMessageContent messageContent1 = (NegotiationBidMessageContent) message.getContents();
				JPlan receivedJPlan = messageContent1.getJointPlan();
				if(jplan != null && model.getCurrentPlan().value(receivedJPlan.getOtherPlan(),timeLastAction+delay)< model.getCurrentPlan().value(this.jplan.getOtherPlan(), timeLastAction+delay)){
					//if the received joint plan is better then the current best received joint plan, store the owner of the current plan and set the new best joint plan with the given joint plan
					if(jplan.getJPlanAgent()!=null)losers.add(jplan.getJPlanAgent());
					jplan = receivedJPlan;
					
				}
				else{
					losers.add(receivedJPlan.getJPlanAgent());
				}
				this.model.messages().remove(message);
				i--;
			}			
		}
		


		}
	
		public Bid finishNegotiation(){
			if(timeLastAction!= null && timeLastAction+delay<=model.getTime().getStartTime()){
				if( jplan.getJPlanAgent()!= null){
					ArrayList<TimeWindow> toBeDeleted = new ArrayList<TimeWindow>();
					ArrayList<TimeWindow> toBePlaced = new ArrayList<TimeWindow>();
					if(model.isReserveChargingStation()){
					for(Goal g:jplan.getOwnPlan()){
						if(g.type() == GoalTypes.Charging){
							toBePlaced.add(new TimeWindow(g.getStartWindow(),g.getEndWindow()));
						}
					}
					for(Goal g:jplan.getOtherPlan()){
						if(g.type() == GoalTypes.Charging){
							toBePlaced.add(new TimeWindow(g.getStartWindow(),g.getEndWindow()));
						}
					}
					for(Goal g:jplan.getOwnPlan()){
						if(g.type() == GoalTypes.Charging){
							toBeDeleted.add(new TimeWindow(g.getStartWindow(),g.getEndWindow()));
						}
					}
					for(Goal g:jplan.ToBeDeleted()){

						toBeDeleted.add(new TimeWindow(g.getStartWindow(),g.getEndWindow()));
					}
					communication.sendReserveMessage(toBeDeleted, toBePlaced);
					}
					else{
						this.setNewJointPlan(jplan,true);
						communication.sendNegotiationReplyMessage(jplan.getJPlanAgent(),false);
						jplan =null;
						timeLastAction =null;
						model.setNegotiationOngoing(false);
					}
					
				}
				for(CommUser l:losers){
					communication.sendNegotiationReplyMessage(l,false);
				}
				timeLastAction=null;
				losers =null;
				jplan = null;
			}
			return null;
		}
		public void answerNegotiation(){
			for(int i =0;i< model.messages().size();i++){
				Message message = model.messages().get(i);
				MessageContent messageContent = (MessageContent) message.getContents();
				MessageTypes type = messageContent.getType();
				if(type == MessageTypes.StartNegotiationMessage){
					StartNegotiationMessageContent messageContent1 = (StartNegotiationMessageContent) message.getContents();
					
					if(model.getCurrentPlan().getPlan().size()> 3){
						long lastTime = messageContent1.getEndTime();
						long currentTime = model.getTime().getTime();
						if(currentTime<lastTime){
							//calculate best plausible jointplan
							ArrayList<Goal> otherNegotiationPlan = messageContent1.getPlan();
							Point otherPos = messageContent1.getPosition();
							long otherBat = messageContent1.getBattery();
							long endTime = messageContent1.getEndTime();
							double minOtherValue = messageContent1.getMinValue();
							JPlan jointPlan = bestJPlan(messageContent1.getWins(),otherNegotiationPlan, otherPos, otherBat, endTime, minOtherValue);
							if(jointPlan!=null && jointPlan.getOtherPlan()!= null && jointPlan.getOwnPlan()!= null ){
								//if there exist a better plausible jointplan send a negotiationbidmessage
								model.setNegotiationOngoing(true);
								timeLastAction = lastTime - delay; 
								jplan = jointPlan;
								jplan.setJPlanAgent(model.getThisRobot());
								communication.sendNegotiationBidMessage(jointPlan, message.getSender());
								
							}
						}
						
					}
					this.model.messages().remove(message);
					i--;
				}
			}
		}
		

		public void negotiationRequest(){
			timeLastAction = model.getTime().getTime();
			//calculate the state the robot will be when it's finished negotiating
			Plan negotiationPlan = new Plan(model.getCurrentPlan().calculateGoals(timeLastAction+delay+negExtend),model);
			long timeEndNegotiation = timeLastAction+delay;
			Point pos = model.getCurrentPlan().calculatePosition(timeEndNegotiation);
			long battery = model.getCurrentPlan().calculateBattery(timeEndNegotiation);
			
			losers =new ArrayList<CommUser>();
			jplan = new JPlan();
			jplan.setOwnPlan(new ArrayList<Goal>());
			jplan.setOtherPlan( negotiationPlan.getPlan());
			model.setNegotiationOngoing(true);
			//calculate the minimum value a plan has to have before you accept it
			double minValue = model.getCurrentPlan().value(model.getCurrentPlan().getPlan(), timeEndNegotiation);
			//send the negotiation start message
			communication.sendStartNegotiationMessage(pos,model.getWins(), negotiationPlan.getPlan(),battery, timeLastAction+delay, minValue);

		}
		public void reserveMessages(){
			for(int i =0;i< model.messages().size();i++){
				Message message = model.messages().get(i);
				MessageContent messageContent = (MessageContent) message.getContents();
				MessageTypes type = messageContent.getType();
				if(type == MessageTypes.ReturnChargestationMessage){
					if(((ReturnChargestationMessageContents) messageContent).hasSucceeded()){
						this.setNewJointPlan(jplan,true);
						communication.sendNegotiationReplyMessage(jplan.getJPlanAgent(),true);
					}else{
						communication.sendNegotiationReplyMessage(jplan.getJPlanAgent(),false);
					}
					jplan =null;
					timeLastAction =null;
					model.setNegotiationOngoing(false);
					communication.sendNegotiationReplyMessage(jplan.getJPlanAgent(),true);	
					this.model.messages().remove(message);
					i--;
				}
				
			}
		}


	private void setNewJointPlan(JPlan jplan2, boolean b) {
			if(b){
				model.setCurrentPlan(new Plan(jplan2.getOtherPlan(), model));
			}else{
				model.setCurrentPlan(new Plan(jplan2.getOwnPlan(), model));
			}
			
		}


	private long delay;

	private Long timeLastAction;

	private JPlan jplan;



	private ArrayList<CommUser> losers;
	private WorldModel model;





	
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
		Point ownPos = model.getCurrentPlan().calculatePosition(endtime+2000);
		long ownBat = model.getCurrentPlan().calculateBattery(endtime+2000);
		ArrayList<Goal> bestStartingGoals = model.getCurrentPlan().calculateGoals(endtime+2000);
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
	private JPlan bestJPlan(ArrayList<Bid> wins,
			ArrayList<Goal> otherNegotiationPlan, Point otherPos,
			long otherBat, long endTime, double minOtherValue) {
		Double winValue = (double) 0;
		JPlan best = null;
		for(Bid win:wins){
			Goal g1 = new Goal(win.getPackageToDeliver().getStart(), GoalTypes.Pickup, win.getPackageToDeliver().getPickupTimeWindow());
			Goal g2 = new Goal(win.getPackageToDeliver().getEnd(), GoalTypes.Drop, win.getPackageToDeliver().getDeliveryTimeWindow());
			@SuppressWarnings("unchecked")
			ArrayList<Goal> temp = (ArrayList<Goal>) otherNegotiationPlan.clone();
			temp.add(g1);
			temp.add(g2);
			
			JPlan temPlan = bestJPlan(temp,  otherPos, otherBat,  endTime,  minOtherValue);
			double tempVal = model.getCurrentPlan().value(temPlan.getOwnPlan(), endTime+negExtend);
			if(temPlan!=null && temPlan.getOtherPlan()!= null && temPlan.getOwnPlan()!= null && (best== null || winValue < tempVal )){
				best=temPlan;
				winValue = tempVal;
			}
		}
		return best;
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
			ArrayList<Goal> tempBestOwnGoals = Plan.GenerateBestPlan(ownGoals2,ownList , null, model.getWindows(), ownPos, ownBat, endTime+negExtend, model);
			ArrayList<Goal> tempOtherOwnGoals = Plan.GenerateBestPlan(otherGoals2, otherList, null, model.getWindows(), otherPos, otherBat, endTime, model);

			if(model.getCurrentPlan().value(tempBestOwnGoals, endTime+negExtend)<model.getCurrentPlan().value(bestOwn, endTime+negExtend) 
					&& Plan.value(tempOtherOwnGoals, tempOtherBattery,tempOtherPos, endTime, model)<=minOtherValue){
				bestOwn=tempBestOwnGoals;
				bestOther = tempOtherOwnGoals;
			}

		

			tempBestOwnGoals = Plan.GenerateBestPlan(ownGoals2, ownList, null, model.getWindows(), ownPos, ownBat, endTime+negExtend, model);
			tempOtherOwnGoals = Plan.GenerateBestPlan(otherGoals2, otherList, null, model.getWindows(), otherPos, otherBat, endTime, model);

			if(model.getCurrentPlan().value(tempBestOwnGoals, endTime+negExtend)<model.getCurrentPlan().value(bestOwn, endTime+negExtend) 
					&& Plan.value(tempOtherOwnGoals, tempOtherBattery,tempOtherPos, endTime, model)<=minOtherValue){
				bestOwn=tempBestOwnGoals;
				bestOther = tempOtherOwnGoals;
			}

			JPlan jplan = new JPlan();
			jplan.setOwnPlan(bestOwn);
			jplan.setOtherPlan(bestOther);
			ArrayList<ChargeGoal> toBeDeleted = new ArrayList<ChargeGoal>();
			for(Goal g : model.getCurrentPlan().getPlan()){
				if(g.type() == GoalTypes.Charging){
					toBeDeleted.add((ChargeGoal) g);
				}
			}
			jplan.addTobeDeleted(toBeDeleted);
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



}