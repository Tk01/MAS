package robotPackage;

import java.util.ArrayList;

import worldInterface.Actions;
import Messages.ChargeMessageContent;
import Messages.DefBidMessageContent;
import Messages.NegotiationBidMessageContent;
import Messages.NegotiationReplyMessageContent;
import Messages.StartNegotiationMessageContent;
import Planning.ContractNet;
import WorldModel.ChargeGoal;
import WorldModel.Goal;
import WorldModel.GoalTypes;
import WorldModel.JPlan;
import WorldModel.Plan;
import WorldModel.Robot;
import WorldModel.WorldModel;

import com.github.rinde.rinsim.core.model.comm.CommUser;
import com.github.rinde.rinsim.geom.Point;
import com.github.rinde.rinsim.util.TimeWindow;

public class GoalsToActions {
	//private Goal goal;
	private boolean done;
	private WorldModel model;
	private Actions actions;
	//private Robot thisRobot;
	//private ContractNet pbc ;

	//private WorldInterface worldInterface;


	/**
	 * Constructor of the Behaviour based component
	 * @param worldInterface: the link to the worldinterface
	 * @param model: the worldmodel
	 * @param robot: the robot for which the BBC is set up
	 * @param delay: the delay for the communication comopnent
	 */
	public GoalsToActions( WorldModel model, Actions actions) {
		this.model = model;
	}
		

	/**
	 * This method will check the current goal and translate these in actions
	 */
	public void goalToActions(){
		Goal goal = model.getCurrentGoal();
		//ask pbc for new goal if old one is finished
		if(done){
			done(goal);
			goal = model.getCurrentGoal();
			done = false;
		}
	
		
		//if you have no goals and if you would go the charging station, you would have less then 90% of your battery on your arrival ask the pbc to plan a charging goal
		//TO SEE WHERE THIS NEEDS to be done as this is planning and should be done in planning module
		if(goal ==null && model.battery()- model.calcTime(model.coordinates(),model.getChargingStation().getPosition().get()) < model.getMaxBattery()*0.90){
			pbc.placeCharge(); //This should be done in the planning
		}
		

		if( goal == null){
			//if you have no goal and you're on the charging station leave the charging station
			if(model.coordinates().equals(model.getChargingStation().getPosition().get())){
				goal = new Goal(new Point(5,4), GoalTypes.MoveTo , TimeWindow.ALWAYS);
				actions.MoveTo(new Point(5,4));
				return;
			}
			//if you have no goal, move to point (5,4)
			if(!model.coordinates().equals(new Point(5,4))){
				goal = new Goal(new Point(5,4),GoalTypes.MoveTo , TimeWindow.ALWAYS);
				actions.MoveTo(new Point(5,4));
				return;
			}
			//if you have no goal and on point (5,4) wait.
			actions.waitMoment(true);
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
				actions.MoveTo(new Point(5,4.9));
				return;	
			}
			boolean b = false;
			if(model.isReserveChargingStation())b=true;
			if(model.battery() >Plan.getLimit()*model.getMaxBattery())b=true;
			// if you're on the chargingstaion while it is take,wait
			actions.waitMoment(b);
			return;
		}
		//if you're not on the coordinates of your goal move to your goal.
		if(!goal.coordinates().equals(model.coordinates())){
			actions.MoveTo(goal.coordinates());
			return;
		}
		// if the time is not before the start of your goal's timewindow in wait for the right to process your goal
		if(goal.getStartWindow() > model.getTime().getTime() ){
			actions.waitMoment(true);
			return;
		}
		//if your goal is moveto it is completed
		if(goal.type() == GoalTypes.MoveTo){
			done = true;
			return;
		}
		//if your goal is drop , drop the package and the goal is completed
		if(goal.type() == GoalTypes.Drop){
			actions.drop();
			done = true;
			return;
		}
		//if your goal is pickup , pickup the package and the goal is completed
		if(goal.type() == GoalTypes.Pickup){

			actions.pickup();
			done =true;
			return;



		}
		//if your goal is charging , charge, if the battery is full or you reached the end of the time window the goal is completed
		if(goal.type() ==GoalTypes.Charging ){
			actions.charge(((ChargeGoal)goal).getEndWindow());
			if(model.battery() == model.getMaxBattery() || ((ChargeGoal)goal).getEndWindow() <= model.getTime().getTime()){
				done =true;
			}
			return;
		}
	}

	
	/**
	 * remove the goal that is done from the current plan and set the current goal in the bbc
	 */
	public void done(Goal g){
		model.getCurrentPlan().remove(g);
		removeUnattainablePackages(model.getCurrentPlan());
		model.setCurrentGoal(model.getCurrentPlan().getNextgoal());

	}
	/**
	 * remove the pickup goals and drop goals that can't be reached in time
	 */
	private void removeUnattainablePackages(Plan plan) {
		while(plan != null && plan.getNextgoal() !=null && plan.getNextgoal().type() == GoalTypes.Pickup && plan.getNextgoal().getEndWindow() < model.calcTime(model.coordinates(), plan.getNextgoal().coordinates())+ model.getTime().getTime()){
			plan.remove(plan.getNextgoal());
			for(Goal g:plan.getPlan()){
				if(g.type() == GoalTypes.Drop){
					plan.remove(g);
					break;
				}
			}
		}

	}

	

	
	
	/**
	 * Checks if the charging station is available.
	 * This is dependent on the fact if reservation is needed or not
	 * @return: return true if taken
	 */
	public boolean chargeTaken() {
		Goal goal = model.getCurrentGoal();
		if(((ChargeGoal)goal).isReserved() && ((ChargeGoal)goal).getStartWindow() <= model.getTime().getTime()+ model.calcTime(model.coordinates(),model.getChargingStation().getPosition().get()) && ((ChargeGoal)goal).getEndWindow() >= model.getTime().getTime()+ model.calcTime(model.coordinates(),model.getChargingStation().getPosition().get())) return false;
		if(((ChargeGoal)goal).isReserved() && !(((ChargeGoal)goal).getStartWindow() <= model.getTime().getTime()+ model.calcTime(model.coordinates(),model.getChargingStation().getPosition().get()) && ((ChargeGoal)goal).getEndWindow() >= model.getTime().getTime()+ model.calcTime(model.coordinates(),model.getChargingStation().getPosition().get()))) return true;
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

	


}
