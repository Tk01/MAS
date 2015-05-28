package robotPackage;

import java.util.ArrayList;

import com.github.rinde.rinsim.core.model.road.RoadUnits;
import com.github.rinde.rinsim.geom.Point;
import com.github.rinde.rinsim.util.TimeWindow;

public class Plan {



	private ArrayList <Goal> goals;


	WorldModel model;

	public Plan(ArrayList <Goal> goals, WorldModel model){
		this.goals =goals;

		this.model =model;
	}

	public ArrayList <Goal> getPlan(){
		return goals;
	}
	
	public void setPlan(ArrayList<Goal> goals){
		this.goals=goals;
	}

	public double distance(Point point,Point point2 ){



		double startX = point.x;
		double startY = point.y;

		double endX = point2.x;
		double endY = point2.y;


		double xd = endX-startX;
		double yd = endY- startY;
		double distance = Math.sqrt(xd*xd + yd*yd);

		return distance;


	}

	public Plan getNegotiationPlan(long negotiationTime){

		RoadUnits roadUnits = model.getRoadUnits();
		Plan negotiationPlan = new Plan(goals, model);

		long time = 0;

		Point currentLocation = model.coordinates();

		for(int i=0;i<goals.size();i++){
			long neededTime = (long) roadUnits.toExTime(roadUnits.toInDist(distance(currentLocation,goals.get(i).coordinates()))/roadUnits.toInSpeed(model.getSpeed()),model.getTime().getTimeUnit());
			time = time + neededTime;
			if(time<negotiationTime){
				negotiationPlan.remove(goals.get(i));
			}
			
		}

		return negotiationPlan;
	}

	/**
	 * Return the time you're not moving packages of the plan. Is used to compare tasks that are added to the current plan. The lower the better
	 */
	public double value(ArrayList<Goal> newPlan, long planTime, Point temppos, long tempbat){
		if(newPlan.size() ==0) return 0;
		RoadUnits r = model.getRoadUnits();
		long time = planTime;
		double battery = tempbat;
		Point curcor = temppos;
		for(Goal g:newPlan){
			long timespend = (long) r.toExTime(r.toInDist(distance(curcor,g.coordinates()))/r.toInSpeed(model.getSpeed()),model.getTime().getTimeUnit());
			time = time+ timespend;
			if(!g.type().equals("charging") && time<g.getStartWindow()){
				timespend = g.getStartWindow()-time+timespend;
				time=g.getStartWindow();
			}
			battery-= timespend;
			if(g.type().equals("charging")){
				double batterydiff = model.getMaxBattery()-battery;
				if(batterydiff/model.getChargeRate()==(long) (batterydiff/model.getChargeRate())){
					batterydiff=(long) (batterydiff/model.getChargeRate());
				}else{
					batterydiff=(long) (batterydiff/model.getChargeRate())+1;
				}
				time=(long) (time+Math.min(g.endWindow-time, batterydiff));
				battery = battery + Math.min(g.endWindow-time, batterydiff)*model.getChargeRate();
			}
			curcor =g.point;
		}
		long totalTimeSpend = time-model.getTime().getTime();
		return totalTimeSpend-TimeDelivering(newPlan,temppos)+((model.getMaxBattery()-battery)/model.getMaxBattery())*r.toExTime(r.toInDist(distance(newPlan.get(newPlan.size()-1).coordinates(),model.ChargingStation.getPosition().get()))/r.toInSpeed(model.getSpeed()),model.getTime().getTimeUnit());
	}



	/**
	 * Calculates the time you are moving packages
	 */
	private long TimeDelivering(ArrayList<Goal> newPlan, Point temppos) {
		@SuppressWarnings("unchecked")
		ArrayList<Goal> newPlan2 = (ArrayList<Goal>) newPlan.clone();
		for(Goal g:newPlan2){
			if(g.type().equals("charging")){
				newPlan2.remove(g);
				break;
			}
		}
		int h=0;
		long val=0;
		if(newPlan2.size() ==0)return 0;
		if(newPlan2.get(0).type().equals("drop")){
			val = model.calcTime(temppos, newPlan2.get(0).coordinates());
			h++;
		}
		for(int i =h;i<newPlan2.size();i+=2){
			val += model.calcTime(newPlan2.get(i).coordinates(), newPlan2.get(i+1).coordinates()); 
		}
		return val;
	}

	/**
	 *  check if the pack can be taken up in the plan by checking if in the specified timewindows it is possible to pick up the package.
	 */
	public Plan isPossiblePlan(Goal pickupGoal, Goal dropGoal, ArrayList<TimeWindow> windows,long startTime){

		ArrayList<Goal> tempgoals = calculateGoals(startTime);
		Point temppos = calculatePosition(startTime);
		long tempbat = calculateBattery(startTime);
		//if(goals.size() > 7) return null;

		@SuppressWarnings("unchecked")
		ArrayList <Goal> copyGoals = (ArrayList<Goal>) tempgoals.clone();
		copyGoals.add(pickupGoal);
		copyGoals.add(dropGoal);
		Goal charged = null;
		for(int i=0; i<copyGoals.size();i++){
			if(copyGoals.get(i).type.equals("charging")){
				charged = copyGoals.remove(i);
				break;
			}
		}

		ArrayList<Goal> newPlan = new ArrayList<Goal>();
		if(copyGoals.size()>0 && copyGoals.get(0).type().equals("drop") ){

			newPlan.add(copyGoals.remove(0));
		}
		ArrayList<Goal> result = GenerateBestPlan(copyGoals,newPlan,charged,null,windows,temppos,tempbat,startTime);
		return new Plan(result,model);
	}



	/**
	 * Calculates how much battery you have at time l according to the plan
	 */
	public long calculateBattery(long l) {
		RoadUnits r = model.getRoadUnits();
		long time = model.getTime().getTime();
		long battery = model.battery();
		Point curcor = model.coordinates();
		for(Goal g:goals){
			long timespend = (long) r.toExTime(r.toInDist(distance(curcor,g.coordinates()))/r.toInSpeed(model.getSpeed()),model.getTime().getTimeUnit());
			time = time+ timespend;

			if(!g.type().equals("charging") && time<g.getStartWindow()){
				timespend = g.getStartWindow()-time+timespend;
				time=g.getStartWindow();
			}
			if(time>= l){
				return  (battery- l+(time- timespend));
			}
			battery-= timespend;
			if(g.type().equals("charging")){
				double batterydiff = model.getMaxBattery()-battery;
				if(batterydiff/model.getChargeRate()==(long) (batterydiff/model.getChargeRate())){
					batterydiff=(long) (batterydiff/model.getChargeRate());
				}else{
					batterydiff=(long) (batterydiff/model.getChargeRate())+1;
				}
				time=(long) (time+Math.min(g.endWindow-time, batterydiff));
				battery = (long) (battery + Math.min(g.endWindow-time, batterydiff)*model.getChargeRate());
				if(time>= l){
					return (long) (battery+ model.getChargeRate()*(l-(time-Math.min(g.endWindow-time, batterydiff))) -Math.min(g.endWindow-time, batterydiff)*model.getChargeRate());
				}
			}
			curcor =g.point;

		}
		return battery;

	}
	/**
	 * Calculates which position you have at time l according to the plan
	 */
	public Point calculatePosition(long l) {
		RoadUnits r = model.getRoadUnits();
		long time = model.getTime().getTime();
		double battery = model.battery();
		Point curcor = model.coordinates();
		for(Goal g:goals){
			long timespend = (long) r.toExTime(r.toInDist(distance(curcor,g.coordinates()))/r.toInSpeed(model.getSpeed()),model.getTime().getTimeUnit());
			time = time+ timespend;
			if(time>= l){
				return new Point(curcor.x+(g.coordinates().x-curcor.x)*(l-(time-timespend))/timespend,curcor.y+(g.coordinates().y-curcor.y)*(l-(time-timespend))/timespend);
			}
			if(!g.type().equals("charging") && time<g.getStartWindow()){
				timespend = g.getStartWindow()-time+timespend;
				time=g.getStartWindow();
			}
			battery-= timespend;
			if(g.type().equals("charging")){
				double batterydiff = model.getMaxBattery()-battery;
				if(batterydiff/model.getChargeRate()==(long) (batterydiff/model.getChargeRate())){
					batterydiff=(long) (batterydiff/model.getChargeRate());
				}else{
					batterydiff=(long) (batterydiff/model.getChargeRate())+1;
				}
				time=(long) (time+Math.min(g.endWindow-time, batterydiff));
				battery = battery + Math.min(g.endWindow-time, batterydiff)*model.getChargeRate();
			}
			curcor =g.point;
			if(time<l){

			}else{
				return curcor;
			}
		}
		return curcor;
	}
	/**
	 * Calculates which goals you have at time l according to the plan
	 */
	public ArrayList<Goal> calculateGoals(long l) {
		@SuppressWarnings("unchecked")
		ArrayList<Goal> result = (ArrayList<Goal>) goals.clone();
		RoadUnits r = model.getRoadUnits();
		long time = model.getTime().getTime();
		double battery = model.battery();
		Point curcor = model.coordinates();
		for(Goal g:goals){
			long timespend = (long) r.toExTime(r.toInDist(distance(curcor,g.coordinates()))/r.toInSpeed(model.getSpeed()),model.getTime().getTimeUnit());
			time = time+ timespend;
			if(!g.type().equals("charging") && time<g.getStartWindow()){
				timespend = g.getStartWindow()-time+timespend;
				time=g.getStartWindow();
			}
			battery-= timespend;
			if(g.type().equals("charging")){
				double batterydiff = model.getMaxBattery()-battery;
				if(batterydiff/model.getChargeRate()==(long) (batterydiff/model.getChargeRate())){
					batterydiff=(long) (batterydiff/model.getChargeRate());
				}else{
					batterydiff=(long) (batterydiff/model.getChargeRate())+1;
				}
				time=(long) (time+Math.min(g.endWindow-time, batterydiff));
				battery = battery + Math.min(g.endWindow-time, batterydiff)*model.getChargeRate();
			}
			curcor =g.point;
			if(time<l){
				result.remove(g);
			}else{
				return result;
			}
		}
		return result;

	}
	/**
	 * check of a permutation of goals is better than a given plan
	 */
	@SuppressWarnings("unchecked")
	public ArrayList<Goal> GenerateBestPlan(ArrayList<Goal> copyGoals,
			ArrayList<Goal> newPlan, Goal charged, ArrayList<Goal> bestplan, ArrayList<TimeWindow> windows, Point temppos, long tempbat, long startTime) {
		if(copyGoals.size() ==0){
			bestplan = addCharging(newPlan, charged, bestplan,windows,  temppos, tempbat,startTime);
			return bestplan;
		}
		else{
			for(int i =0;i< copyGoals.size();i+=2){
				ArrayList<Goal> ccopyGoals = (ArrayList<Goal>) copyGoals.clone();
				ArrayList<Goal> cnewPlan = (ArrayList<Goal>) newPlan.clone();


				cnewPlan.add(ccopyGoals.remove(i));

				cnewPlan.add(ccopyGoals.remove(i));

				bestplan=GenerateBestPlan(ccopyGoals,cnewPlan,charged,bestplan, windows,  temppos, tempbat,startTime);
			}
			return bestplan;
		}
	}
	/**
	 * checks which place you best place charging.
	 */
	@SuppressWarnings("unchecked")
	private ArrayList<Goal> addCharging(ArrayList<Goal> newPlan, Goal charged,
			ArrayList<Goal> bestplan, ArrayList<TimeWindow> windows, Point temppos, long tempbat, long startTime) {
		if(valid(newPlan,windows, startTime, temppos,  tempbat)){
			if(bestplan == null) bestplan = (ArrayList<Goal>) newPlan.clone();
			else{
				if(value(newPlan,startTime,  temppos,  tempbat)>value(bestplan, startTime, temppos,  tempbat)) bestplan = (ArrayList<Goal>) newPlan.clone();
			}
		}
		for(int number=0;number<=newPlan.size();number++){
			if(charged==null){
				newPlan.add(number, new ChargeGoal(model.ChargingStation.getPosition().get(), "charging",new TimeWindow(0, Long.MAX_VALUE) ,false));
			}else{
				newPlan.add(number, charged);
			}
			if(valid(newPlan,windows, startTime, temppos,  tempbat)){
				if(bestplan == null) bestplan = (ArrayList<Goal>) newPlan.clone();
				else{
					if(value(newPlan, startTime, temppos,  tempbat)>value(bestplan, startTime, temppos,  tempbat)) bestplan = (ArrayList<Goal>) newPlan.clone();
				}
			}
			newPlan.remove(number);
		}
		return bestplan;
	}
	/**
	 * checks if a plan can be executed
	 */
	public boolean valid(ArrayList<Goal> newPlan, ArrayList<TimeWindow> windows, long startTime, Point temppos, double tempbat) {
		if(newPlan.size() == 0) return true;


		RoadUnits r = model.getRoadUnits();
		long time = startTime;
		double battery = tempbat;
		Point curcor = temppos;
		for(Goal g:newPlan){
			long timespend = (long) r.toExTime(r.toInDist(distance(curcor,g.coordinates()))/r.toInSpeed(model.getSpeed()),model.getTime().getTimeUnit());

			time = time+ timespend;
			if(time>g.getEndWindow())return false;
			if(!g.type().equals("charging") && time<g.getStartWindow()){
				timespend = g.getStartWindow()-time+timespend;
				time=g.getStartWindow();
			}
			battery-= timespend;
			if(battery <=0.1*model.getMaxBattery()) return false;
			if(g.type().equals("charging")){
				double batterydiff = model.getMaxBattery()-battery;
				long timestart = time;
				if(batterydiff/model.getChargeRate()==(long) (batterydiff/model.getChargeRate())){
					batterydiff=(long) (batterydiff/model.getChargeRate());
				}else{
					batterydiff=(long) (batterydiff/model.getChargeRate())+1;
				}
				time=(long) (time+Math.min(g.endWindow-time, batterydiff));
				battery = battery + Math.min(g.endWindow-time, batterydiff)*model.getChargeRate();
				if(time>g.endWindow)return false;
				if(!((ChargeGoal)g).isReserved()){
					if(!checkWindows(g,windows)) return false;
					g.setEndWindow(time);
					g.setStartWindow(timestart);					
				}

			}
			curcor =g.point;
		}
		double timespend = r.toExTime(r.toInDist(distance(newPlan.get(newPlan.size()-1).coordinates(),model.ChargingStation.getPosition().get()))/r.toInSpeed(model.getSpeed()),model.getTime().getTimeUnit());
		long timespend2;
		if(timespend == (long) timespend)timespend2 = (long) timespend;
		else{
			timespend2 = (long) timespend+1;
		}
		battery-= timespend2;
		if(battery <=0.1*model.getMaxBattery()) return false;
		return true;
	}
	/**
	 * checks if there is a timewindow in windows which contains the start and end time of a goal
	 */
	private boolean checkWindows(Goal g, ArrayList<TimeWindow> windows) {
		for(TimeWindow w:windows){
			if(w.isIn(g.getStartWindow()) && w.isIn(g.getEndWindow()))return true;
		}
		return false;
	}
	/**
	 * remove a goal from the plan
	 */
	public void remove(Goal g) {
		this.goals.remove(g);

	}
	/**
	 * get the next reachable goal
	 */
	public Goal getNextgoal() {
		while(this.goals != null && !this.goals.isEmpty() && this.goals.get(0).type().equals("pickup") && this.goals.get(0).getEndWindow() < model.calcTime(model.coordinates(), goals.get(0).coordinates())+ model.getTime().getTime()){
			this.goals.remove(0);
			for(Goal g:goals){
				if(g.type().equals("drop")){
					goals.remove(g);
					break;
				}
			}
		}
		if(this.goals == null || this.goals.isEmpty())return null;
		return this.goals.get(0);
	}

	public double value(ArrayList<Goal> plan, long startTime) {
		Point temppos = calculatePosition(startTime);
		long tempbat = calculateBattery(startTime);
		return value(plan,startTime,temppos,tempbat);
	}

	public ChargeGoal lostChargeGoal(Plan definitivebid) {
		for( Goal g : goals){
			if(g.type().equals("charging") && !definitivebid.getPlan().contains(g)) return (ChargeGoal) g;
		}
		return null;
	}





}
