/**
 * 
 */
package robotPackage;

import java.util.ArrayList;

import world.ChargingStation;
import worldInterface.Actions;
import worldInterface.Communication;
import worldInterface.Perspective;
import Messages.DeliverPackageMessageContent;
import Messages.MessageContent;
import Messages.MessageTypes;
import Messages.StartNegotiationMessageContent;
import Planning.ContractNet;
import Planning.Negotiation;
import WorldModel.Robot;
import WorldModel.WorldModel;

import com.github.rinde.rinsim.core.TimeLapse;
import com.github.rinde.rinsim.core.model.comm.CommDeviceBuilder;
import com.github.rinde.rinsim.core.model.comm.Message;
import com.github.rinde.rinsim.core.model.pdp.PDPModel;
import com.github.rinde.rinsim.core.model.road.RoadModel;
import com.github.rinde.rinsim.geom.Point;
import com.google.common.base.Optional;


public class FlowControl {
	
	
	private WorldModel model;
	
	private Actions actions;
	private Communication communication;
	private Perspective perspective;
	
	private ContractNet cNet;
	
	private Negotiation negotiation;
	
	private GoalsToActions goalsToActions;
	
	
	
	public FlowControl(Robot robot, Point p, ChargingStation c, double s, long batterySize, long chargeRate, boolean reserveChargingStation, long delay){
		Optional<RoadModel> roadModel= Optional.absent();
		Optional<PDPModel> pdpModel= Optional.absent();
		model = new WorldModel(p,c,s, batterySize, chargeRate, reserveChargingStation,robot, roadModel, pdpModel);
		actions = new Actions(model);
		communication = new Communication(model);
		perspective = new Perspective(model);
		//bbc = new BBC(this,model,robot,delay);
		
		cNet = new ContractNet(model, communication);
		negotiation = new Negotiation(delay, model, communication);
		
		goalsToActions = new GoalsToActions(model, actions);
		
	}
	
	public void setCommDevice(CommDeviceBuilder builder) {
		communication.setCommDevice(builder);
	}
	
	
	public void run(TimeLapse time){
		perspective.gatherInfo(time);
		
		communication.getMessages();
		
		readMessages();
		
		goalsToActions.goalToActions();
	}
	
	
	
	
	
	
	
	/** handles the messages found in worldModel.messages()
	 */
	
	public void readMessages(){
		ArrayList<Message> messages = model.messages();
		cleanUp(messages);
		reserveMessages(messages); //Where to handle the battery station messages?
		
		ArrayList <Message> packRequestMessages = new ArrayList <Message>();
		ArrayList <Message> packReplyMessages = new ArrayList <Message>();
		ArrayList <Message> negReqMessages = new ArrayList <Message>();
		ArrayList <Message> negBidMessages = new ArrayList <Message>();
		ArrayList <Message> negReplyMessages = new ArrayList <Message>();
		ArrayList <Message> chargeReplyMessages = new ArrayList <Message>();

		for(int i = 0; i<messages.size();i++){
			Message message = messages.get(i);
			MessageContent content = (MessageContent) message.getContents();
			if(content.getType() == MessageTypes.DeliverMessage){
				
				packRequestMessages.add(message);
			}
			if(content.getType() == MessageTypes.DefAssignmentMessage ){
				packReplyMessages.add(message);
			}
			if(content.getType() == MessageTypes.StartNegotiationMessage ){
				negReqMessages.add(message);
			}
			if(content.getType() == MessageTypes.NegotiationBidMessage){
				negBidMessages.add(message);
			}
			if(content.getType() == MessageTypes.NegotiationReplyMessage){
				negReplyMessages.add(message);
			}
			if(content.getType() == MessageTypes.ReturnChargestationMessage){
				chargeReplyMessages.add(message);
			}
			
			
		}
		
		negotiation.processNegotiationBid(negBidMessages);
		
		negotiation.checkNegotiation();
		
		negotiation.processNegotiationReply(messages);
		
		cNet.callForBids(packRequestMessages);
		
		boolean startNeg = cNet.defAssignment(packReplyMessages);
		if(startNeg){
			negotiation.startNegotiation();
		}
		
		negotiation.ProcessStartNegotiation(negReqMessages);
		



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

}
