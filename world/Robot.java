package world;

import robotPackage.FlowControl;






import WorldModel.Goal;
import WorldModel.WorldModel;

import com.github.rinde.rinsim.core.TimeLapse;
import com.github.rinde.rinsim.core.model.comm.CommDeviceBuilder;
import com.github.rinde.rinsim.core.model.comm.CommUser;
import com.github.rinde.rinsim.core.model.pdp.PDPModel;
import com.github.rinde.rinsim.core.model.pdp.Vehicle;
import com.github.rinde.rinsim.core.model.road.RoadModel;
import com.github.rinde.rinsim.geom.Point;
import com.google.common.base.Optional;

public class Robot extends Vehicle implements CommUser{
	private FlowControl flow;
	private WorldModel model;
	private Point Startlocation;
	public Robot(Point location, ChargingStation chargingStation, double speed, long batterySize, long chargeRate, boolean reserveChargingStation, long delay){
		setStartPosition(location);
		setCapacity(1);
		Startlocation = location;
		flow = new FlowControl(this, location, chargingStation, speed, batterySize, chargeRate, reserveChargingStation,delay);
		model = flow.getModel();
	}
	@Override
	public double getSpeed() {
		return this.model.getSpeed();
	}

	@Override
	protected void tickImpl(TimeLapse arg0) {
		while(arg0.hasTimeLeft()){
			flow.run(arg0);
		}
		this.model.batteryDrop(arg0.getTimeStep());
	}

	@Override
	public void initRoadPDP(RoadModel arg0, PDPModel arg1) {
		model.initRoadPDP( arg0,  arg1);
	}
	@Override
	public Optional<Point> getPosition() {
		return Optional.of(this.model.coordinates());

	}
	@Override
	public void setCommDevice(CommDeviceBuilder builder) {
		this.flow.setCommDevice(builder);
	}
	
	public Goal getGoal() {
		return model.getCurrentGoal();
	}
	public long getBattery() {
		return model.battery();
	}
	public Point getStartLocation() {
		return this.Startlocation;
	}

}
