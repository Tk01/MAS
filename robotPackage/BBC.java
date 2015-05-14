package robotPackage;

import java.util.ArrayList;

import com.github.rinde.rinsim.core.model.comm.CommDevice;
import com.github.rinde.rinsim.core.model.comm.Message;
import com.github.rinde.rinsim.geom.Point;

public class BBC {
	Goal goal;
	Boolean charging;
	boolean done;
	WorldModel model;
	CommDevice commDevice;

	Robot thisRobot;

	PBC pbc = new PBC();

	ArrayList<Message> messages;
	private WorldInterface worldInterface;
	
	

	public BBC(WorldInterface worldInterface, WorldModel model, Robot robot) {
		// TODO Auto-generated constructor stub
		thisRobot = robot;
		this.model = model;
		this.worldInterface =worldInterface;
	}

	public void msg(Message message) {
		messages.add(message);
		// TODO Auto-generated method stub

	}

	public void placeGoal(Goal goal){
		this.goal = goal;
	}
	public void run() {
		if(done) pbc.done(goal);
		else{
			if( (model.battery() < 0.25 && goal.type().equals("charging") && !charging) ) pbc.plan("charging");
			else{
				if( model.messages().size() !=0) pbc.plan();
			}
		}

		checkMessages();

		if( goal == null){
			worldInterface.waitMoment();
			return;
		}
		if(goal.coordinates().equals(model.coordinates())){
			model.moveTo(goal.coordinates());
			return;
		}
		if(goal.type().equals("pickup")){
			worldInterface.pickup();
			done =true;
			return;
		}
		if(goal.type().equals("drop")){
			worldInterface.drop();
			done = true;
			return;
		}
		if(goal.type().equals("charging") && model.chargeTaken()){
			worldInterface.waitMoment();
			return;
		}
		if(goal.type().equals("charging")){
			worldInterface.charge();
			if(model.battery() == 1) done =true;
			return;
		}
		/*
		if(goal.type().equals("doBid")){

			BidMessage bidmessage = new BidMessage(goal.getReceiver(), goal.getBid(), goal.getPackage());
			if(model.battery() == 1) done =true;
			return;
		}
		*/
	}



	//read the messages
	private void checkMessages(){
		for(int i = 0; i<messages.size();i++){
			Message message = messages.get(i);
			MessageContent content = (MessageContent) message.getContents();
			if(content.getType().equals("DeliverPackage")){

				sendDeliverBidMessage(message);


			}
			if(content.getType().equals("PreAssignment")){
				
				evaluatePreAssignment(message);
				
			
			}
			
			
		}
	}



	private void evaluatePreAssignment(Message message) {
		PreAssignmentMessageContent packMessage = (PreAssignmentMessageContent) message.getContents();
		boolean assigned = packMessage.getAssigned();
		if(!assigned){
			double betterBid = pbc.checkIfBetterBid(packMessage.getPackge(), packMessage.getBid());
			if(betterbid > -1){
				
			}
		}
		
		
		
	}

	//Send a message
	private void sendDeliverBidMessage(Message message){

		DeliverPackageMessageContent packMessage = (DeliverPackageMessageContent) message.getContents();
		
		double bid = pbc.doPreBid(packMessage.getPackageToDel(), message.getSender());
		PreBidMessageContent bidMessageContent;
		if(bid> 0){
			
		
			bidMessageContent = new PreBidMessageContent(thisRobot, bid, packMessage.getPackageToDel());
			
		}
		else{
			bidMessageContent = new PreBidMessageContent(thisRobot, -1, packMessage.getPackageToDel());
			

		}

		commDevice.send(bidMessageContent, message.getSender());


	}
	
	public WorldModel getWorldModel(){
		return model;
	}
}
