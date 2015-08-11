package worldInterface;

import java.util.ArrayList;

import world.InformationHandler;
import Messages.ChargeMessageContent;
import Messages.DefBidMessageContent;
import Messages.MessageContent;
import Messages.MessageTypes;
import Messages.NegotiationBidMessageContent;
import Messages.NegotiationReplyMessageContent;
import Messages.StartNegotiationMessageContent;
import WorldModel.ChargeGoal;
import WorldModel.Goal;
import WorldModel.GoalTypes;
import WorldModel.JPlan;
import WorldModel.WorldModel;

import com.github.rinde.rinsim.core.model.comm.CommDevice;
import com.github.rinde.rinsim.core.model.comm.CommDeviceBuilder;
import com.github.rinde.rinsim.core.model.comm.CommUser;
import com.github.rinde.rinsim.geom.Point;
import com.github.rinde.rinsim.util.TimeWindow;
import com.google.common.base.Optional;

public class Communication {
	
	Optional<CommDevice>  translator;
	WorldModel model;

	
	public Communication( WorldModel model){
		
		this.model=model;
		
	}
	
	
	public void getMessages(){
		model.addMessages(translator.get().getUnreadMessages());
	}
	
	
	public void sendMessage( MessageContent MessageContent) {
		if(MessageContent.getUser() ==null){
			this.translator.get().broadcast(MessageContent);
		}else{
			if(MessageContent.getType() == MessageTypes.NegotiationReplyMessage 
					&& ((NegotiationReplyMessageContent)MessageContent).isAccepted())
				InformationHandler.getInformationHandler().completedNegatiation();
			this.translator.get().send(MessageContent, MessageContent.getUser());
		}
	}
	
	
	/**
	 * Sends a definitive bid message received from the PBC and send to the worldinterface
	 * @param sender: the sender of the request (the package)
	 * @param bid: the bid of the bid message
	 */
	public void sendDefBidMessage(CommUser sender, double bid) {
		sendMessage(new DefBidMessageContent(sender, bid));

	}


	/**
	 * Send a reservation message to the charging station
	 * @param startWindow: the startwindow of the reservation
	 * @param endWindow: the endwindow of the reservation
	 */
	public void sendReserveMessage(ArrayList<TimeWindow> toBeDeleted,ArrayList<TimeWindow> toBePlaced) {
		sendMessage(new ChargeMessageContent(this.model.getChargingStation(), startWindow, endWindow, "reserve"));	
	}

	/**
	 * Send a bid for a negotiation
	 * @param jointPlan: the plans that are in the bid
	 * @param sender: the sender of the request
	 */
	public void sendNegotiationBidMessage(JPlan jointPlan, CommUser sender) {
		jointPlan.setJPlanAgent(model.getThisRobot());
		sendMessage(new NegotiationBidMessageContent(sender,jointPlan));

	}

	/**
	 * Set up a request to start a negotiation
	 *
	 */
	public void sendStartNegotiationMessage(Point pos,ArrayList<Goal> plan,long battery, long endTime, double minValue) {
		sendMessage(new StartNegotiationMessageContent(null,pos,plan,battery, endTime, minValue));

	}
	
	public void setCommDevice(CommDeviceBuilder builder) {
		builder.setMaxRange(20);

		translator = Optional.of(builder
				.setReliability(1)
				.build());	
	}
	
	 public void sendNegotiationBidMessage(final JPlan jointPlan, final CommUser sender) {
	        final ArrayList<Goal> ownGoals = (ArrayList<Goal>)jointPlan.getOwnPlan();
	        for (int i = 0; i < ownGoals.size(); ++i) {
	            if (ownGoals.get(i).type() == GoalTypes.Charging && !((ChargeGoal)ownGoals.get(i)).isReserved()) {
	                sendReserveMessage(((ChargeGoal)ownGoals.get(i)).getStartWindow(), ((ChargeGoal)ownGoals.get(i)).getEndWindow());
	                return;
	            }
	        }
	        final ArrayList<Goal> otherGoals = (ArrayList<Goal>)jointPlan.getOtherPlan();
	        for (int j = 0; j < otherGoals.size(); ++j) {
	            if (otherGoals.get(j).type() == GoalTypes.Charging && !((ChargeGoal)otherGoals.get(j)).isReserved()) {
	                sendReserveMessage(((ChargeGoal)otherGoals.get(j)).getStartWindow(), ((ChargeGoal)otherGoals.get(j)).getEndWindow());
	                return;
	            }
	        }
	        sendNegotiationBidMessage(jointPlan, sender);
	    }
	
}
