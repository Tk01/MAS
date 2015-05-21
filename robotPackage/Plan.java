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

	//Return the utility of the plan. Is used to compare tasks that are added to the current plan. The lower the better
	public double value(ArrayList<Goal> newPlan){
		RoadUnits r = model.getRoadUnits();
		if(newPlan.size()==0)return 0;
		long startTime = model.getTime().getTime();
		long time = model.getTime().getTime();
		double battery = model.battery();
		Point curcor = model.coordinates();
		for(Goal g:newPlan){
			double timespend = r.toExTime(r.toInDist(distance(curcor,g.coordinates()))/r.toInSpeed(model.getSpeed()),model.getTime().getTimeUnit());
			long timespend2;
			if(timespend == (long) timespend)timespend2 = (long) timespend;
			else{
				timespend2 = (long) timespend+1;
			}
			time = time+ timespend2;
			if(time<g.getStartWindow()){
				timespend = g.getStartWindow()-time+timespend;
				time=g.getStartWindow();
			}
			battery-= timespend;
			if(g.type().equals("charging")){
				double batterydiff = 1-battery;
				battery =1;
				if(batterydiff/5==(long) (batterydiff/5)){
					time=time+(long) (batterydiff/5);
				}else{
					time=time+(long) (batterydiff/5)+1;
				}
			}
			curcor =g.point;
		}
		long totalTimeSpend = time-startTime;
		return totalTimeSpend+(1-battery)*r.toExTime(r.toInDist(distance(newPlan.get(newPlan.size()-1).coordinates(),model.ChargingStation.getPosition().get()))/r.toInSpeed(model.getSpeed()),model.getTime().getTimeUnit());
	}




	// check if the pack can be taken up in the plan by checking if in the specified timewindows it is possible to pick up the package.
	public Plan isPossiblePlan(Goal pickupGoal, Goal dropGoal){
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

		return new Plan(GenerateBestPlan(copyGoals,newPlan,charged,null),model);
	}




	private ArrayList<Goal> GenerateBestPlan(ArrayList<Goal> copyGoals,
			ArrayList<Goal> newPlan, Goal charged, ArrayList<Goal> bestplan) {
		if(copyGoals.size() ==0){
			bestplan = addCharging(newPlan, charged, bestplan);
			return bestplan;
		}
		else{
			for(int i =0;i< copyGoals.size();i+=2){
				ArrayList<Goal> ccopyGoals = (ArrayList<Goal>) copyGoals.clone();
				ArrayList<Goal> cnewPlan = (ArrayList<Goal>) newPlan.clone();
				cnewPlan.add(ccopyGoals.remove(i));
				cnewPlan.add(ccopyGoals.remove(i));

				bestplan=GenerateBestPlan(ccopyGoals,cnewPlan,charged,bestplan);
			}
			return bestplan;
		}
	}

	private ArrayList<Goal> addCharging(ArrayList<Goal> newPlan, Goal charged,
			ArrayList<Goal> bestplan) {
		if(valid(newPlan)){
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
			if(valid(newPlan)){
				if(bestplan == null) bestplan = (ArrayList<Goal>) newPlan.clone();
				else{
					if(value(newPlan)>value(bestplan)) bestplan = (ArrayList<Goal>) newPlan.clone();
				}
			}
			newPlan.remove(number);
		}
		return bestplan;
	}
	private boolean valid(ArrayList<Goal> newPlan) {
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
			if(battery <=0) return false;
			if(g.type().equals("charging")){
				double batterydiff = 1-battery;
				long timestart = time;
				battery =1;
				if(batterydiff/5==(long) (batterydiff/5)){
					time=time+(long) (batterydiff/5);
				}else{
					time=time+(long) (batterydiff/5)+1;
				}
				if(time>g.endWindow)return false;
				if(!((ChargeGoal)g).isReserved()){
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
		if(battery <=0) return false;
		return true;
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



}
