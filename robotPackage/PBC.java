package robotPackage;

import java.util.ArrayList;

import com.github.rinde.rinsim.core.model.comm.CommUser;
import com.github.rinde.rinsim.core.model.comm.Message;
import com.github.rinde.rinsim.geom.Point;
import com.github.rinde.rinsim.util.TimeWindow;

import world.InformationHandler;
import world.Package;
import world.ReturnChargestationMessageContents;

public class PBC {




	private long defTime = 0;
	private Plan definitivebid = null;
	private CommUser defSender;
	private WorldModel worldModel;
	private Plan currentplan;
	private BBC bbc;
	private CC cc;

	public PBC(BBC bbc, long delay){
		this.bbc=bbc;
		worldModel = bbc.getWorldModel();
		cc = new CC(this,delay,worldModel);
		currentplan = new Plan(new ArrayList<Goal>(), worldModel);
		windows = new ArrayList<TimeWindow>();
		windows.add(TimeWindow.ALWAYS);
	}

	private boolean chargeGoal=false;
	private ArrayList<TimeWindow> windows;
	/**
	 * remove the goal that is done from the current plan and set the current goal in the bbc
	 */
	public void done(Goal g){
		getCurrentPlan().remove(g);
		removeUnattainablePackages(getCurrentPlan());
		bbc.setGoal(getCurrentPlan().getNextgoal());

	}
	/**
	 * remove the pickup goals and drop goals that can't be reached in time
	 */
	private void removeUnattainablePackages(Plan plan) {
		while(plan != null && plan.getNextgoal() !=null && plan.getNextgoal().type() == GoalTypes.Pickup && plan.getNextgoal().getEndWindow() < worldModel.calcTime(worldModel.coordinates(), plan.getNextgoal().coordinates())+ worldModel.getTime().getTime()){
			plan.remove(plan.getNextgoal());
			for(Goal g:plan.getPlan()){
				if(g.type() == GoalTypes.Drop){
					plan.remove(g);
					break;
				}
			}
		}

	}

	public Plan getCurrentPlan(){
		return currentplan;
	}
	/**
	 * handles the messages found in worldModel.messages()
	 */
	public void readMessages(){
		ArrayList<Message> messages = worldModel.messages();
		cleanUp(messages);
		reserveMessages(messages);

		for(int i = 0; i<messages.size();i++){
			Message message = messages.get(i);
			MessageContent content = (MessageContent) message.getContents();
			if(content.getType() == MessageTypes.NegotiationReplyMessage || content.getType() == MessageTypes.NegotiationBidMessage){
				cc.handleMessage(message);
				i--;
			}
		}
		for(int i = 0; i<messages.size();i++){
			Message message = messages.get(i);
			MessageContent content = (MessageContent) message.getContents();
			if(content.getType() == MessageTypes.DefAssignmentMessage){

				defAssignment((DefAssignmentMessageContent)content);
				worldModel.messages().remove(message);

			}
			if(content.getType() == MessageTypes.StartNegotiationMessage ){

				cc.handleMessage(message);

			}
		}

		if(definitivebid == null)
			callForBids(messages);



	}
	/**
	 * remove DeliverMessages and StartNegotiationMessage whose deadlines have been met.
	 */
	private void cleanUp(ArrayList<Message> messages) {
		for(int i=0;i<messages.size();i++){
			if(((MessageContent) messages.get(i).getContents()).getType()== MessageTypes.DeliverMessage && ((DeliverPackageMessageContent)messages.get(i).getContents()).getEndTime() <= worldModel.getTime().getEndTime()){
				messages.remove(i);
				i--;
			}else{
				if(((MessageContent) messages.get(i).getContents()).getType() == MessageTypes.StartNegotiationMessage && ((StartNegotiationMessageContent)messages.get(i).getContents()).getEndTime() <= worldModel.getTime().getEndTime()){
					messages.remove(i);
					i--;
				}
			}
		}
	}
	/**
	 * Checks messages for the response of charging station on a reservation message and handle them
	 */
	private void reserveMessages(ArrayList<Message> messages){
		for(Message message:messages){
			MessageContent content = (MessageContent) message.getContents();
			if(content.getType() == MessageTypes.ReturnChargestationMessage){
				ReturnChargestationMessageContents chargeContent  = ((ReturnChargestationMessageContents) content);
				//if cc is bidding let cc handle it
				if(cc.IsBidding()){
					cc.chargeMessage(chargeContent);
					worldModel.messages().remove(message);
					return;
				}

				if(this.chargeGoal){
					if( chargeContent.hasSucceeded()){
						// if this message is a response from a message created in placeCharge() set definitive bid as current plan
						((ChargeGoal)this.definitivebid.getPlan().get(0)).setReserved(true);
						this.SetNewPlan(definitivebid);
					}
					this.windows = chargeContent.getFreeSlots();
					this.chargeGoal=false;
					definitivebid= null;
					messages.remove(message);
					return;	
				}
				if(chargeContent.isReserved() && chargeContent.hasSucceeded()){
					// set the first ChargeGoal as reserved and retry to place a definitive bid
					for(int i =0; i<definitivebid.getPlan().size();i++){
						Goal goal = definitivebid.getPlan().get(i);
						GoalTypes type = goal.type();
						if(type == GoalTypes.Charging && worldModel.isReserveChargingStation() && !((ChargeGoal)goal).isReserved()){
							((ChargeGoal)goal).setReserved(true);
							Plan plan = definitivebid;
							definitivebid= null;
							doDefBid(plan, defSender,defTime );
							messages.remove(message);
							this.windows = chargeContent.getFreeSlots();
							return;
						}
					}
				}
				if(chargeContent.isReserved() && !chargeContent.hasSucceeded()){
					//if the reservation has failed, set the definitve bid as null
					definitivebid= null;
					messages.remove(message);
					this.windows = chargeContent.getFreeSlots();
					return;
				}
				this.windows = chargeContent.getFreeSlots();
			}
		}
	}
	/**
	 * Handle a DefAssignment message
	 */
	private void defAssignment(DefAssignmentMessageContent content){
		if(content.assigned){
			this.SetNewPlan(definitivebid);
			definitivebid = null;
			//Call CC to start negotiation
			if(currentplan.getPlan().size()>3){
				cc.startNegotiation();
			}

		}
		else{
			ArrayList <Goal> goals = definitivebid.getPlan();
			for(int i=0; i<goals.size();i++){
				if(goals.get(i).type() == GoalTypes.Charging){
					if(!this.currentplan.getPlan().contains(goals.get(i)))bbc.deleteChargeReservation(goals.get(i).getStartWindow(), goals.get(i).getEndWindow());
				}
			}
			definitivebid = null;
		}
	}


	/**
	 * check if a DefBidMessage can be send and sent it if possible
	 */
	private void doDefBid(Plan plan, CommUser sender, long bidreturntime){
		if(definitivebid!= null || cc.IsBidding() || cc.IsNegotiating())return;
		definitivebid = plan;
		defTime=bidreturntime;
		defSender = sender;
		ArrayList<Goal> goals = plan.getPlan();
		Plan finalPlan = plan;
		for(int i =0; i<goals.size();i++){
			Goal goal = goals.get(i);
			GoalTypes type = goal.type();
			if(type == GoalTypes.Charging && worldModel.isReserveChargingStation() && !((ChargeGoal)goal).isReserved()){
				//if Chargoal have to be reserved send a reservation message first
				bbc.sendReserveMessage(goal.getStartWindow(), goal.getEndWindow());
				return;
			}
		}

		double oldValue = currentplan.value(currentplan.getPlan(), bidreturntime);
		double newValue = finalPlan.value(finalPlan.getPlan(), bidreturntime);
		double bid = newValue - oldValue;

		bbc.sendDefBidMessage(sender, bid);


	}
	/**
	 *  Handles all the DeliverMessages in messages  
	 */
	private void callForBids(ArrayList<Message> messages){
		if(!cc.IsBidding() && !cc.IsNegotiating()){
			Plan bestPlan = null;
			CommUser sender = null;
			long time =-1;
			//look up for the best possible goals to be added to the plan
			for(int i= 0;i<messages.size();i++){
				Message message = messages.get(i);
				MessageContent content = (MessageContent) message.getContents();
				if(content.getType() == MessageTypes.DeliverMessage){

					DeliverPackageMessageContent callForBidContent = (DeliverPackageMessageContent) content;
					Package pack = callForBidContent.getPackageToDel();
					Plan bidPlan = null;
					Goal pickupGoal = new Goal(pack.getStart(), GoalTypes.Pickup, pack.getPickupTimeWindow());
					Goal dropGoal = new Goal(pack.getEnd(), GoalTypes.Drop, pack.getDeliveryTimeWindow());
					bidPlan = this.currentplan.isPossiblePlan(pickupGoal,dropGoal,windows,callForBidContent.getEndTime()+1000);
					if(bidPlan !=null && bidPlan.getPlan() !=null){
						if(bestPlan == null){
							bestPlan = bidPlan;
							sender = message.getSender();

						}
						else if(bestPlan.value(bestPlan.getPlan(),callForBidContent.getEndTime()+1000)<bidPlan.value(bidPlan.getPlan(),callForBidContent.getEndTime()+1000)){
							bestPlan = bidPlan;
							sender = message.getSender();
							time =callForBidContent.getEndTime()+1000;
						}
					}
				}

			}
			//try to definitive bid based the best plan
			if(bestPlan!=null){
				doDefBid(bestPlan, sender,time);
			}
		}


	}


	public void sendNegotiationBidMessage(JPlan jointPlan, CommUser sender) {
	
		ArrayList<Goal> ownGoals = jointPlan.getOwnPlan();
		for(int i = 0; i<ownGoals.size();i++){
			if(ownGoals.get(i).type() == GoalTypes.Charging && !((ChargeGoal)ownGoals.get(i)).isReserved() ){
				bbc.sendReserveMessage(((ChargeGoal)ownGoals.get(i)).getStartWindow(), ((ChargeGoal)ownGoals.get(i)).getEndWindow());
				return;
			}
		}
		ArrayList<Goal> otherGoals = jointPlan.getOtherPlan();
		for(int i = 0; i<otherGoals.size();i++){
			if(otherGoals.get(i).type() == GoalTypes.Charging && !((ChargeGoal)otherGoals.get(i)).isReserved() ){
				bbc.sendReserveMessage(((ChargeGoal)otherGoals.get(i)).getStartWindow(), ((ChargeGoal)otherGoals.get(i)).getEndWindow());
				return;
			}
		}
		bbc.sendNegotiationBidMessage( jointPlan,  sender);
	}
	public void sendStartNegotiationMessage(Point pos, ArrayList<Goal> goals, long battery, long endTime, double minValue) {
		bbc.sendStartNegotiationMessage( pos, goals, battery, endTime, minValue);

	}



	public void sendNegotiationReplyMessage(CommUser jPlanAgent) {
		bbc.sendNegotiationReplyMessage(jPlanAgent);

	}
	/**
	 * try to place charging in the current plan
	 */
	public void placeCharge() {
		if(!worldModel.isReserveChargingStation()){
			bbc.setGoal( new ChargeGoal(worldModel.getChargingStation().getPosition().get(),new TimeWindow(0, Long.MAX_VALUE) ,false));
			return;
		}
		if(definitivebid == null){ 
			//find the first fitting slot for charging
			for( TimeWindow w: windows){
				
				long start = worldModel.getTime().getTime()+worldModel.calcTime(worldModel.coordinates(), worldModel.getChargingStation().getPosition().get())+2000;
				if(w.isIn(start)){

					double batterydiff = worldModel.getMaxBattery()-2000-worldModel.battery()-worldModel.calcTime(worldModel.coordinates(), worldModel.getChargingStation().getPosition().get());
					if(batterydiff/5==(long) (batterydiff/5)){
						batterydiff=(long) (batterydiff/5);
					}else{
						batterydiff=(long) (batterydiff/5)+1;
					}
					long end = (long) Math.min((start+batterydiff), w.end);
					Goal goal = new ChargeGoal(worldModel.getChargingStation().getPosition().get(),new TimeWindow(start, end), false);
					ArrayList<Goal> list = new ArrayList<Goal>();
					list.add(goal);
					this.chargeGoal =true;
					definitivebid = new Plan(list, worldModel);
					bbc.sendReserveMessage(goal.getStartWindow(), goal.getEndWindow());
					return;
				}
			}
		}

	}


	public void checkNegotiation() {
		cc.checkNegotiation();


	}
	public void sendNegativeNegotiationReplyMessage(CommUser l) {
		bbc.sendNegativeNegotiationReplyMessage(l);

	}
	/**
	 * set newPlan as the current plan with s
	 */
	public void SetNewPlan(Plan newPlan){
		ChargeGoal lostChargeGoal = currentplan.lostChargeGoal(newPlan.getPlan());
		
		if( bbc.getGoal() != null
				&& bbc.getGoal().type() == GoalTypes.Charging 
				&& !newPlan.getPlan().contains(bbc.getGoal())){
			InformationHandler.getInformationHandler().setlostcharge();
		}
		//correct the error mad in Plan.calculateGoals()
		if(this.currentplan !=null){
			@SuppressWarnings("unchecked")
			ArrayList<Goal> testplan = (ArrayList<Goal>) this.currentplan.getPlan().clone();
			testplan.remove(lostChargeGoal);
			if(testplan.size()>0 && !newPlan.getPlan().contains(testplan.get(0))){
				newPlan.getPlan().add(0,testplan.get(0));
			}
		}

		currentplan=newPlan;
		removeUnattainablePackages(getCurrentPlan());
		bbc.setGoal(currentplan.getNextgoal());
		// cancel reservations in chargingstones that aren't in the new plan anymore
		if(worldModel.isReserveChargingStation() && lostChargeGoal != null) bbc.sendCancelReservationMessage(lostChargeGoal.getStartWindow(),lostChargeGoal.getEndWindow());
	}
	/**
	 * set newPlan as the current plan without any intervention
	 */
	public void forcefullSetNewPlan(ArrayList<Goal> otherPlan) {
		this.currentplan.setPlan(otherPlan);
		removeUnattainablePackages(getCurrentPlan());
		bbc.setGoal(currentplan.getNextgoal());

	}

	public void sendCancelReservationMessage(long startWindow, long endWindow) {
		bbc.sendCancelReservationMessage(startWindow, endWindow);		
	}

	public Goal getGoal() {
		return bbc.getGoal();
	}

	public ArrayList<TimeWindow> getWindows() {
		return windows;
	}

	public void setWindows(ArrayList<TimeWindow> freeSlots) {
		this.windows =freeSlots;

	}

































}
