package Planning;

import com.github.rinde.rinsim.util.*;
import worldInterface.*;
import com.github.rinde.rinsim.core.model.comm.*;
import Messages.*;
import WorldModel.*;
import com.github.rinde.rinsim.geom.*;
import java.util.*;

public class ContractNet
{
    private long defTime;
    private Plan definitivebid;
    private CommUser defSender;
    private WorldModel worldModel;
    private boolean chargeGoal;
    private ArrayList<TimeWindow> windows;
    private Communication comm;
    
    public ContractNet(final WorldModel model, final Communication comm) {
        this.defTime = 0L;
        this.definitivebid = null;
        this.chargeGoal = false;
        this.worldModel = model;
        (this.windows = new ArrayList<TimeWindow>()).add(TimeWindow.ALWAYS);
        this.comm = comm;
    }
    
    private void reserveMessages(final ArrayList<Message> list) {
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
    
    public void defAssignment(final DefAssignmentMessageContent defAssignmentMessageContent) {
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
    
    private void doDefBid(final Plan plan, final CommUser commUser, final long n) {
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
    
    public void callForBids(final ArrayList<Message> list) {
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
    
    public void sendNegotiationBidMessage(final JPlan jointPlan, final CommUser sender) {
        final ArrayList<Goal> ownGoals = (ArrayList<Goal>)jointPlan.getOwnPlan();
        for (int i = 0; i < ownGoals.size(); ++i) {
            if (ownGoals.get(i).type() == GoalTypes.Charging && !((ChargeGoal)ownGoals.get(i)).isReserved()) {
                this.comm.sendReserveMessage(((ChargeGoal)ownGoals.get(i)).getStartWindow(), ((ChargeGoal)ownGoals.get(i)).getEndWindow());
                return;
            }
        }
        final ArrayList<Goal> otherGoals = (ArrayList<Goal>)jointPlan.getOtherPlan();
        for (int j = 0; j < otherGoals.size(); ++j) {
            if (otherGoals.get(j).type() == GoalTypes.Charging && !((ChargeGoal)otherGoals.get(j)).isReserved()) {
                this.comm.sendReserveMessage(((ChargeGoal)otherGoals.get(j)).getStartWindow(), ((ChargeGoal)otherGoals.get(j)).getEndWindow());
                return;
            }
        }
        this.comm.sendNegotiationBidMessage(jointPlan, sender);
    }
    
    public void sendStartNegotiationMessage(final Point pos, final ArrayList<Goal> goals, final long battery, final long endTime, final double minValue) {
        this.comm.sendStartNegotiationMessage(pos, (ArrayList)goals, battery, endTime, minValue);
    }
    
    public void placeCharge() {
        if (!this.worldModel.isReserveChargingStation()) {
            this.worldModel.setCurrentGoal((Goal)new ChargeGoal((Point)this.worldModel.getChargingStation().getPosition().get(), new TimeWindow(0L, Long.MAX_VALUE), false));
            return;
        }
        if (this.definitivebid == null) {
            for (final TimeWindow w : this.windows) {
                final long start = this.worldModel.getTime().getTime() + this.worldModel.calcTime(this.worldModel.coordinates(), (Point)this.worldModel.getChargingStation().getPosition().get()) + 2000L;
                if (w.isIn(start)) {
                    double batterydiff = this.worldModel.getMaxBattery() - 2000L - this.worldModel.battery() - this.worldModel.calcTime(this.worldModel.coordinates(), (Point)this.worldModel.getChargingStation().getPosition().get());
                    if (batterydiff / 5.0 == (long)(batterydiff / 5.0)) {
                        batterydiff = (long)(batterydiff / 5.0);
                    }
                    else {
                        batterydiff = (long)(batterydiff / 5.0) + 1L;
                    }
                    final long end = (long)Math.min(start + batterydiff, w.end);
                    final Goal goal = (Goal)new ChargeGoal((Point)this.worldModel.getChargingStation().getPosition().get(), new TimeWindow(start, end), false);
                    final ArrayList<Goal> list = new ArrayList<Goal>();
                    list.add(goal);
                    this.chargeGoal = true;
                    this.definitivebid = new Plan((ArrayList)list, this.worldModel);
                    this.comm.sendReserveMessage(goal.getStartWindow(), goal.getEndWindow());
                }
            }
        }
    }
    
    public void checkNegotiation() {
    	cc.checkNegotiation();
    }
    
    public void SetNewPlan(final Plan plan) {
    	
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
    
    public void forcefullSetNewPlan(final ArrayList<Goal> list) {
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