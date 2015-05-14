package robotPackage;

import com.github.rinde.rinsim.geom.Point;
import com.github.rinde.rinsim.util.TimeWindow;

public class Goal {
	Point point;
	String type;
	
	long startWindow;

	long endWindow;
	
	public Goal(Point coordinates, String type, TimeWindow window){
		point = coordinates;
		this.type = type;
		startWindow=window.begin;
		endWindow= window.end;
	}
	public Point coordinates() {
		// TODO Auto-generated method stub
		return point;
	}
	public String type() {
		// TODO Auto-generated method stub
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
	

}
