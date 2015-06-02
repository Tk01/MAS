package robotPackage;

import com.github.rinde.rinsim.geom.Point;
import com.github.rinde.rinsim.util.TimeWindow;

public class ChargeGoal extends Goal{
	
	boolean reserved;
	
	/**
	 * This is a ChargeGoal which sets the drone to go charging. If reservation is needed a timewindow is set.
	 * @param point
	 * @param timeWindow
	 * @param reserved
	 */
	public ChargeGoal(Point point, TimeWindow timeWindow, boolean reserved){
	super(point, GoalTypes.Charging, timeWindow);
	this.reserved = reserved;
	}
	
	public boolean isReserved(){
		return reserved;
	}
	
	public void setReserved(boolean reserved){
		this.reserved = reserved;
	}

}
