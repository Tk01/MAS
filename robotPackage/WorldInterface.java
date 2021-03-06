/**package robotPackage;




import java.util.ArrayList;
import java.util.NoSuchElementException;

import Messages.MessageContent;
import Messages.MessageTypes;
import Messages.NegotiationReplyMessageContent;
import WorldModel.Robot;
import WorldModel.WorldModel;

import com.github.rinde.rinsim.core.TimeLapse;
import com.github.rinde.rinsim.core.model.comm.CommDevice;
import com.github.rinde.rinsim.core.model.comm.CommDeviceBuilder;
import com.github.rinde.rinsim.core.model.pdp.PDPModel;
import com.github.rinde.rinsim.core.model.road.MoveProgress;
import com.github.rinde.rinsim.core.model.road.RoadModel;
import com.github.rinde.rinsim.core.model.road.RoadUnits;
import com.github.rinde.rinsim.geom.Point;
import com.google.common.base.Optional;

import world.ChargingStation;
import world.InformationHandler;
import world.Package;
public class WorldInterface {

	private Optional<CommDevice> translator;
	private GoalsToActions goalsToActions;
	private WorldModel model;
	private Optional<RoadModel> roadModel;
	private Optional<PDPModel> pdpModel;
	private Robot robot;
	
	public WorldInterface(Robot robot, Point p, ChargingStation c, double s, long batterySize, long chargeRate, boolean reserveChargingStation, long delay){
		roadModel= Optional.absent();
		pdpModel= Optional.absent();
		model = new WorldModel(p,c,s, batterySize, chargeRate, reserveChargingStation,robot, roadModel, pdpModel);
		goalsToActions = new GoalsToActions(this,model,robot,delay);
		this.robot = robot;
		
		
	}
	public void sendMessage( MessageContent MessageContent) {
		if(MessageContent.getUser() ==null){
			this.translator.get().broadcast(MessageContent);
		}else{
			if(MessageContent.getType() == MessageTypes.NegotiationReplyMessage 
					&& ((NegotiationReplyMessageContent)MessageContent).isAccepted())
				InformationHandler.getInformationHandler().completedNegatiation();
			this.translator.get().send(MessageContent, MessageContent.getUser());
		}
	}
	public void MoveTo(Point x){
		@SuppressWarnings("unused")
		MoveProgress consumed = this.roadModel.get().moveTo(robot, x, model.getTime());

	}
	public void run(TimeLapse time){
		if(model.battery() > 0){
			this.gatherInfo(time);
			goalsToActions.run();
		}else{
			InformationHandler.getInformationHandler().batteryEmpty(robot,time.getTime());
			time.consumeAll();
		}
	}
	
	 //gather the information that the robot should be able to collect.
	 
	private void gatherInfo(TimeLapse time) {
		model.setCoordinates(this.roadModel.get().getPosition(robot));
		model.addMessages(translator.get().getUnreadMessages());
		ArrayList<Point> list = new ArrayList<Point>();
		for(Robot r:roadModel.get().getObjectsOfType( Robot.class)){
			if(!r.equals(this.robot) &&Point.distance(roadModel.get().getPosition(r),this.model.coordinates()) < 1){
				list.add(roadModel.get().getPosition(r));
			}
		}

		model.setRobots(list);
		model.setTime(time);
	}
	

	public void charge(long l) {
		double toCharge = model.getMaxBattery()-model.battery();
		
		double chargeTime =(long) (toCharge/(model.getChargeRate()+1));
		if(chargeTime ==0) chargeTime =1; 
		long timeSpend = (long) Math.min(Math.min(chargeTime , model.getTime().getTimeLeft()), l-model.getTime().getTime());
		model.charge((model.getChargeRate()+1)*timeSpend);
		model.getTime().consume(timeSpend);
	}
	public void drop() {
		try{
		pdpModel.get().deliver(robot, model.getCarriedPackage(), model.getTime());
		model.getCarriedPackage().deliver();
		model.dropPackage();
		model.getTime().consume(0);
		}
		catch(IllegalArgumentException e){
			throw e;
		}
	}
	public void pickup() throws NoSuchElementException{
		try{
		Package parcel = this.getPackageHere();
		pdpModel.get().pickup(robot, parcel, model.getTime());
		model.pickupPackage(parcel);
		model.getTime().consume(0);}
		catch(IllegalArgumentException e){
			throw e;
		}
		catch(NoSuchElementException e){
			throw e;
		}
	}
	
	 //returns the first package at the location of robot
	  //@throws NoSuchElementException
	 
	private Package getPackageHere() throws NoSuchElementException{
		try{
			return roadModel.get().getObjectsAt(robot, world.Package.class).iterator().next();
		}catch(NoSuchElementException e){
			throw e;
		}
		
	}
	public void waitMoment(boolean b) {
		//model.batteryDrop(model.getTime().getTimeLeft()*SpendingRate);
		InformationHandler.getInformationHandler().addTime(model.getTime().getTimeLeft(), robot);
		model.getTime().consumeAll();;

	}
	public void initRoadPDP(RoadModel arg0, PDPModel arg1) {
		roadModel = Optional.of(arg0);
		pdpModel = Optional.of(arg1);
		model.setRoadUnits(new RoadUnits(arg0.getDistanceUnit(),arg0.getSpeedUnit()));

	}
	public void setCommDevice(CommDeviceBuilder builder) {
		builder.setMaxRange(20);

		translator = Optional.of(builder
				.setReliability(1)
				.build());	
	}
	public WorldModel getModel() {
		return model;
	}

	public GoalsToActions getBBC() {
		return goalsToActions;
	}




}*/
