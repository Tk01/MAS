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

	PBC pbc = new PBC();

	ArrayList<Message> messages;
	private WorldInterface worldInterface;



	public BBC(WorldInterface worldInterface, WorldModel model, Robot robot) {
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
		if( model.messages().size() !=0) pbc.readMessages();


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
}



public WorldModel getWorldModel(){
	return model;
}

public void setGoal(Goal nextgoal) {
	this.goal =nextgoal;	
}

public void sendDefBidMessage(CommUser sender, double bid, Integer id) {
	// TODO Auto-generated method stub

}

public void sendPreBidMessage(CommUser sender, double bid, int iD) {
	// TODO Auto-generated method stub
	
}
}
