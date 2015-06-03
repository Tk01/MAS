package robotPackage;

import java.util.ArrayList;

import com.github.rinde.rinsim.core.model.comm.CommUser;
import com.github.rinde.rinsim.geom.Point;
import com.github.rinde.rinsim.util.TimeWindow;

public class BBC {
	private Goal goal;
	private boolean done;
	private WorldModel model;
	private Robot thisRobot;
	private PBC pbc ;

	private WorldInterface worldInterface;



	public BBC(WorldInterface worldInterface, WorldModel model, Robot robot, long delay) {
		thisRobot = robot;
		this.model = model;
		this.worldInterface =worldInterface;
		pbc= new PBC(this,delay);
	}
	/**
	 * This method will contact the pbc if apprioriate and handle the current goal. 
	 */
	public void run(){
		//ask pbc for new goal if old one is finished
		if(done){
			pbc.done(goal);
			done = false;
		}
		//call the pbc to check if a negotiationreply has to be send 
		pbc.checkNegotiation();
		//call the pbc to read the messages 
		if( model.messages().size() !=0) {
			
			pbc.readMessages();
			
		}
		//if you have no goals and if you would go the chargingsation,you would have less then 90% of your battery on your arrival ask the pbc to plan a charging goal
		if(this.goal ==null && model.battery()- model.calcTime(model.coordinates(),model.getChargingStation().getPosition().get()) < model.getMaxBattery()*0.90){
			pbc.placeCharge();
		}
		
		if( goal == null){
			//if you have no goal and you're on the charging station leave the charging station
			if(model.coordinates().equals(model.getChargingStation().getPosition().get())){
				goal = new Goal(new Point(5,4), GoalTypes.MoveTo , TimeWindow.ALWAYS);
				this.worldInterface.MoveTo(new Point(5,4));
				return;
			}
			//if you have no goal, move to point (5,4)
			if(!model.coordinates().equals(new Point(5,4))){
				goal = new Goal(new Point(5,4),GoalTypes.MoveTo , TimeWindow.ALWAYS);
				this.worldInterface.MoveTo(new Point(5,4));
				return;
			}
			//if you have no goal and on point (5,4) wait.
			worldInterface.waitMoment(true);
			return;
		}
		if(goal.type() == GoalTypes.Charging && chargeTaken()){
			// end charging when your timewindow is up
			if(goal.getEndWindow() <= model.getTime().getTime()){
				done = true;
				return;
			}
			// if you're on the chargingstation while it is taken move to point (4,4.9)
			if(this.model.coordinates().equals(model.getChargingStation().getPosition().get()) ){
				this.worldInterface.MoveTo(new Point(5,4.9));
				return;	
			}
			boolean b = false;
			if(model.isReserveChargingStation())b=true;
			if(model.battery() >Plan.getLimit()*model.getMaxBattery())b=true;
			// if you're on the chargingstaion while it is take,wait
			this.worldInterface.waitMoment(b);
			return;
		}
		//if you're not on the coordinates of your goal move to your goal.
		if(!goal.coordinates().equals(model.coordinates())){
			worldInterface.MoveTo(goal.coordinates());
			return;
		}
		// if the time is not before the start of your goal's timewindow in wait for the right to process your goal
		if(goal.getStartWindow() > model.getTime().getTime() ){
			worldInterface.waitMoment(true);
			return;
		}
		//if your goal is moveto it is completed
		if(goal.type() == GoalTypes.MoveTo){
			done = true;
			return;
		}
		//if your goal is drop , drop the package and the goal is completed
		if(goal.type() == GoalTypes.Drop){
			worldInterface.drop();
			done = true;
			return;
		}
		//if your goal is pickup , pickup the package and the goal is completed
		if(goal.type() == GoalTypes.Pickup){

				worldInterface.pickup();
				done =true;
				return;
			
			
			
		}
		//if your goal is charging , charge, if the battery is full or you reached the end of the time window the goal is completed
		if(goal.type() ==GoalTypes.Charging ){
			worldInterface.charge(((ChargeGoal)goal).getEndWindow());
			if(model.battery() == model.getMaxBattery() || ((ChargeGoal)goal).getEndWindow() <= model.getTime().getTime()){
				done =true;
			}
			return;
		}
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

/**
 * see if the chargingstation is taken
 */
public boolean chargeTaken() {
	if(((ChargeGoal)this.goal).isReserved() && ((ChargeGoal)this.goal).getStartWindow() <= model.getTime().getTime()+ model.calcTime(model.coordinates(),model.getChargingStation().getPosition().get()) && ((ChargeGoal)this.goal).getEndWindow() >= model.getTime().getTime()+ model.calcTime(model.coordinates(),model.getChargingStation().getPosition().get())) return false;
	if(((ChargeGoal)this.goal).isReserved() && !(((ChargeGoal)this.goal).getStartWindow() <= model.getTime().getTime()+ model.calcTime(model.coordinates(),model.getChargingStation().getPosition().get()) && ((ChargeGoal)this.goal).getEndWindow() >= model.getTime().getTime()+ model.calcTime(model.coordinates(),model.getChargingStation().getPosition().get()))) return true;
	for(Point r:model.getRobots()){
		if(r.equals(new Point(5,5)))return true;
		if(Point.distance(r, model.getChargingStation().getPosition().get()) <= 0.1 && Point.distance(r, model.getChargingStation().getPosition().get())  <= Point.distance(model.coordinates(), model.getChargingStation().getPosition().get())){
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
