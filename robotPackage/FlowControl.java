/**
 * 
 */
package robotPackage;

import java.util.ArrayList;

import world.ChargingStation;
import world.Robot;
import worldInterface.Actions;
import worldInterface.Communication;
import worldInterface.Perspective;
import Messages.DeliverPackageMessageContent;
import Messages.MessageContent;
import Messages.MessageTypes;
import Messages.StartNegotiationMessageContent;
import Planning.ContractNet;
import Planning.Negotiation;
import WorldModel.Bid;
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
	
	
	private boolean negotiationDuringCNET= true;
	
	
	
	public FlowControl(Robot robot, Point p, ChargingStation c, double s, long batterySize, long chargeRate, boolean reserveChargingStation, long delay){
		Optional<RoadModel> roadModel= Optional.absent();
		Optional<PDPModel> pdpModel= Optional.absent();
		model = new WorldModel(p,c,s, batterySize, chargeRate, reserveChargingStation,robot, roadModel, pdpModel);
		actions = new Actions(model);
		communication = new Communication(model);
		perspective = new Perspective(model);
		//bbc = new BBC(this,model,robot,delay);
		
		cNet = new ContractNet(model, communication);
		negotiation = new Negotiation();
		
		goalsToActions = new GoalsToActions(model, actions, communication);
		
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
	
	
	
	
	
	
	
	public WorldModel getModel() {
		return model;
	}

	/** handles the messages found in worldModel.messages()
	 */
	
	public void readMessages(){
		
		cleanUp(messages);
		// first check for reservationmessages
		
		//if negotiation is ongoing the reservation is done by the negotiation.
		if(model.isNegotiationOngoing()){
			negotiation.reserveMessages();
		}
		//if negotiation is not going on then the resevtaion messages are for CNET
		else{
			
			boolean reservationSuccess = cNet.reserveMessages();
			//If reservation for CNET is succesfull the currentplan is adapted with the new won package
			//If then the negotiation is after the CNET the negotiation is started.
			if(reservationSuccess && !negotiationDuringCNET){
				negotiation.startNegotiation();
				return;
				
			}
		}
		
		
		negotiation.processNegotiationReply();
		
		//If negotiation is ongoing bids for the negotiation can be expected
		if(model.isNegotiationOngoing()){
			//Bids are processed
			negotiation.processNegotiationBid();
			//Check is done if the negotiation is finished. If finished it should return the winning bid in case of negotiation during CNET
			//In case no negotiation was succesful it should still return the winning bid so if a bid id returned it is clear negotiation during CNET has happened and finished
			//If not finished yet it is returned null
			Bid negotiationBid = negotiation.finishNegotiation();
			//If a negotiationbid is not null and negotiation during CNET is ongoing, it means the negotiation has finished and the CNET can be finished
			if(negotiationBid != null && negotiationDuringCNET ) cNet.finishCNetAfterNegotiation(negotiationBid);
			
		}
		
		//Only when no bids and wins are available negotiations from other AGVs can be considered
		if(model.getWins().size()==0 && model.getBids().size()==0){
			negotiation.negotiationRequest();
		}
		
		//Package sends pickup requests
		cNet.packageRequests();
		
		//Package replys are processed. 
		cNet.packageWinLoss();
		
		
		
		// If all bids have a reply from the respective packages and a win is available and negotiation is done during CNET, the negotiation is started
		if(model.getWins().size()>0 && model.getBids().size()==0 && negotiationDuringCNET){
			negotiation.startNegotiation();
		}
		//if no negotiation during the CNET the CNET is ended when a win is available and no bids are left
		else if(model.getWins().size()>0 && model.getBids().size()==0){
			cNet.finishContractNet();
		}
	
		



	}
	
	/**
	 * remove DeliverMessages and StartNegotiationMessage whose deadlines have been met.
	 */
	private void cleanUp(ArrayList<Message> messages) {
		for(int i=0;i<messages.size();i++){
			if(((MessageContent) messages.get(i).getContents()).getType()== MessageTypes.DeliverMessage && ((DeliverPackageMessageContent)messages.get(i).getContents()).getEndTime() <= model.getTime().getEndTime()){
				messages.remove(i);
				i--;
			}else{
				if(((MessageContent) messages.get(i).getContents()).getType() == MessageTypes.StartNegotiationMessage && ((StartNegotiationMessageContent)messages.get(i).getContents()).getEndTime() <= model.getTime().getEndTime()){
					messages.remove(i);
					i--;
				}
			}
		}
	}

}
