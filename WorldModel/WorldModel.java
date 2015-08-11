package WorldModel;

import java.util.ArrayList;

import world.ChargingStation;
import world.Package;

import com.github.rinde.rinsim.core.TimeLapse;
import com.github.rinde.rinsim.core.model.comm.CommUser;
import com.github.rinde.rinsim.core.model.comm.Message;
import com.github.rinde.rinsim.core.model.pdp.PDPModel;
import com.github.rinde.rinsim.core.model.road.RoadModel;
import com.github.rinde.rinsim.core.model.road.RoadUnits;
import com.github.rinde.rinsim.geom.Point;
import com.github.rinde.rinsim.util.TimeWindow;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

public class WorldModel {


	private Robot robot;
	private TimeLapse time =null;
	private long chargeRate;
	private ArrayList<Point> Robots = new ArrayList<Point>();
	private long battery;
	private ChargingStation ChargingStation;
	private Package Carried=null;
	private ArrayList<Message> messages= new ArrayList<Message>();
	private Point coordinates;
	private double speed;
	private long BatterySize;
	private final boolean reserveChargingStation ;
	private RoadUnits RoadUnits;
	private Optional<RoadModel> roadModel;
	private Optional<PDPModel> pdpModel;
	private Plan currentPlan;
	
	private ArrayList<TimeWindow> windows;
	
	private Goal currentGoal;


	public WorldModel(Point p,ChargingStation c, double s,long BatterySize, long chargeRate, boolean reserveChargingStation, Robot robot, Optional<RoadModel> roadModel, Optional<PDPModel> pdpModel ) {
		this.robot = robot;
		coordinates =p;
		ChargingStation=c;
		speed =s;
		this.BatterySize = BatterySize;
		battery=BatterySize;
		this.chargeRate=chargeRate;
		this.reserveChargingStation =reserveChargingStation;
		this.roadModel=(Optional<RoadModel>) roadModel;
		this.pdpModel=(Optional<PDPModel>) pdpModel;
		currentPlan = new Plan(new ArrayList<Goal>(), this);
		
		 ArrayList<TimeWindow> windows =  new ArrayList<TimeWindow>();
	     windows.add(TimeWindow.ALWAYS);
	     setWindows(windows);
	}
	public ArrayList<Point> getRobots() {

		return Robots;
	}
	public void setRobots(ArrayList<Point> robots) {
		Robots = robots;
	}

	

	public ArrayList<TimeWindow> getWindows() {
		return windows;
	}
	public void setWindows(ArrayList<TimeWindow> windows) {
		this.windows = windows;
	}
	public Goal getCurrentGoal() {
		return currentGoal;
	}
	public void setCurrentGoal(Goal currentGoal) {
		this.currentGoal = currentGoal;
	}
	public Plan getCurrentPlan() {
		return currentPlan;
	}
	public Optional<PDPModel> getPdpModel() {
		return pdpModel;
	}
	public Optional<RoadModel> getRoadModel() {
		return roadModel;
	}
	public void batteryDrop(long d) {
		battery= Math.max(battery-d, 0);

	}
	public long battery() {

		return battery;
	}

	public ArrayList<Message> messages() {
		return messages;
	}
	public Point coordinates() {
		return coordinates;
	}
	
	public void charge(long d) {
		battery= Math.min(battery+d, getMaxBattery());

	}
	public Package getCarriedPackage() {
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
		return speed;
	}

	public boolean isReserveChargingStation() {
		return reserveChargingStation;
	}

	public RoadUnits getRoadUnits() {
		return RoadUnits;
	}
	public void setRoadUnits(RoadUnits roadUnits) {
		this.RoadUnits	= roadUnits;
	}
	public long getMaxBattery() {		
		return BatterySize;
	}
	/**
	 * calculates how long it takes to move from point to point2
	 * @note this method is not 100% accurate
	 */
	public long calcTime(Point point, Point point2) {
		return (long) RoadUnits.toExTime(RoadUnits.toInDist(Point.distance(point,point2))/RoadUnits.toInSpeed(speed),time.getTimeUnit());
	}
	public long getChargeRate() {
		return this.chargeRate;
	}
	public ChargingStation getChargingStation() {
		return ChargingStation;
	}
	public Robot getThisRobot() {
		return robot;
	}
}
