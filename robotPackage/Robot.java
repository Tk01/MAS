package robotPackage;

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
	public Robot(){
		
	}
	@Override
	public double getSpeed() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	protected void tickImpl(TimeLapse arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void initRoadPDP(RoadModel arg0, PDPModel arg1) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public Optional<Point> getPosition() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public void setCommDevice(CommDeviceBuilder builder) {
		// TODO Auto-generated method stub
		
	}

}
