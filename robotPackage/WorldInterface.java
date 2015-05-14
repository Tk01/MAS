package robotPackage;



import java.awt.List;
import java.util.ArrayList;

import com.github.rinde.rinsim.core.TimeLapse;
import com.github.rinde.rinsim.core.model.comm.CommDevice;
import com.github.rinde.rinsim.core.model.comm.CommUser;
import com.github.rinde.rinsim.core.model.comm.Message;
import com.github.rinde.rinsim.core.model.pdp.PDPModel;
import com.github.rinde.rinsim.core.model.pdp.Parcel;
import com.github.rinde.rinsim.core.model.road.MoveProgress;
import com.github.rinde.rinsim.core.model.road.RoadModel;
import com.github.rinde.rinsim.geom.Point;
import com.google.common.base.Optional;
import world.Package;
public class WorldInterface {
	private final double SpendingRate = 0.01;
	CommDevice translator;
	Point moveTo;
	BBC bbc;
	WorldModel model;
	TimeLapse time;
	private Optional<RoadModel> roadModel;
	private Optional<PDPModel> pdpModel;
	private Robot robot;
	public WorldInterface(CommDevice communication,Robot robot){
		bbc = new BBC(this,model,robot);
		this.robot = robot;
		roadModel = Optional.absent();
		pdpModel = Optional.absent();

	}
	public void sendMessage( Message message) {
		
	}
	public void MoveTo(Point x){
		MoveProgress consumed = this.roadModel.get().moveTo(robot, x, time);
		model.batteryDrop(time.getTimeConsumed()*SpendingRate);
	}
	public void run(TimeLapse time){
		if(model.battery() > 0){
		this.gatherInfo();
		bbc.run();
		}
	}
	private void gatherInfo() {
		model.setCoordinates(this.roadModel.get().getPosition(robot));
		model.addMessages(translator.getUnreadMessages());
		ArrayList<Point> list = new ArrayList<Point>();
		for(Robot r:roadModel.get().getObjectsAt(this.robot, Robot.class)){
			if(!r.equals(this.robot)){
				list.add(model.coordinates());
			}
		}
		
		model.setRobots(list);
	}
	public void charge() {
		double toCharge = 1-model.battery();
		double chargeRate = 5*this.SpendingRate;
		double timeSpend = Math.max(toCharge/chargeRate , time.getTimeLeft());
		model.charge(timeSpend*chargeRate);
		time.consume((long) timeSpend);
	}
	public void drop() {
		pdpModel.get().drop(robot, model.getCarriedPackage(), time);
		model.dropPackage();
		time.consume(0);
		
	}
	public void pickup() {
		Package parcel = this.getPackageHere();
		pdpModel.get().pickup(robot, parcel, time);
		model.pickupPackage(parcel);
		time.consume(0);
	}
	private Package getPackageHere() {
		return roadModel.get().getObjectsAt(robot, world.Package.class).iterator().next();
	}
	public void waitMoment() {
		model.batteryDrop(time.getTimeLeft()*SpendingRate);
		time.consume(time.getTimeLeft());
		
	}


	

}
