package robotPackage;

import java.util.ArrayList;

import com.github.rinde.rinsim.core.model.comm.CommDevice;
import com.github.rinde.rinsim.core.model.comm.CommUser;
import com.github.rinde.rinsim.core.model.comm.Message;
import com.github.rinde.rinsim.geom.Point;

public class BBC {
	Goal goal;
	Boolean charging;
	boolean done;
	WorldModel model;
	CommDevice commDevice;

	Robot thisRobot;

	PBC pbc ;

	ArrayList<Message> messages = new ArrayList<Message>();
	private WorldInterface worldInterface;



	public BBC(WorldInterface worldInterface, WorldModel model, Robot robot) {
		thisRobot = robot;
		this.model = model;
		this.worldInterface =worldInterface;
		pbc= new PBC(this);
	}

	public void msg(Message message) {
		messages.add(message);

	}

	public void placeGoal(Goal goal){
		this.goal = goal;
	}
	public void run() {
		if(done) pbc.done(goal);
		if( model.messages().size() !=0) pbc.readMessages();


		checkMessages();

		if( goal == null){
			worldInterface.waitMoment();
			return;
		}
		if(!goal.coordinates().equals(model.coordinates())){
			worldInterface.MoveTo(goal.coordinates());
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
		// TODO Auto-generated method stub
		
	}

private void sendDeliverBidMessage(Message message) {
		// TODO Auto-generated method stub
		
	}

public WorldModel getWorldModel(){
	return model;
}

public void setGoal(Goal nextgoal) {
	this.goal =nextgoal;	
}

public void sendDefBidMessage(CommUser sender, double bid) {
	this.worldInterface.sendMessage(new DefBidMessageContent(sender, bid));
	

}

public void sendPreBidMessage(CommUser sender, double bid, int iD) {
	this.worldInterface.sendMessage(new PreBidMessageContent(sender, bid, iD));
	
}

public void deleteChargeReservation(long startWindow, long endWindow) {
	this.worldInterface.sendMessage(new ChargeMessageContent(this.model.ChargingStation, startWindow, startWindow, "delete"));
	
}

public void sendReserveMessage(long startWindow, long endWindow) {
	this.worldInterface.sendMessage(new ChargeMessageContent(this.model.ChargingStation, startWindow, startWindow, "reserve"));	
}

public void sendNegotiationBidMessage(JPlan jointPlan, CommUser sender) {
	this.worldInterface.sendMessage(new NegotiationBidMessageContent(sender,jointPlan));
	
}

public void sendStartNegotiationMessage(Plan plan) {
	this.worldInterface.sendMessage(new StartNegotiationMessageContent(null,plan));
	
}

public void sendConfirmationMessage(JPlan bestJPlan) {
	// TODO Auto-generated method stub
	
}
}
