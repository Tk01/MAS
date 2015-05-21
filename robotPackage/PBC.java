package robotPackage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.jscience.geography.coordinates.Coordinates;

import com.github.rinde.rinsim.core.model.comm.CommUser;
import com.github.rinde.rinsim.core.model.comm.Message;
import com.github.rinde.rinsim.core.model.pdp.Depot;
import com.github.rinde.rinsim.util.TimeWindow;

import world.ChargingStation;
import world.Package;
import world.ReturnChargeContents;

public class PBC {




	Plan definitivebid = null;
	CommUser defSender;
	HashMap<Integer,Plan> prebids = new HashMap<Integer,Plan>();
	WorldModel worldModel;
	Plan currentplan;
	BBC bbc;

	CC cc;

	boolean ccOnHold = false;

	public PBC(BBC bbc){
		this.bbc=bbc;
		worldModel = bbc.getWorldModel();
		cc = new CC();
		currentplan = new Plan(new ArrayList<Goal>(), worldModel);
	}

	boolean chargingInPlan = true;


	public void done(Goal g){
		getCurrentPlan().remove(g);

		bbc.setGoal(getCurrentPlan().getNextgoal());

	}

	public Plan getCurrentPlan(){
		return currentplan;
	}

	public Plan getdefinitivebid(){
		return definitivebid;
	}





	public void readMessages(){
		


		ArrayList<Message> messages = worldModel.messages();
		cleanUp(messages);
		reserveMessages(messages);

		for(int i = 0; i<messages.size();i++){
			Message message = messages.get(i);
			MessageContent content = (MessageContent) message.getContents();
			if(content.getType().equals("DefAssignment")){

				defAssignment((DefAssignmentMessageContent)content);
				worldModel.messages().remove(message);

			}
		}


		preAssignment(messages);

		callForBids(messages);



	}

	private void cleanUp(ArrayList<Message> messages) {
		for(int i=0;i<messages.size();i++){
			if(((MessageContent) messages.get(i).getContents()).getType().equals("DeliverMessage") && ((DeliverPackageMessageContent)messages.get(i).getContents()).getEndTime() <= worldModel.getTime().getStartTime()){
				messages.remove(i);
				i--;
			}else{
				if(((MessageContent) messages.get(i).getContents()).getType().equals("PreAssignment") && ((PreAssignmentMessageContent)messages.get(i).getContents()).getEndTime() <= worldModel.getTime().getStartTime()){
					messages.remove(i);
					i--;
				}
			}
		}
		
	}

	private void reserveMessages(ArrayList<Message> messages){
		for(Message message:messages){

			MessageContent content = (MessageContent) message.getContents();
			if(content.getType().equals("ChargeMessage")){
				ReturnChargeContents chargeContent  = ((ReturnChargeContents) content);
				if(chargeContent.isReserved() && chargeContent.hasSucceeded()){
					for(int i =0; i<definitivebid.getPlan().size();i++){
						Goal goal = definitivebid.getPlan().get(i);
						String type = goal.type();
						if(type.equals("charging") && worldModel.isReserveChargingStation() && !((ChargeGoal)goal).isReserved()){
							((ChargeGoal)goal).setReserved(true);
							Plan plan = definitivebid;
							definitivebid= null;
							doDefBid(plan, defSender );
						}
					}
				}
				if(chargeContent.isReserved() && !chargeContent.hasSucceeded()){
					definitivebid= null;
				}


			}
		}
	}

	//The package has been def assigned to the agent so the definitive bid plan becomes the currentPlan
	private void defAssignment(DefAssignmentMessageContent content){
		if(content.assigned){
			currentplan=definitivebid;
			bbc.setGoal(currentplan.getNextgoal());
			cc.negotiationAbort();
			definitivebid = null;
			this.worldModel.messages().removeAll(this.worldModel.messages());

		}
		else{
			ArrayList <Goal> goals = definitivebid.getPlan();
			for(int i=0; i<goals.size();i++){

				if(goals.get(i).type.equals("charging")){
					bbc.deleteChargeReservation(goals.get(i).startWindow, goals.get(i).endWindow);
				}


			}
			definitivebid = null;
			ccOnHold = false;

		}

	}


	private void preAssignment(ArrayList <Message> messages){

		ArrayList deleteMessages = new ArrayList();
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
						deleteMessages.add(i);
					}
					else{
						double planValue = plan.value(plan.goals);
						if(planValue<bestPlanValue && bestPlanValue>-1){
							bestPlanValue = planValue;
							bestPlan = plan;
							sender = message.getSender();
							deleteMessages.add(i);

						}
						else if(bestPlanValue==-1){
							bestPlanValue = planValue;
							bestPlan = plan;
							sender = message.getSender();
							deleteMessages.add(i);
						}

					}
				}
			}

		}

		/*
		for(int i=0;i<deleteMessages.size();i++){
			messages.remove(deleteMessages.get(i));
		}
		*/
		if(bestPlan != null){
			doDefBid(bestPlan, sender);
		}
		

	}


	private void doDefBid(Plan plan, CommUser sender){
		if(definitivebid!= null)return;
		definitivebid = plan;
		defSender = sender;
		ArrayList<Goal> goals = plan.getPlan();
		Plan finalPlan = plan;
		for(int i =0; i<goals.size();i++){
			Goal goal = goals.get(i);
			String type = goal.type();
			if(type.equals("charging") && worldModel.isReserveChargingStation() && !((ChargeGoal)goal).isReserved()){
				bbc.sendReserveMessage(goal.startWindow, goal.endWindow);
				return;
			}
		}
		
		double oldValue = currentplan.value(currentplan.getPlan());
		double newValue = finalPlan.value(finalPlan.getPlan());
		double bid = newValue - oldValue;

		bbc.sendDefBidMessage(sender, bid);
		this.worldModel.deletePreAssign(sender);


	}

	private Plan reserveSlotCharging(Plan plan){
		Plan finalPlan= plan;
		//TODO
		return finalPlan;
	}



	private void callForBids(ArrayList<Message> messages){

		for(int i= 0;i<messages.size();i++){
			Message message = messages.get(i);
			MessageContent content = (MessageContent) message.getContents();
			if(content.getType().equals("DeliverMessage")){

				DeliverPackageMessageContent callForBidContent = (DeliverPackageMessageContent) content;
				int ID = callForBidContent.getContractID();
				Package pack = callForBidContent.getPackageToDel();
				Plan plan;
				if(definitivebid!=null){
					plan  = new Plan(definitivebid.getPlan(), worldModel);
				}
				else{
					plan  = new Plan(currentplan.getPlan(), worldModel);
				}
				Plan bidPlan = null;
				Goal pickupGoal = new Goal(pack.getStart(), "pickup", pack.getPickupTimeWindow());
				Goal dropGoal = new Goal(pack.getEnd(), "drop", pack.getDeliveryTimeWindow());

				if(worldModel.isReserveChargingStation()){
					
					bidPlan = plan.isPossiblePlan(pickupGoal,dropGoal);
				}
				else{
					bidPlan = plan.isPossiblePlan(pickupGoal,dropGoal);
				}
				if(bidPlan !=null && bidPlan.getPlan() !=null){
				double oldValue = currentplan.value(currentplan.getPlan());
				double newValue = bidPlan.value(bidPlan.getPlan());
				double bid = newValue - oldValue;
				bidPlan.setBidPackage(pack);
				this.prebids.put(ID, bidPlan);
				bbc.sendPreBidMessage(message.getSender(), bid, ID);
				}
			}

		}

	}

	public void sendNegotiationBidMessage(JPlan jointPlan, CommUser sender) {
		bbc.sendNegotiationBidMessage( jointPlan,  sender);

	}

	public void sendStartNegotiationMessage(Plan plan) {
		bbc.sendStartNegotiationMessage( plan);

	}

	public void sendConfirmationMessage(JPlan bestJPlan) {
		bbc.sendConfirmationMessage(bestJPlan);

	}

	public void sendNegotiationReplyMessage(CommUser jPlanAgent) {
		// TODO Auto-generated method stub
		
	}

	public void placeCharge() {
		bbc.setGoal( new ChargeGoal(worldModel.ChargingStation.getPosition().get(), "charging",new TimeWindow(0, Long.MAX_VALUE) ,false));
		
	}

	public void failPickUp() {
		currentplan.goals.remove(0);
	
		for(Goal goal:currentplan.goals){
			if(goal.type().equals("drop")){
				currentplan.goals.remove(goal);
				bbc.setGoal(getCurrentPlan().getNextgoal());
				return;
			}
		}
	}































}
