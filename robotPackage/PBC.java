package robotPackage;

import java.util.ArrayList;

import com.github.rinde.rinsim.core.model.comm.CommUser;
import com.github.rinde.rinsim.core.model.comm.Message;
import com.github.rinde.rinsim.core.model.road.RoadUnits;
import com.github.rinde.rinsim.geom.Point;
import com.github.rinde.rinsim.util.TimeWindow;

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

	boolean chargingInPlan = true;
	private boolean chargeGoal=false;
	ArrayList<TimeWindow> windows;

	public void done(Goal g){
		getCurrentPlan().remove(g);
		removeUnattainablePackages(getCurrentPlan());
		bbc.setGoal(getCurrentPlan().getNextgoal());

	}

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


		//preAssignment(messages);

		if(definitivebid == null)
			callForBids(messages);



	}

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

	private void reserveMessages(ArrayList<Message> messages){
		for(Message message:messages){

			MessageContent content = (MessageContent) message.getContents();
			if(content.getType() == MessageTypes.ReturnChargestationMessage){
				ReturnChargestationMessageContents chargeContent  = ((ReturnChargestationMessageContents) content);
				if(cc.IsBidding()){
					cc.chargeMessage(chargeContent);
					worldModel.messages().remove(message);
					return;
				}
				if(this.chargeGoal){
					if( chargeContent.hasSucceeded()){
						((ChargeGoal)this.definitivebid.getPlan().get(0)).setReserved(true);
						this.currentplan = this.definitivebid;
						bbc.setGoal(this.currentplan.getNextgoal());
					}
					this.windows = chargeContent.getFreeSlots();
					this.chargeGoal=false;
					definitivebid= null;
					messages.remove(message);
					return;	
				}
				if(chargeContent.isReserved() && chargeContent.hasSucceeded()){
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
					definitivebid= null;
					messages.remove(message);
					this.windows = chargeContent.getFreeSlots();
					return;
				}
				this.windows = chargeContent.getFreeSlots();
			}
		}
	}
	//The package has been def assigned to the agent so the definitive bid plan becomes the currentPlan
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

	/*
	private void preAssignment(ArrayList <Message> messages){


		Plan bestPlan = null;
		double bestPlanValue=-1;
		CommUser sender=null;
		for(int i= 0;i<messages.size();i++){
			Message message = messages.get(i);
			MessageContent content = (MessageContent) message.getContents();
			if(content.getType().equals("PreAssignment")){

				PreAssignmentMessageContent preAssignContent = (PreAssignmentMessageContent) content;
				int ID = preAssignContent.getContractID();

				if(prebids.get(ID) !=null){
					Plan plan = prebids.get(ID);
					long timeLastAction =plan.getBidPackage().getTimeLastAction();
					long delay  = plan.getBidPackage().getDelay();
					long lastTime = timeLastAction+delay;
					long currentTime = worldModel.getTime().getTime();
					if(currentTime>lastTime){

					}
					else{
						double planValue = plan.value(plan.goals);

						//bestPlanValue = preAssignContent.getBid();

						if(planValue<bestPlanValue && bestPlanValue>-1){
							bestPlanValue = planValue;
							bestPlan = plan;
							sender = message.getSender();


						}
						else if(bestPlanValue==-1){
							bestPlanValue = planValue;
							bestPlan = plan;
							sender = message.getSender();

						}

					}
				}
			}

		}


		for(int i=0;i<deleteMessages.size();i++){
			messages.remove(deleteMessages.get(i));
		}

		if(bestPlan != null){
			doDefBid(bestPlan, sender);
		}


	}
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
				bbc.sendReserveMessage(goal.getStartWindow(), goal.getEndWindow());
				return;
			}
		}

		double oldValue = currentplan.value(currentplan.getPlan(), bidreturntime);
		double newValue = finalPlan.value(finalPlan.getPlan(), bidreturntime);
		double bid = newValue - oldValue;

		bbc.sendDefBidMessage(sender, bid);


	}







	private void callForBids(ArrayList<Message> messages){
		if(!cc.IsBidding() && !cc.IsNegotiating()){
			Plan bestPlan = null;
			CommUser sender = null;
			long time =-1;
			for(int i= 0;i<messages.size();i++){
				Message message = messages.get(i);
				MessageContent content = (MessageContent) message.getContents();
				if(content.getType() == MessageTypes.DeliverMessage){

					DeliverPackageMessageContent callForBidContent = (DeliverPackageMessageContent) content;
					Package pack = callForBidContent.getPackageToDel();
					Plan plan;
					if(definitivebid!=null){
						plan  = new Plan(definitivebid.getPlan(), worldModel);
					}
					else{
						plan  = new Plan(currentplan.getPlan(), worldModel);
					}
					Plan bidPlan = null;
					Goal pickupGoal = new Goal(pack.getStart(), GoalTypes.Pickup, pack.getPickupTimeWindow());
					Goal dropGoal = new Goal(pack.getEnd(), GoalTypes.Drop, pack.getDeliveryTimeWindow());

					if(worldModel.isReserveChargingStation()){

						bidPlan = plan.isPossiblePlan(pickupGoal,dropGoal,windows,callForBidContent.getEndTime()+1000);
					}
					else{
						bidPlan = plan.isPossiblePlan(pickupGoal,dropGoal,windows,callForBidContent.getEndTime()+1000);
					}
					if(bidPlan !=null && bidPlan.getPlan() !=null){
						//double oldValue = currentplan.value(currentplan.getPlan());
						//double newValue = bidPlan.value(bidPlan.getPlan());
						if(bestPlan == null){
							bestPlan = bidPlan;
							sender = message.getSender();

						}
						else if(bestPlan.value(bestPlan.getPlan(),callForBidContent.getEndTime()+1000)<bidPlan.value(bidPlan.getPlan(),callForBidContent.getEndTime()+1000)){
							bestPlan = bidPlan;
							sender = message.getSender();
							time =callForBidContent.getEndTime()+1000;
						}


						//this.prebids.put(ID, bidPlan);

					}
				}

			}
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

	public void placeCharge() {
		if(!worldModel.isReserveChargingStation()){
			bbc.setGoal( new ChargeGoal(worldModel.getChargingStation().getPosition().get(),new TimeWindow(0, Long.MAX_VALUE) ,false));
			return;
		}
		if(definitivebid == null){ 
			for( TimeWindow w: windows){
				RoadUnits r = worldModel.getRoadUnits();
				long start = (long) (worldModel.getTime().getTime()+ r.toExTime(r.toInDist(Point.distance(worldModel.coordinates(),new Point(5,5)))/r.toInSpeed(worldModel.getSpeed()),worldModel.getTime().getTimeUnit()))+2000;
				if(w.isIn(start)){

					double batterydiff = worldModel.getMaxBattery()-2000-worldModel.battery()-r.toExTime(r.toInDist(Point.distance(worldModel.coordinates(),new Point(5,5)))/r.toInSpeed(worldModel.getSpeed()),worldModel.getTime().getTimeUnit()) ;
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

	public void failPickUp() {

		currentplan.getPlan().remove(0);

		for(Goal goal:currentplan.getPlan()){
			if(goal.type() == GoalTypes.Drop){
				currentplan.getPlan().remove(goal);
				bbc.setGoal(getCurrentPlan().getNextgoal());
				return;
			}
		}

	}

	public void checkNegotiation() {
		cc.checkNegotiation();


	}
	public void sendNegativeNegotiationReplyMessage(CommUser l) {
		bbc.sendNegativeNegotiationReplyMessage(l);

	}
	public void SetNewPlan(Plan newPlan){
		ChargeGoal lostChargeGoal = currentplan.lostChargeGoal(newPlan.getPlan());

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

		//cc.negotiationAbort();
		
		if(worldModel.isReserveChargingStation() && lostChargeGoal != null) bbc.sendCancelReservationMessage(lostChargeGoal.getStartWindow(),lostChargeGoal.getEndWindow());
	}
	/*
	public void returnNegPlan(Plan negotiatedCCPlan) {
		if(negotiatedCCPlan ==null && negotiating){
			doDefBid(negotiationPlan, messageForNegotiation.getSender());
		}
		else if(negotiatedCCPlan !=null && negotiating){
			if(negotiatedCCPlan.value(negotiatedCCPlan.goals)<negotiationPlan.value(negotiationPlan.goals)){
				doDefBid(negotiatedCCPlan, messageForNegotiation.getSender());
			}
			else{
				doDefBid(negotiationPlan, messageForNegotiation.getSender());
			}
		}

	}
	 */

	public void forcefullSetNewPlan(ArrayList<Goal> otherPlan) {
		this.currentplan.setPlan(otherPlan);
		removeUnattainablePackages(getCurrentPlan());
		bbc.setGoal(currentplan.getNextgoal());
		
	}

	public void sendCancelReservationMessage(long startWindow, long endWindow) {
		bbc.sendCancelReservationMessage(startWindow, endWindow);		
	}

































}
