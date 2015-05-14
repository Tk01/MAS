package robotPackage;

import com.github.rinde.rinsim.geom.Point;

public class Goal {
	Point point;
	String type;
	public Goal(Point coordinates, String type){
		point = coordinates;
		this.type = type;
	}
	public Point coordinates() {
		// TODO Auto-generated method stub
		return point;
	}
	public String type() {
		// TODO Auto-generated method stub
		return type;
	}
	
	

}
