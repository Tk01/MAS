package robotPackage;

import java.util.ArrayList;

import javax.measure.converter.UnitConverter;

import world.ChargingStation;
import world.Package;
import com.github.rinde.rinsim.core.TimeLapse;
import com.github.rinde.rinsim.core.model.comm.CommUser;
import com.github.rinde.rinsim.core.model.comm.Message;

import com.github.rinde.rinsim.core.model.road.RoadUnits;
import com.github.rinde.rinsim.geom.Point;
import com.google.common.collect.ImmutableList;

public class WorldModel {


	int maxTask=3;
	TimeLapse time =null;

	ArrayList<Point> Robots = new ArrayList<Point>();
	long battery = getMaxBattery();
	ChargingStation ChargingStation;
	Package Carried=null;
	private ArrayList<Message> messages= new ArrayList<Message>();
	private Point coordinates;
	private double speed;
	ArrayList<Point> ChargingStations;
	



	private boolean reserveChargingStation = false;
	public UnitConverter getDistanceConverter() {
		return DistanceConverter;
	}
	public UnitConverter getSpeedConverter() {
		return SpeedConverter;
	}
	private UnitConverter DistanceConverter;
	private UnitConverter SpeedConverter;
	private RoadUnits RoadUnits;


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




	public void batteryDrop(long d) {
		battery= Math.max(battery-d, 0);

	}
	public long battery() {

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
	
	public void charge(long d) {
		battery= Math.min(battery+d, getMaxBattery());

	}
	public Package getCarriedPackage() {
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
	public void deletePreAssign(CommUser sender) {
		for(Message m:this.messages()){
			if(m.getSender()==sender && ((MessageContent) m.getContents()).getType().equals("PreAssignment") ){
				this.messages.remove(m);
				return;
			}
		}


	}
	public void setDistanceConverter(UnitConverter converterTo) {
		DistanceConverter = converterTo;

	}
	public void setSpeedConverter(UnitConverter converterTo) {
		SpeedConverter = converterTo;

	}
	
	public RoadUnits getRoadUnits() {
		// TODO Auto-generated method stub
		return RoadUnits;
	}
	public void setRoadUnits(RoadUnits roadUnits2) {
		this.RoadUnits	=roadUnits2;
	}
	public long getMaxBattery() {
		// TODO Auto-generated method stub
		return 1000l*10000l;
	}
	public long calcTime(Point point, Point point2) {
		;
		// TODO Auto-generated method stub
    	
		return (long) RoadUnits.toExTime(RoadUnits.toInDist(Point.distance(point,point2))/RoadUnits.toInSpeed(speed),time.getTimeUnit());
	}
}
