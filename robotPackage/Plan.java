package robotPackage;

import java.util.ArrayList;

import world.Pack;

import com.github.rinde.rinsim.geom.Point;

public class Plan {
	
	ArrayList <Pack> packages;
	
	public Plan(){
		packages = new ArrayList();
	}
	
	public void addPackage (Pack pack, int order){
		packages.add(order, pack);
		
	}
	
	public double getdistanceWithNextPackage(Pack thePackage,Pack nextPackage ){
		
		
		
		Point start = thePackage.getStart();
		Point end = nextPackage.getEnd();
		
		double startX = start.x;
		double startY = start.y;
		
		double endX = end.x;
		double endY = end.y;
				
		
		double xd = endX-startX;
		double yd = endY- startY;
		double distance = Math.sqrt(xd*xd + yd*yd);
		
		return distance;
		
		
	}
	
	//Return the utility of the plan. Is used to compare tasks that are added to the current plan. The lower the better
	public double getPlanValue(){
		double value=0;
		for(int i=0; i<packages.size()-1;i++){
			getdistanceWithNextPackage(packages.get(i), packages.get(i+1));
		}
		return value;
	}
	
	public ArrayList<Pack> getPlan(){
		return packages;
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
	private boolean possibleSequence(Pack firstPack, Pack secondPack){
		boolean possible = false;
		double distance  = getdistanceWithNextPackage(firstPack, secondPack);
		long endWindowFirst= firstPack.getPickupTimeWindow().end;
		long beginWindowSecond = secondPack.getPickupTimeWindow().begin;
		
		
		if(distance<beginWindowSecond-endWindowFirst){
			possible = true;
		}
		
		return possible;
	}
	
	

}
