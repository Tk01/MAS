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
	
	public double getdistanceWithNextPackage(int order){
		
		Pack thePackage = packages.get(order);
		Pack nextPackage = packages.get(order+1);
		
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
	
	//Return the utility of the plan. Is used to compare tasks that are added to the current plan.
	public double getPlanValue(){
		double value=0;
		for(int i=0; i<packages.size()-1;i++){
			value = value + getdistanceWithNextPackage(i);
		}
		return value;
	}
	
	public ArrayList<Pack> getPlan(){
		return packages;
	}
	
	

}
