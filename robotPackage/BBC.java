package robotPackage;

import java.util.ArrayList;
import java.util.NoSuchElementException;

import com.github.rinde.rinsim.core.model.comm.CommUser;
import com.github.rinde.rinsim.core.model.comm.Message;
import com.github.rinde.rinsim.geom.Point;
import com.github.rinde.rinsim.util.TimeWindow;

public class BBC {
	private Goal goal;
	private boolean done;
	private WorldModel model;
	private Robot thisRobot;
	private PBC pbc ;

	private ArrayList<Message> messages = new ArrayList<Message>();
	private WorldInterface worldInterface;



	public BBC(WorldInterface worldInterface, WorldModel model, Robot robot, long delay) {
		thisRobot = robot;
		this.model = model;
		this.worldInterface =worldInterface;
		pbc= new PBC(this,delay);
	}

	public void msg(Message message) {
		messages.add(message);

	}

	public void placeGoal(Goal goal){
		this.goal = goal;
	}
	public void run(){
		
		if(done){
			pbc.done(goal);
			done = false;
		}
		
		pbc.checkNegotiation();
		
		if( model.messages().size() !=0) {
			
			pbc.readMessages();
			
		}
		if(this.goal ==null && model.battery()- model.calcTime(model.coordinates(),model.getChargingStation().getPosition().get()) < model.getMaxBattery()*0.90){
			pbc.placeCharge();
		}
		
		if( goal == null){
			if(model.coordinates().equals(model.getChargingStation().getPosition().get())){
				goal = new Goal(new Point(5,4), GoalTypes.MoveTo , TimeWindow.ALWAYS);
				this.worldInterface.MoveTo(new Point(5,4));
				return;
			}
			if(!model.coordinates().equals(new Point(5,4))){
				goal = new Goal(new Point(5,4),GoalTypes.MoveTo , TimeWindow.ALWAYS);
				this.worldInterface.MoveTo(new Point(5,4));
				return;
			}
			worldInterface.waitMoment(true);
			return;
		}
		if(goal.type() == GoalTypes.Charging && chargeTaken()){
			if(goal.getEndWindow() <= model.getTime().getTime()){
				done = true;
				return;
			}
			if(this.model.coordinates().equals(model.getChargingStation().getPosition().get()) ){
				this.worldInterface.MoveTo(new Point(5,4.9));
				return;	
			}
			boolean b = false;
			if(model.isReserveChargingStation())b=true;
			if(model.battery() >0.1*model.getMaxBattery())b=true;
			this.worldInterface.waitMoment(b);
			return;
		}
		
		if(!goal.coordinates().equals(model.coordinates())){
			worldInterface.MoveTo(goal.coordinates());
			return;
		}
		
		if(goal.getStartWindow() >= model.getTime().getTime() ){
			worldInterface.waitMoment(true);
			return;
		}
		if(goal.type() == GoalTypes.MoveTo){
			done = true;
			return;
		}
		
		if(goal.type() == GoalTypes.Drop){
			worldInterface.drop();
			done = true;
			return;
		}
		if(goal.type() == GoalTypes.Pickup){
			try{
				worldInterface.pickup();
				done =true;
				return;
			}catch(NoSuchElementException e){
				pbc.failPickUp();
			}
			
			
		}
		
		
		if(goal.type() ==GoalTypes.Charging ){
			worldInterface.charge(((ChargeGoal)goal).getEndWindow());
			if(model.battery() == model.getMaxBattery() || ((ChargeGoal)goal).getEndWindow() <= model.getTime().getTime()){
				done =true;
			}
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
public WorldModel getWorldModel(){
	return model;
}

public void setGoal(Goal nextgoal) {
	this.goal =nextgoal;	
}

public void sendDefBidMessage(CommUser sender, double bid) {
	this.worldInterface.sendMessage(new DefBidMessageContent(sender, bid));
	

}



public void deleteChargeReservation(long startWindow, long endWindow) {
	this.worldInterface.sendMessage(new ChargeMessageContent(this.model.getChargingStation(), startWindow, startWindow, "delete"));
	
}

public void sendReserveMessage(long startWindow, long endWindow) {
	this.worldInterface.sendMessage(new ChargeMessageContent(this.model.getChargingStation(), startWindow, endWindow, "reserve"));	
}

public void sendNegotiationBidMessage(JPlan jointPlan, CommUser sender) {
	jointPlan.setJPlanAgent(thisRobot);
	this.worldInterface.sendMessage(new NegotiationBidMessageContent(sender,jointPlan));
	
}

public void sendStartNegotiationMessage(Point pos,ArrayList<Goal> plan,long battery, long endTime, double minValue) {
	this.worldInterface.sendMessage(new StartNegotiationMessageContent(null,pos,plan,battery, endTime, minValue));
	
}


public boolean chargeTaken() {
	if(((ChargeGoal)this.goal).isReserved() && ((ChargeGoal)this.goal).getStartWindow() <= model.getTime().getTime()+ model.calcTime(model.coordinates(),model.getChargingStation().getPosition().get()) && ((ChargeGoal)this.goal).getEndWindow() >= model.getTime().getTime()+ model.calcTime(model.coordinates(),model.getChargingStation().getPosition().get())) return false;
	if(((ChargeGoal)this.goal).isReserved() && !(((ChargeGoal)this.goal).getStartWindow() <= model.getTime().getTime()+ model.calcTime(model.coordinates(),model.getChargingStation().getPosition().get()) && ((ChargeGoal)this.goal).getEndWindow() >= model.getTime().getTime()+ model.calcTime(model.coordinates(),model.getChargingStation().getPosition().get()))) return true;
	for(Point r:model.getRobots()){
		if(r.equals(new Point(5,5)))return true;
		if(Point.distance(r, model.getChargingStation().getPosition().get()) <= 0.1 && Point.distance(r, model.getChargingStation().getPosition().get())  <= Point.distance(model.coordinates(), new Point(5,5))){
		if(Point.distance(r, model.getChargingStation().getPosition().get())  < Point.distance(model.coordinates(), model.getChargingStation().getPosition().get()))return true;
		if(r.x> model.coordinates().x)return true;
		if(r.x== model.coordinates().x && true)return true;
		}
	}
	return false;
}

public void sendNegotiationReplyMessage(CommUser jPlanAgent) {
	this.worldInterface.sendMessage(new NegotiationReplyMessageContent(jPlanAgent, true));
	
}

public void sendCancelReservationMessage(long start, long end) {
	this.worldInterface.sendMessage( new ChargeMessageContent(model.getChargingStation(),start, end, "delete"));
	
}

public void sendNegativeNegotiationReplyMessage(CommUser l) {
	this.worldInterface.sendMessage(new NegotiationReplyMessageContent(l, false));
	
}

public Goal getGoal() {
	return goal;
}
}
