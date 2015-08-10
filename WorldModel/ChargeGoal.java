package WorldModel;

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
	public ChargeGoal clone(){
		return new ChargeGoal(super.coordinates(),new TimeWindow(super.getStartWindow(),super.getEndWindow()),reserved);
	}
	public boolean equals(Object arg0){
		if(!super.equals(arg0)) return false;
		if(((ChargeGoal) arg0).isReserved() != this.reserved) return false;
		return true;
	}
}
