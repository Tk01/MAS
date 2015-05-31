package robotPackage;

import com.github.rinde.rinsim.geom.Point;
import com.github.rinde.rinsim.util.TimeWindow;

public class Goal {
	private Point point;
	private GoalTypes type;
	
	private long startWindow;

	private long endWindow;
	
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
}
