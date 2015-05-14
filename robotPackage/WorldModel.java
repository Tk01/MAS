package robotPackage;

import java.util.ArrayList;
import world.ChargingStation;
import com.github.rinde.rinsim.core.TimeLapse;
import com.github.rinde.rinsim.core.model.comm.Message;
import com.github.rinde.rinsim.core.model.pdp.Parcel;
import com.github.rinde.rinsim.geom.Point;
import com.google.common.collect.ImmutableList;

public class WorldModel {


	int maxTask=3;
	TimeLapse time =null;

	ArrayList<Point> Robots = new ArrayList<Point>();
	Double battery = 1d;
	ChargingStation ChargingStation;
	world.Package Carried=null;
	private ArrayList<Message> messages= new ArrayList<Message>();
	private Point coordinates;
	private double speed;
	ArrayList<Point> ChargingStations;
	
	
	private boolean reserveChargingStation = false;

	
	public WorldModel(Point p,ChargingStation c, double s ) {
		coordinates =p;
		ChargingStation=c;
		speed =s;
	}
	public ArrayList<Point> getRobots() {

		return Robots;
	}
	public void setRobots(ArrayList<Point> robots) {
		Robots = robots;
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
		return Carried;
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

	public TimeLapse getTime() {
		return time;
	}
	public void setTime(TimeLapse time) {
		this.time = time;
	}
	public double getSpeed() {
		// TODO Auto-generated method stub
		return speed;
	}
	
	public boolean isReserveChargingStation() {
		return reserveChargingStation;
	}
	public void setReserveChargingStation(boolean reserveChargingStation) {
		this.reserveChargingStation = reserveChargingStation;
	}


}
