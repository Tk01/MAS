package robotPackage;

import java.util.ArrayList;

import com.github.rinde.rinsim.geom.Point;

public class WorldModel {
	  Double BatteryLife;
	  int maxTask;
	  Point Location;
	  ArrayList<Point> Robots;
	  
	  int contractID = 0;
	  
	  public ArrayList<Point> getRobots() {
		return Robots;
	}
	public void setRobots(ArrayList<Point> robots) {
		Robots = robots;
	}
	ArrayList<Point> ChargingStations;
	  Package Carried;
	  
	  public int getContractID(){
		  contractID = contractID+1;
		  return contractID;
	  }
}
