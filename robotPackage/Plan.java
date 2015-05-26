package robotPackage;

import java.util.ArrayList;

import world.Package;

import com.github.rinde.rinsim.core.model.road.RoadUnits;
import com.github.rinde.rinsim.geom.Point;
import com.github.rinde.rinsim.util.TimeWindow;

public class Plan {



	ArrayList <Goal> goals;

	Package bidPackage;
	WorldModel model;

	public Plan(ArrayList <Goal> goals, WorldModel model){
		this.goals =goals;

		this.model =model;
	}

	public ArrayList <Goal> getPlan(){
		return goals;
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
			else{ 
				if(goals.get(i).type().equals("pickup")){
					negotiationPlan.remove(goals.get(i+1));
					break;
				}
				else{
					break;
				}
			}
		}
		
		return negotiationPlan;
	}

	//Return the utility of the plan. Is used to compare tasks that are added to the current plan. The lower the better
	public double value(ArrayList<Goal> newPlan){
		if(newPlan.size() ==0) return 0;
		RoadUnits r = model.getRoadUnits();
		long time = model.getTime().getTime();
		double battery = model.battery();
		Point curcor = model.coordinates();
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
				if(batterydiff/5==(long) (batterydiff/5)){
					batterydiff=(long) (batterydiff/5);
				}else{
					batterydiff=(long) (batterydiff/5)+1;
				}
				time=(long) (time+Math.min(g.endWindow-time, batterydiff));
				battery = battery + Math.min(g.endWindow-time, batterydiff)*5;
				}
			curcor =g.point;
		}
		long totalTimeSpend = time-model.getTime().getTime();
		return totalTimeSpend+(1-battery)*r.toExTime(r.toInDist(distance(newPlan.get(newPlan.size()-1).coordinates(),model.ChargingStation.getPosition().get()))/r.toInSpeed(model.getSpeed()),model.getTime().getTimeUnit());
	}




	// check if the pack can be taken up in the plan by checking if in the specified timewindows it is possible to pick up the package.
	public Plan isPossiblePlan(Goal pickupGoal, Goal dropGoal, ArrayList<TimeWindow> windows){
		if(goals.size() > 7) return null;

		@SuppressWarnings("unchecked")
		ArrayList <Goal> copyGoals = (ArrayList<Goal>) goals.clone();
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

		return new Plan(GenerateBestPlan(copyGoals,newPlan,charged,null,windows),model);
	}




	private ArrayList<Goal> GenerateBestPlan(ArrayList<Goal> copyGoals,
			ArrayList<Goal> newPlan, Goal charged, ArrayList<Goal> bestplan, ArrayList<TimeWindow> windows) {
		if(copyGoals.size() ==0){
			bestplan = addCharging(newPlan, charged, bestplan,windows);
			return bestplan;
		}
		else{
			for(int i =0;i< copyGoals.size();i+=2){
				ArrayList<Goal> ccopyGoals = (ArrayList<Goal>) copyGoals.clone();
				ArrayList<Goal> cnewPlan = (ArrayList<Goal>) newPlan.clone();
				
				
					cnewPlan.add(ccopyGoals.remove(i));
				
				cnewPlan.add(ccopyGoals.remove(i));

				bestplan=GenerateBestPlan(ccopyGoals,cnewPlan,charged,bestplan, windows);
			}
			return bestplan;
		}
	}

	private ArrayList<Goal> addCharging(ArrayList<Goal> newPlan, Goal charged,
			ArrayList<Goal> bestplan, ArrayList<TimeWindow> windows) {
		if(valid(newPlan,windows)){
			if(bestplan == null) bestplan = (ArrayList<Goal>) newPlan.clone();
			else{
				if(value(newPlan)>value(bestplan)) bestplan = (ArrayList<Goal>) newPlan.clone();
			}
		}
		for(int number=0;number<=newPlan.size();number++){
			if(charged==null){
				newPlan.add(number, new ChargeGoal(model.ChargingStation.getPosition().get(), "charging",new TimeWindow(0, Long.MAX_VALUE) ,false));
			}else{
				newPlan.add(number, charged);
			}
			if(valid(newPlan,windows)){
				if(bestplan == null) bestplan = (ArrayList<Goal>) newPlan.clone();
				else{
					if(value(newPlan)>value(bestplan)) bestplan = (ArrayList<Goal>) newPlan.clone();
				}
			}
			newPlan.remove(number);
		}
		return bestplan;
	}
	private boolean valid(ArrayList<Goal> newPlan, ArrayList<TimeWindow> windows) {
		RoadUnits r = model.getRoadUnits();
		long time = model.getTime().getTime();
		double battery = model.battery();
		Point curcor = model.coordinates();
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
				if(batterydiff/5==(long) (batterydiff/5)){
					batterydiff=(long) (batterydiff/5);
				}else{
					batterydiff=(long) (batterydiff/5)+1;
				}
				time=(long) (time+Math.min(g.endWindow-time, batterydiff));
				battery = battery + Math.min(g.endWindow-time, batterydiff)*5;
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
	private boolean checkWindows(Goal g, ArrayList<TimeWindow> windows) {
		for(TimeWindow w:windows){
			if(w.isIn(g.getStartWindow()) && w.isIn(g.getEndWindow()))return true;
		}
		return false;
	}

	public void remove(Goal g) {
		this.goals.remove(g);

	}

	public Package getBidPackage() {
		return bidPackage;
	}
	public void setBidPackage(Package bidPackage) {
		this.bidPackage = bidPackage;
	}
	public Goal getNextgoal() {
		if(this.goals.isEmpty())return null;
		return this.goals.get(0);
	}
	
	public Plan returnPlanWithoutCharging(){
		ArrayList<Goal> goalsCopy = (ArrayList) goals.clone();
		for(int i=0;i<goalsCopy.size();i++){
			if(((Goal)goalsCopy.get(i)).type().equals("charging")){
				goalsCopy.remove(goalsCopy.get(i));
			}
			
		}
		return new Plan(goalsCopy,model);
	}

	



}
