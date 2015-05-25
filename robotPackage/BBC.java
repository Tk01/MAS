package robotPackage;

import java.util.ArrayList;
import java.util.NoSuchElementException;

import com.github.rinde.rinsim.core.model.comm.CommDevice;
import com.github.rinde.rinsim.core.model.comm.CommUser;
import com.github.rinde.rinsim.core.model.comm.Message;
import com.github.rinde.rinsim.core.model.road.RoadUnits;
import com.github.rinde.rinsim.geom.Point;
import com.github.rinde.rinsim.util.TimeWindow;

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
	public void run(){
		if(done){
			pbc.done(goal);
			done = false;
		}
		if( model.messages().size() !=0) 
			pbc.readMessages();
		RoadUnits r = model.getRoadUnits();
		if(goal == null && model.battery()- r.toExTime(r.toInDist(distance(model.coordinates(),model.ChargingStation.getPosition().get()))/r.toInSpeed(model.getSpeed()),model.getTime().getTimeUnit()) < model.getMaxBattery()*0.90){
			pbc.placeCharge();
		}
		
		if( goal == null){
			if(model.coordinates().equals(new Point(5,5))){
				goal = new Goal(new Point(5,4),"MoveTo" , TimeWindow.ALWAYS);
				this.worldInterface.MoveTo(new Point(5,4));
				return;
			}
			worldInterface.waitMoment();
			return;
		}
		if(goal.type().equals("charging") && chargeTaken()){
			if(this.model.coordinates().equals(new Point(5,5)) ){
				this.worldInterface.MoveTo(new Point(5,4.9));
				return;	
			}
			this.worldInterface.waitMoment();
			return;
		}
		
		if(!goal.coordinates().equals(model.coordinates())){
			worldInterface.MoveTo(goal.coordinates());
			return;
		}
		if(goal.getStartWindow() >= model.time.getTime() ){
			worldInterface.waitMoment();
			return;
		}
		if(goal.type().equals("MoveTo")){
			done = true;
			return;
		}
		
		if(goal.type().equals("drop")){
			worldInterface.drop();
			done = true;
			return;
		}
		if(goal.type().equals("pickup")){
			try{
				worldInterface.pickup();
			}catch(NoSuchElementException e){
				pbc.failPickUp();
			}
			
			done =true;
			return;
		}
		
		
		if(goal.type().equals("charging") ){
			worldInterface.charge(((ChargeGoal)goal).getEndWindow());
			if(model.battery() == model.getMaxBattery() || ((ChargeGoal)goal).getEndWindow() == model.getTime().getTime()){
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



	private Double distance(Point point, Point point2) {
		double startX = point.x;
		double startY = point.y;

		double endX = point2.x;
		double endY = point2.y;


		double xd = endX-startX;
		double yd = endY- startY;
		double distance = Math.sqrt(xd*xd + yd*yd);

		return distance;
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
	this.worldInterface.sendMessage(new ChargeMessageContent(this.model.ChargingStation, startWindow, endWindow, "reserve"));	
}

public void sendNegotiationBidMessage(JPlan jointPlan, CommUser sender) {
	jointPlan.setJPlanAgent(thisRobot);
	this.worldInterface.sendMessage(new NegotiationBidMessageContent(sender,jointPlan));
	
}

public void sendStartNegotiationMessage(Plan plan, long endTime) {
	this.worldInterface.sendMessage(new StartNegotiationMessageContent(null,plan, endTime));
	
}

public void sendConfirmationMessage(JPlan bestJPlan) {
	// TODO Auto-generated method stub
	
}
public boolean chargeTaken() {
	if(((ChargeGoal)this.goal).isReserved() && ((ChargeGoal)this.goal).getStartWindow() <= model.getTime().getTime()+ model.calcTime(model.coordinates(),new Point(5,5)) && ((ChargeGoal)this.goal).getEndWindow() >= model.getTime().getTime()+ model.calcTime(model.coordinates(),new Point(5,5))) return false;
	if(((ChargeGoal)this.goal).isReserved() && !(((ChargeGoal)this.goal).getStartWindow() <= model.getTime().getTime()+ model.calcTime(model.coordinates(),new Point(5,5)) && ((ChargeGoal)this.goal).getEndWindow() >= model.getTime().getTime()+ model.calcTime(model.coordinates(),new Point(5,5)))) return true;
	// TODO Auto-generated method stub
	for(Point r:model.Robots){
		if(r.equals(new Point(5,5)))return true;
		if(Point.distance(r, new Point(5,5)) <= 0.1 && Point.distance(r, new Point(5,5))  <= Point.distance(model.coordinates(), new Point(5,5))){
		if(Point.distance(r, new Point(5,5))  < Point.distance(model.coordinates(), new Point(5,5)))return true;
		if(r.x> model.coordinates().x)return true;
		if(r.x== model.coordinates().x && true)return true;
		}
	}
	return false;
}
}
