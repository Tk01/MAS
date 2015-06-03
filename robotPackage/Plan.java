package robotPackage;

import java.util.ArrayList;

import com.github.rinde.rinsim.geom.Point;
import com.github.rinde.rinsim.util.TimeWindow;

public class Plan {



	private ArrayList <Goal> goals;


	private WorldModel model;


	private static double limit;
	public static void setLimit(double d){
		limit = d;
	}
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

	/**
	 * Return the time you're not moving packages of the plan. Is used to compare tasks that are added to the current plan. The lower the better
	 */
	public static double value(ArrayList<Goal> newPlan, long planTime, Point temppos, long tempbat,WorldModel model){
		long time = planTime;
		double battery = tempbat;
		Point curcor = temppos;
		if(newPlan ==null || newPlan.size() ==0) return ((model.getMaxBattery()-battery)/model.getMaxBattery())*model.calcTime(curcor,model.getChargingStation().getPosition().get());
		for(Goal g:newPlan){
			long timespend = model.calcTime(curcor,g.coordinates());
			time = time+ timespend;
			if(g.type() != GoalTypes.Charging && time<g.getStartWindow()){
				timespend = g.getStartWindow()-time+timespend;
				time=g.getStartWindow();
			}
			battery-= timespend;
			if(g.type() == GoalTypes.Charging){
				double batterydiff = model.getMaxBattery()-battery;
				if(batterydiff/model.getChargeRate()==(long) (batterydiff/model.getChargeRate())){
					batterydiff=(long) (batterydiff/model.getChargeRate());
				}else{
					batterydiff=(long) (batterydiff/model.getChargeRate())+1;
				}
				time=(long) (time+Math.min(g.getEndWindow()-time, batterydiff));
				battery = battery + Math.min(g.getEndWindow()-time, batterydiff)*model.getChargeRate();
			}
			curcor =g.coordinates();
		}
		long totalTimeSpend = time-model.getTime().getTime();
		return totalTimeSpend-TimeDelivering(newPlan,temppos, model)+((model.getMaxBattery()-battery)/model.getMaxBattery())*model.calcTime(curcor,model.getChargingStation().getPosition().get());
	}



	/**
	 * Calculates the time you are moving packages
	 */
	private static long TimeDelivering(ArrayList<Goal> newPlan, Point temppos,WorldModel model) {
		@SuppressWarnings("unchecked")
		ArrayList<Goal> newPlan2 = (ArrayList<Goal>) newPlan.clone();
		for(Goal g:newPlan2){
			if(g.type() == GoalTypes.Charging){
				newPlan2.remove(g);
				break;
			}
		}
		int h=0;
		long val=0;
		if(newPlan2.size() ==0)return 0;
		if(newPlan2.get(0).type() == GoalTypes.Drop){
			val = model.calcTime(temppos, newPlan2.get(0).coordinates());
			h++;
		}
		for(int i =h;i<newPlan2.size();i+=2){
			val += model.calcTime(newPlan2.get(i).coordinates(), newPlan2.get(i+1).coordinates()); 
		}
		return val;
	}

	/**
	 *Creates the best plan with the goals in goals,pickupGoal and dropGoal. null if such does not exist.
	 */
	public Plan isPossiblePlan(Goal pickupGoal, Goal dropGoal, ArrayList<TimeWindow> windows,long startTime){

		ArrayList<Goal> tempgoals = calculateGoals(startTime);
		Point temppos = calculatePosition(startTime);
		long tempbat = calculateBattery(startTime);

		@SuppressWarnings("unchecked")
		ArrayList <Goal> copyGoals = (ArrayList<Goal>) tempgoals.clone();
		copyGoals.add(pickupGoal);
		copyGoals.add(dropGoal);
		Goal charged = null;
		for(int i=0; i<copyGoals.size();i++){
			if(copyGoals.get(i).type() == GoalTypes.Charging){
				charged = copyGoals.remove(i);
				break;
			}
		}

		ArrayList<Goal> newPlan = new ArrayList<Goal>();
		if(copyGoals.size()>0 && copyGoals.get(0).type() == GoalTypes.Drop ){

			newPlan.add(copyGoals.remove(0));
		}
		ArrayList<Goal> result = GenerateBestPlan(copyGoals,newPlan,(ChargeGoal) charged,null,windows,temppos,tempbat,startTime, model);
		return new Plan(result,model);
	}



	/**
	 * Calculates how much battery you have at time l according to the plan
	 */
	public long calculateBattery(long l) {
		if(goals ==null) return model.battery();
		long time = model.getTime().getTime();
		long battery = model.battery();
		Point curcor = model.coordinates();
		for(Goal g:goals){
			long timespend = model.calcTime(curcor,g.coordinates());
			time = time+ timespend;

			if(g.type() != GoalTypes.Charging && time<g.getStartWindow()){
				timespend = g.getStartWindow()-time+timespend;
				time=g.getStartWindow();
			}
			if(time>= l){
				return  (battery- l+(time- timespend));
			}
			battery-= timespend;
			if(g.type() == GoalTypes.Charging){
				double batterydiff = model.getMaxBattery()-battery;
				if(batterydiff/model.getChargeRate()==(long) (batterydiff/model.getChargeRate())){
					batterydiff=(long) (batterydiff/model.getChargeRate());
				}else{
					batterydiff=(long) (batterydiff/model.getChargeRate())+1;
				}
				time=(long) (time+Math.min(g.getEndWindow()-time, batterydiff));
				battery = (long) (battery + Math.min(g.getEndWindow()-time, batterydiff)*model.getChargeRate());
				if(time>= l){
					return (long) (battery+ model.getChargeRate()*(l-(time-Math.min(g.getEndWindow()-time, batterydiff))) -Math.min(g.getEndWindow()-time, batterydiff)*model.getChargeRate());
				}
			}
			curcor =g.coordinates();

		}
		return battery;

	}
	/**
	 * Calculates which position you have at time l according to the plan
	 */
	public Point calculatePosition(long l) {
		if(goals == null) return model.coordinates();
		long time = model.getTime().getTime();
		double battery = model.battery();
		Point curcor = model.coordinates();
		
		for(Goal g:goals){
			long timespend =model.calcTime(curcor,g.coordinates());
			
			time = time+ timespend;
			if(time>= l){
				return new Point(curcor.x+(g.coordinates().x-curcor.x)*(l-(time-timespend))/timespend,curcor.y+(g.coordinates().y-curcor.y)*(l-(time-timespend))/timespend);
			}
			if(g.type() !=GoalTypes.Charging && time<g.getStartWindow()){
				timespend = g.getStartWindow()-time+timespend;
				time=g.getStartWindow();
			}
			battery-= timespend;
			if(g.type() == GoalTypes.Charging){
				double batterydiff = model.getMaxBattery()-battery;
				if(batterydiff/model.getChargeRate()==(long) (batterydiff/model.getChargeRate())){
					batterydiff=(long) (batterydiff/model.getChargeRate());
				}else{
					batterydiff=(long) (batterydiff/model.getChargeRate())+1;
				}
				time=(long) (time+Math.min(g.getEndWindow()-time, batterydiff));
				battery = battery + Math.min(g.getEndWindow()-time, batterydiff)*model.getChargeRate();
			}
			curcor =g.coordinates();
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
		
		if(this.goals == null) return new ArrayList<Goal>();
		@SuppressWarnings("unchecked")
		ArrayList<Goal> result = (ArrayList<Goal>) goals.clone();
		long time = model.getTime().getTime();
		double battery = model.battery();
		Point curcor = model.coordinates();
		for(Goal g:goals){
			long timespend =model.calcTime(curcor,g.coordinates());
			
			time = time+ timespend;
			if(g.type() !=GoalTypes.Charging && time<g.getStartWindow()){
				timespend = g.getStartWindow()-time+timespend;
				time=g.getStartWindow();
			}
			battery-= timespend;
			if(g.type() == GoalTypes.Charging){
				double batterydiff = model.getMaxBattery()-battery;
				if(batterydiff/model.getChargeRate()==(long) (batterydiff/model.getChargeRate())){
					batterydiff=(long) (batterydiff/model.getChargeRate());
				}else{
					batterydiff=(long) (batterydiff/model.getChargeRate())+1;
				}
				time=(long) (time+Math.min(g.getEndWindow()-time, batterydiff));
				battery = battery + Math.min(g.getEndWindow()-time, batterydiff)*model.getChargeRate();
			}
			curcor =g.coordinates();
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
	public static ArrayList<Goal> GenerateBestPlan(ArrayList<Goal> copyGoals,
			ArrayList<Goal> newPlan, ChargeGoal charged, ArrayList<Goal> bestplan, ArrayList<TimeWindow> windows, Point temppos, long tempbat, long startTime, WorldModel model) {
		if(copyGoals.size() ==0){
			if(charged !=null)bestplan = addCharging(newPlan, charged.clone(), bestplan,windows,  temppos, tempbat,startTime, model);
			bestplan = addCharging(newPlan, null, bestplan,windows,  temppos, tempbat,startTime, model);
			return bestplan;
		}
		else{
			for(int i =0;i< copyGoals.size();i+=2){
				ArrayList<Goal> ccopyGoals = (ArrayList<Goal>) copyGoals.clone();
				ArrayList<Goal> cnewPlan = (ArrayList<Goal>) newPlan.clone();


				cnewPlan.add(ccopyGoals.remove(i));

				cnewPlan.add(ccopyGoals.remove(i));

				bestplan=GenerateBestPlan(ccopyGoals,cnewPlan,charged,bestplan, windows,  temppos, tempbat,startTime, model);
			}
			return bestplan;
		}
	}
	/**
	 * checks which place you best place charging.
	 */
	@SuppressWarnings("unchecked")
	private static ArrayList<Goal> addCharging(ArrayList<Goal> newPlan, ChargeGoal charged,
			ArrayList<Goal> bestplan, ArrayList<TimeWindow> windows, Point temppos, long tempbat, long startTime,WorldModel model) {
		if(valid(newPlan,windows, startTime, temppos,  tempbat, model)){
			if(bestplan == null) bestplan = (ArrayList<Goal>) newPlan.clone();
			else{
				if(value(newPlan,startTime,  temppos,  tempbat, model)>value(bestplan, startTime, temppos,  tempbat, model)) bestplan = (ArrayList<Goal>) newPlan.clone();
			}
		}
		for(int number=0;number<=newPlan.size();number++){
			if(charged==null){
				newPlan.add(number, new ChargeGoal(model.getChargingStation().getPosition().get(), new TimeWindow(0, Long.MAX_VALUE) ,false));
			}else{
				newPlan.add(number, charged);
			}
			if(valid(newPlan,windows, startTime, temppos,  tempbat, model)){
				if(bestplan == null) bestplan = (ArrayList<Goal>) newPlan.clone();
				else{
					if(value(newPlan, startTime, temppos,  tempbat, model)>value(bestplan, startTime, temppos,  tempbat, model)) bestplan = (ArrayList<Goal>) newPlan.clone();
				}
			}
			newPlan.remove(number);
		}
		return bestplan;
	}
	/**
	 * checks if a plan can be executed
	 */
	private static boolean valid(ArrayList<Goal> newPlan, ArrayList<TimeWindow> windows, long startTime, Point temppos, long tempbat,WorldModel model) {
		if(newPlan.size() == 0) return true;
		long time = startTime;
		long battery =  tempbat;
		Point curcor = temppos;
		for(Goal g:newPlan){
			long timespend = model.calcTime(curcor,g.coordinates());
			time = time+ timespend;
			if(time>g.getEndWindow())return false;
			if(g.type() != GoalTypes.Charging && time<g.getStartWindow()){
				timespend = g.getStartWindow()-time+timespend;
				time=g.getStartWindow();
			}
			battery-= timespend;
			if(battery <=limit*model.getMaxBattery()) return false;
			if(g.type() == GoalTypes.Charging){
				long batterydiff = model.getMaxBattery()-battery;
				if(batterydiff/model.getChargeRate()==(long) (batterydiff/model.getChargeRate())){
					batterydiff=(long) (batterydiff/model.getChargeRate());
				}else{
					batterydiff=(long) (batterydiff/model.getChargeRate())+1;
				}
				TimeWindow timewindow = findBestTimeWindow(time,battery,batterydiff,newPlan,(ChargeGoal) g,windows, model);
				if(timewindow== null)return false;
				time=timewindow.end;
				battery = battery + (timewindow.end-timewindow.begin)*model.getChargeRate();
				if(!((ChargeGoal)g).isReserved()){
					g.setEndWindow(timewindow.end);
					g.setStartWindow(timewindow.begin);					
				}

			}
			curcor =g.coordinates();
		}
		double timespend = model.calcTime(newPlan.get(newPlan.size()-1).coordinates(),model.getChargingStation().getPosition().get());
		long timespend2;
		if(timespend == (long) timespend)timespend2 = (long) timespend;
		else{
			timespend2 = (long) timespend+1;
		}
		battery-= timespend2;
		if(battery <=limit*model.getMaxBattery()) return false;
		return true;
	}
	/**
	 * find the best time window for a ChargeGoal
	 */
	private static TimeWindow findBestTimeWindow(long time, long battery, long batterydiff, ArrayList<Goal> goals2,
			ChargeGoal g, ArrayList<TimeWindow> windows,WorldModel model) {
		if(g.isReserved())return new TimeWindow(g.getStartWindow(),g.getEndWindow());
		TimeWindow best =null;
		if(goals2.indexOf(g) == goals2.size()-1){
			for(TimeWindow w:windows){
				if(w.begin>=time ){
					if( w.begin >= time+battery)break;
					long startTime = Math.max(w.begin,time);
					if(w.end>=batterydiff+startTime) return new TimeWindow(startTime,batterydiff+startTime);
					if(best == null || best.length() < w.end - startTime) best = new TimeWindow(startTime,w.end);
				}
			}
			return best;
		}else{
			Goal g2 = goals2.get(goals2.indexOf(g)+1);
			for(TimeWindow w:windows){
				long end2ndgoal = g2.getEndWindow()+model.calcTime(model.getChargingStation().getPosition().get(), g2.coordinates());
				if(w.begin>=time ){
					if( w.begin >= time+battery || w.begin >= end2ndgoal)break;
					long startTime = Math.max(w.begin,time);
					long endTime = Math.min(end2ndgoal,w.end);
					if(endTime>=batterydiff+startTime) return new TimeWindow(startTime,batterydiff+startTime);
					if(best == null || best.length() < endTime - startTime) best = new TimeWindow(startTime,endTime);
				}
			}
		}
		return best;
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
		
		if(this.goals == null || this.goals.isEmpty())return null;
		return this.goals.get(0);
	}
	/**
	 * calculate the value of  plan if it was placed at startTime
	 */
	public double value(ArrayList<Goal> plan, long startTime) {
		Point temppos = calculatePosition(startTime);
		long tempbat = calculateBattery(startTime);
		return value(plan,startTime,temppos,tempbat, model);
	}
	/**
	 * returns the first chargeGoal in goals that isn't in arrayList
	 */
	public ChargeGoal lostChargeGoal(ArrayList<Goal> arrayList) {
		if(goals == null) return null;
		for( Goal g : goals){
			if(g.type() == GoalTypes.Charging && !arrayList.contains(g)){
				return (ChargeGoal) g;
			}
		}
		return null;
	}

	public String toString(){
		return goals.toString();
	}
	public static double getLimit() {
		return limit;
	}



}
