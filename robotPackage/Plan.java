package robotPackage;

import java.util.ArrayList;

import world.Package;

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
		if(newPlan.size()==0)return 0;
		long startTime = model.getTime().getTime();
		long time = model.getTime().getTime();
		double battery = model.battery();
		for(Goal g:newPlan){
			double timespend = distance(model.coordinates(),g.coordinates())/model.getSpeed()/1000;
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
			battery-= timespend*WorldInterface.SpendingRate;
			if(g.type().equals("charging")){
				double batterydiff = 1-battery;
				battery =1;
				if(batterydiff/5/WorldInterface.SpendingRate==(long) (batterydiff/5/WorldInterface.SpendingRate)){
					time=time+(long) (batterydiff/5/WorldInterface.SpendingRate);
				}else{
					time=time+(long) (batterydiff/5/WorldInterface.SpendingRate)+1;
				}
			}
		}
		long totalTimeSpend = time-startTime;
		return totalTimeSpend+(1-battery)*distance(newPlan.get(newPlan.size()-1).coordinates(),model.coordinates());
	}




	// check if the pack can be taken up in the plan by checking if in the specified timewindows it is possible to pick up the package.
	public Plan isPossiblePlan(Package p){
		if(goals.size() > 5) return null;
		ArrayList<Goal> bestPlan = null;
		@SuppressWarnings("unchecked")
		ArrayList <Goal> copyGoals = (ArrayList<Goal>) goals.clone();
		copyGoals.add(new Goal(p.getStart(), "pickup", p.getPickupTimeWindow()));
		copyGoals.add(new Goal(p.getEnd(), "drop", p.getDeliveryTimeWindow()));
		Goal charged = null;
		for(int i=0; i<copyGoals.size();i++){
			if(copyGoals.get(i).type.equals("charging")){
				charged = copyGoals.remove(i);
				break;
			}
		}
		
		ArrayList<Goal> newPlan = new ArrayList<Goal>();
		if(goals.size()>0 && goals.get(0).type().equals("drop") ){
			
			newPlan.add(copyGoals.remove(0));
		}
		
		return new Plan(GenerateBestPlan(copyGoals,newPlan,charged,null),model);
	}
	
	


	private ArrayList<Goal> GenerateBestPlan(ArrayList<Goal> copyGoals,
			ArrayList<Goal> newPlan, Goal charged, ArrayList<Goal> bestplan) {
		if(copyGoals.size() ==0){
			if(valid(newPlan)){
				if(bestplan == null) bestplan = newPlan;
				else{
					if(value(newPlan)>value(bestplan)) bestplan = newPlan;
				}
			}
			for(int l=0;l<newPlan.size()+1;l++){
				if(charged==null){
					newPlan.add(l, new ChargeGoal(model.ChargingStation.getPosition().get(), "charging",new TimeWindow(0, Long.MAX_VALUE) ,false));
				}else{
					newPlan.add(l, charged);
				}
				
				if(valid(newPlan)){
					if(bestplan == null) bestplan = newPlan;
					else{
						if(value(newPlan)>value(bestplan)) bestplan = newPlan;
					}
				}
				newPlan.remove(l);
			}
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
	private boolean valid(ArrayList<Goal> newPlan) {
		
		long time = model.getTime().getTime();
		double battery = model.battery();
		for(Goal g:newPlan){
			double timespend = distance(model.coordinates(),g.coordinates())/model.getSpeed()/1000;
			long timespend2;
			if(timespend == (long) timespend)timespend2 = (long) timespend;
			else{
				timespend2 = (long) timespend+1;
			}
			time = time+ timespend2;
			if(time>g.getEndWindow())return false;
			if(time<g.getStartWindow()){
				timespend = g.getStartWindow()-time+timespend;
				time=g.getStartWindow();
			}
			battery-= timespend*WorldInterface.SpendingRate;
			if(battery <=0) return false;
			if(g.type().equals("charging")){
				double batterydiff = 1-battery;
				long timestart = time;
				battery =1;
				if(batterydiff/5/WorldInterface.SpendingRate==(long) (batterydiff/5/WorldInterface.SpendingRate)){
					time=time+(long) (batterydiff/5/WorldInterface.SpendingRate);
				}else{
					time=time+(long) (batterydiff/5/WorldInterface.SpendingRate)+1;
				}
				if(time>g.endWindow)return false;
				if(!((ChargeGoal)g).isReserved()){
					g.setEndWindow(time);
					g.setStartWindow(timestart);					
				}
				
			}
		}
		double timespend = distance(newPlan.get(newPlan.size()-1).coordinates(),model.coordinates())/model.getSpeed()/1000;
		long timespend2;
		if(timespend == (long) timespend)timespend2 = (long) timespend;
		else{
			timespend2 = (long) timespend+1;
		}
		battery-= timespend2*WorldInterface.SpendingRate;
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
