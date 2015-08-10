package WorldModel;

import com.github.rinde.rinsim.geom.Point;
import com.github.rinde.rinsim.util.TimeWindow;

public class Goal {
	private Point point;
	private GoalTypes type;
	
	private long startWindow;

	private long endWindow;
	
	/**
	 * A goal of a drone which is taken up in a plan
	 * @param coordinates/ the location of the goal
	 * @param type: the type of goal
	 * @param window: a timewindow in case this is needed
	 */
	public Goal(Point coordinates, GoalTypes type, TimeWindow window){
		point = coordinates;
		this.type = type;
		startWindow=window.begin;
		endWindow= window.end;
	}
	public Point coordinates() {
		return point;
	}
	
	public GoalTypes type() {
		return type;
	}
	
	public long getStartWindow() {
		return startWindow;
	}
	public void setStartWindow(long startWindow) {
		this.startWindow = startWindow;
	}
	public long getEndWindow() {
		return endWindow;
	}
	public void setEndWindow(long endWindow) {
		this.endWindow = endWindow;
	}
	
	public String toString(){
		
		return type+":"+point.toString();
		
	}
	public boolean equals(Object arg0){
		if(arg0 == null) return false;
		if(!(arg0 instanceof Goal)) return false;
		Goal arg1 = (Goal) arg0;
		if(!this.point.equals(arg1.coordinates()))return false;
		if(this.type != arg1.type)return false;
		if(this.startWindow != arg1.getStartWindow())return false;
		if(this.endWindow != arg1.getEndWindow())return false;
		return true;
	}
}
