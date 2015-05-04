package robotPackage;

import java.util.ArrayList;

import com.github.rinde.rinsim.core.model.comm.Message;
import com.github.rinde.rinsim.geom.Point;

public class BBC {
	Goal goal;
	Boolean charging;
	boolean done;
	
	ArrayList<Message> messages;
	
	public BBC(WorldInterface worldInterface, WorldModel model) {
		// TODO Auto-generated constructor stub
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
			if( (model.battery() < 0.25 && goal != charging && !charging) ) pbc.plan(new Charging());
			else{
				if( model.messages().size() !=0) pbc.plan();
			}
		}
		
		checkMessages();
		
		if( goal == null){
			model.wait();
			return;
		}
		if(goal.coordinate() != worldinterface.coordinate()){
			model.moveTo(goal.coordinate());
			return;
		}
		if(goal.type() = "pickup"){
			model.pickup();
			done =true;
			return;
		}
		if(goal.type() = "drop"){
			model.drop();
			done = true;
			return;
		}
		if(goal.type= "charging" && this.chargeTaken()){
			model.wait();
			return;
		}
		if(goal.type() = "charging"){
			model.charge();
			if(model.battery() == 1) done =true;
			return;
		}
		
		if(goal.type() = "doBid"){
			
			BidMessage bidmessage = new BidMessage(goal.getReceiver(), goal.getBid(), goal.getPackage());
			if(model.battery() == 1) done =true;
			return;
		}


	}
	
	private void checkMessages(){
		for(int i = 0; i<messages.size();i++){
			Message message = messages.get(i);
			MessageContent content = (MessageContent) message.getContents();
			if(content.getType().equals("DeliverPackage")){
				DeliverPackageMessage packMessage = (DeliverPackageMessage) content;
				pbc.doBid(packMessage.)
			}
		}
	}


}
