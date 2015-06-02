package robotPackage;

import java.util.ArrayList;
import java.util.NoSuchElementException;

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


	/**
	 * Constructor of the Behaviour based component
	 * @param worldInterface: the link to the worldinterface
	 * @param model: the worldmodel
	 * @param robot: the robot for which the BBC is set up
	 * @param delay: the delay for the communication comopnent
	 */
	public BBC(WorldInterface worldInterface, WorldModel model, Robot robot, long delay) {
		thisRobot = robot;
		this.model = model;
		this.worldInterface =worldInterface;
		pbc= new PBC(this,delay);
	}

	


	/**
	 * Each tick the run method is called to do the following things
	 * - Check if the current goal is done and in case yes set a new goal
	 * - Check if an ongoing negotiation is finished
	 * - Check if the ongoing goal is finished	
	 */
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
	
	}
	
	/**
	 * Returns the worldmodel
	 * 
	 */
	public WorldModel getWorldModel(){
		return model;
	}
	
	/**
	 * Sets the current goal of the BBC
	 * @param nextgoal: the goal to be set
	 */
	public void setGoal(Goal nextgoal) {
		this.goal =nextgoal;	
	}
	
	/**
	 * Sends a definitive bid message received from the PBC and send to the worldinterface
	 * @param sender: the sender of the request (the package)
	 * @param bid: the bid of the bid message
	 */
	public void sendDefBidMessage(CommUser sender, double bid) {
		this.worldInterface.sendMessage(new DefBidMessageContent(sender, bid));
		
	
	}
	
	
	/**
	 * Sends a delete of a charging reservation with the timewindows
	 * @param startWindow: startwindow of the reservation
	 * @param endWindow: endwindow of the reservation
	 */
	public void deleteChargeReservation(long startWindow, long endWindow) {
		this.worldInterface.sendMessage(new ChargeMessageContent(this.model.getChargingStation(), startWindow, startWindow, "delete"));
		
	}
	
	
	/**
	 * Send a reservation message to the charging station
	 * @param startWindow: the startwindow of the reservation
	 * @param endWindow: the endwindow of the reservation
	 */
	public void sendReserveMessage(long startWindow, long endWindow) {
		this.worldInterface.sendMessage(new ChargeMessageContent(this.model.getChargingStation(), startWindow, endWindow, "reserve"));	
	}
	
	/**
	 * Send a bid for a negotiation
	 * @param jointPlan: the plans that are in the bid
	 * @param sender: the sender of the request
	 */
	public void sendNegotiationBidMessage(JPlan jointPlan, CommUser sender) {
		jointPlan.setJPlanAgent(thisRobot);
		this.worldInterface.sendMessage(new NegotiationBidMessageContent(sender,jointPlan));
		
	}
	
	/**
	 * Set up a request to start a negotiation
	 *
	 */
	public void sendStartNegotiationMessage(Point pos,ArrayList<Goal> plan,long battery, long endTime, double minValue) {
		this.worldInterface.sendMessage(new StartNegotiationMessageContent(null,pos,plan,battery, endTime, minValue));
		
	}
	
	/**
	 * Checks if the charging station is available.
	 * This is dependent on the fact if reservation is needed or not
	 * @return: return true if taken
	 */
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
	
	/**
	 * Sets up a positive reply to an agent who did a negotiation bid 
	 * @param agent: the agent to send the message to
	 */
	public void sendNegotiationReplyMessage(CommUser agent) {
		this.worldInterface.sendMessage(new NegotiationReplyMessageContent(agent, true));
		
	}
	
	/**
	 * Send a cancel message for a reservation of the charging station
	 * @param start: start of the timewindow which wanted to be reserved
	 * @param end: end of the timewindow which wanted to be reserved
	 */
	public void sendCancelReservationMessage(long start, long end) {
		this.worldInterface.sendMessage( new ChargeMessageContent(model.getChargingStation(),start, end, "delete"));
		
	}
	
	/**
	 * Sets up a negative reply to an agent who did a negotiation bid 
	 * @param agent: the agent to send the message to
	 */
	public void sendNegativeNegotiationReplyMessage(CommUser agent) {
		this.worldInterface.sendMessage(new NegotiationReplyMessageContent(agent, false));
		
	}
	
	/**
	 * returns the current goal of the BBC
	 */
	public Goal getGoal() {
		return goal;
	}
}
