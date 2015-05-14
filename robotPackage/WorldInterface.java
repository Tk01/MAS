package robotPackage;



import java.awt.List;
import java.util.ArrayList;

import com.github.rinde.rinsim.core.TimeLapse;
import com.github.rinde.rinsim.core.model.comm.CommDevice;
import com.github.rinde.rinsim.core.model.comm.CommDeviceBuilder;
import com.github.rinde.rinsim.core.model.comm.CommUser;
import com.github.rinde.rinsim.core.model.comm.Message;
import com.github.rinde.rinsim.core.model.pdp.PDPModel;
import com.github.rinde.rinsim.core.model.pdp.Parcel;
import com.github.rinde.rinsim.core.model.road.MoveProgress;
import com.github.rinde.rinsim.core.model.road.RoadModel;
import com.github.rinde.rinsim.geom.Point;
import com.google.common.base.Optional;

import world.ChargingStation;
import world.Package;
public class WorldInterface {
	static public final double SpendingRate = 0.01;
	Optional<CommDevice> translator;
	Point moveTo;
	BBC bbc;
	WorldModel model;
	
	private Optional<RoadModel> roadModel;
	private Optional<PDPModel> pdpModel;
	private Robot robot;
	public WorldInterface(Robot robot, Point p, ChargingStation c, double s){
		model = new WorldModel(p,c,s);
		bbc = new BBC(this,model,robot);
		this.robot = robot;


	}
	public void sendMessage( Message message) {
		
	}
	public void MoveTo(Point x){
		MoveProgress consumed = this.roadModel.get().moveTo(robot, x, model.getTime());
		model.batteryDrop(model.getTime().getTimeConsumed()*SpendingRate);
	}
	public void run(TimeLapse time){
		if(model.battery() > 0){
		this.gatherInfo(time);
		bbc.run();
		}
	}
	private void gatherInfo(TimeLapse time) {
		model.setCoordinates(this.roadModel.get().getPosition(robot));
		model.addMessages(translator.get().getUnreadMessages());
		ArrayList<Point> list = new ArrayList<Point>();
		for(Robot r:roadModel.get().getObjectsAt(this.robot, Robot.class)){
			if(!r.equals(this.robot)){
				list.add(model.coordinates());
			}
		}
		
		model.setRobots(list);
		model.setTime(time);
	}
	public void charge() {
		double toCharge = 1-model.battery();
		double chargeRate = 5*this.SpendingRate;
		double timeSpend = Math.max(toCharge/chargeRate , model.getTime().getTimeLeft());
		model.charge(timeSpend*chargeRate);
		model.getTime().consume((long) timeSpend);
	}
	public void drop() {
		pdpModel.get().drop(robot, model.getCarriedPackage(), model.getTime());
		model.dropPackage();
		model.getTime().consume(0);
		
	}
	public void pickup() {
		Package parcel = this.getPackageHere();
		pdpModel.get().pickup(robot, parcel, model.getTime());
		model.pickupPackage(parcel);
		model.getTime().consume(0);
	}
	private Package getPackageHere() {
		return roadModel.get().getObjectsAt(robot, world.Package.class).iterator().next();
	}
	public void waitMoment() {
		//model.batteryDrop(model.getTime().getTimeLeft()*SpendingRate);
		model.getTime().consume(model.getTime().getTimeLeft());
		
	}
	public void initRoadPDP(RoadModel arg0, PDPModel arg1) {
		roadModel = Optional.of(arg0);
		pdpModel = Optional.of(arg1);
		
	}
	public void setCommDevice(CommDeviceBuilder builder) {
		builder.setMaxRange(10);

		translator = Optional.of(builder
				.setReliability(1)
				.build());	
	}
	public WorldModel getModel() {
		// TODO Auto-generated method stub
		return model;
	}


	

}
