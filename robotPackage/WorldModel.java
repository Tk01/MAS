package robotPackage;

import java.util.ArrayList;

import com.github.rinde.rinsim.core.model.comm.Message;
import com.github.rinde.rinsim.core.model.pdp.Parcel;
import com.github.rinde.rinsim.geom.Point;
import com.google.common.collect.ImmutableList;

public class WorldModel {
<<<<<<< HEAD
	  Double BatteryLife;
	  int maxTask;
	  Point Location;
	  ArrayList<Point> Robots;
	  
	  int contractID = 0;
	  
	  public ArrayList<Point> getRobots() {
=======
	Double BatteryLife;
	int maxTask;
	Point Location;
	ArrayList<Point> Robots;
	Double battery = 1d;
	Point ChargingStation;
	world.Package Carried;
	private ArrayList<Message> messages;
	private Point coordinates;
	private Parcel carriedPackage;
	public ArrayList<Point> 
	ArrayList<Point> ChargingStations;
	Package Carried;
	
	getRobots() {

		return Robots;
	}
	public void setRobots(ArrayList<Point> robots) {
		Robots = robots;
	}

	
	  
	  public int getContractID(){
		  contractID = contractID+1;
		  return contractID;
	  }

	public void batteryDrop(double d) {
		battery=Math.max(battery-d, 0);

	}
	public double battery() {

		return battery;
	}
	public void moveTo(Point coordinates) {
		// TODO Auto-generated method stub

	}
	public ArrayList<Message> messages() {
		// TODO Auto-generated method stub
		return messages;
	}
	public Point coordinates() {
		// TODO Auto-generated method stub
		return coordinates;
	}
	public boolean chargeTaken() {
		// TODO Auto-generated method stub
		for(Point r:Robots){
			if(this.coordinates.equals(ChargingStation))return true;
		}
		return false;
	}
	public void charge(double d) {
		battery=Math.max(battery+d, 1);

	}
	public Parcel getCarriedPackage() {
		// TODO Auto-generated method stub
		return carriedPackage;
	}
	public void pickupPackage(world.Package parcel) {
		this.Carried =parcel;		
	}
	public void dropPackage() {
		this.Carried = null;
		
	}
	public void addMessages(ImmutableList<Message> unreadMessages) {
		for(Message m:unreadMessages){
			this.messages.add(m);
		}
		
	}
	public void setCoordinates(Point position) {
		this.coordinates=position;	
	}

>>>>>>> origin/master
}
