package robotPackage;

import java.util.ArrayList;

import world.Package;

import com.github.rinde.rinsim.geom.Point;

public class Plan {
	static Integer maxi=0; 

	Integer i;
	ArrayList <Goal> goals;
	
	Package bidPackage;

	
	public Plan(ArrayList <Goal> goals, Package p){
		this.goals =goals;
		i=maxi;
		maxi++;
	}
	public Integer getId(){
		return i;
		
	}
	public ArrayList <Goal> getPlan(){
		return goals;
	}
	
	public double distanceBetween(Goal theGoal,Goal nextGoal ){
		
		
	
		double startX = theGoal.coordinates().x;
		double startY = theGoal.coordinates().y;
		
		double endX = nextGoal.coordinates().x;
		double endY = nextGoal.coordinates().y;
				
		
		double xd = endX-startX;
		double yd = endY- startY;
		double distance = Math.sqrt(xd*xd + yd*yd);
		
		return distance;
		
		
	}
	
	//Return the utility of the plan. Is used to compare tasks that are added to the current plan. The lower the better
	public double getPlanValue(){
		double value=0;
		double totalDistance = 0;
		double lostTime=0;
		
		for(int i=0; i<goals.size()-1;i++){
			totaldistance  = distanceBetween(goals.get(i),goals.get(i+1);
			
			if(goals.get(i))
			
			
		}
		return value;
	}
	

	
	
	// check if the pack can be taken up in the plan by checking if in the specified timewindows it is possible to pick up the package.
	public boolean isPossiblePlan(int order){
		boolean possible = false;
		if(packages.size() > 0){
			if(order>0){
				
				if(possibleSequence(packages.get(order-1),packages.get(order))){
					if(possibleSequence(packages.get(order), packages.get(order+1))){
						possible = true;
					}
				}
				
			}
			if(order == 0){
				if(possibleSequence(packages.get(order), packages.get(order+1))){
					possible = true;
				}
				
			}
			
			if(order == packages.size()){
				if(possibleSequence(packages.get(order-1),packages.get(order))){
					possible = true;
				}
				
			}
		}
		else{
			possible = true;
		}
		
		return possible;
		
		
	}
	
	// check if the first package can be picked up before the second package considering the timewindows and the distance between the packages
	private boolean possibleSequence(Package firstPack, Package secondPack){
		boolean possible = false;
		double distance  = getdistanceWithNextPackage(firstPack, secondPack);
		long endWindowFirst= firstPack.getPickupTimeWindow().end;
		long beginWindowSecond = secondPack.getPickupTimeWindow().begin;
		
		
		if(distance<beginWindowSecond-endWindowFirst){
			possible = true;
		}
		
		return possible;
	}

	public void remove(Goal g) {
		// TODO Auto-generated method stub
		
	}
	
	public Package getBidPackage() {
		return bidPackage;
	}
	public void setBidPackage(Package bidPackage) {
		this.bidPackage = bidPackage;
	}
	
	

}
