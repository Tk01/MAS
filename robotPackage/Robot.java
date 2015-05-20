package robotPackage;

import world.ChargingStation;

import com.github.rinde.rinsim.core.TimeLapse;
import com.github.rinde.rinsim.core.model.comm.CommDeviceBuilder;
import com.github.rinde.rinsim.core.model.comm.CommUser;
import com.github.rinde.rinsim.core.model.pdp.PDPModel;
import com.github.rinde.rinsim.core.model.pdp.Vehicle;
import com.github.rinde.rinsim.core.model.road.RoadModel;
import com.github.rinde.rinsim.geom.Point;
import com.google.common.base.Optional;

public class Robot extends Vehicle implements CommUser{
	WorldInterface inter;
	WorldModel model;
	public Robot(Point location, ChargingStation chargingStation, double speed){
		setStartPosition(location);
		setCapacity(1);
		inter = new WorldInterface(this, location, chargingStation, speed);
		model = inter.getModel();
	}
	@Override
	public double getSpeed() {
		// TODO Auto-generated method stub
		return this.model.getSpeed();
	}

	@Override
	protected void tickImpl(TimeLapse arg0) {
		this.inter.run(arg0);
		
	}

	@Override
	public void initRoadPDP(RoadModel arg0, PDPModel arg1) {
		this.inter.initRoadPDP( arg0,  arg1);
		
	}
	@Override
	public Optional<Point> getPosition() {
		return Optional.of(this.model.coordinates());

	}
	@Override
	public void setCommDevice(CommDeviceBuilder builder) {
		// TODO Auto-generated method stub
		this.inter.setCommDevice(builder);
	}
	

}
