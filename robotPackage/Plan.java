package robotPackage;

import java.util.ArrayList;

import com.github.rinde.rinsim.geom.Point;

public class Plan {
	
	ArrayList <Package> packages;
	
	public Plan(){
		packages = new ArrayList();
	}
	
	public void addPackage (Package package, int order){
		packages.add(order, package);
		
	}
	
	public double getdistanceWithNextPackage(int order){
		
		Package thePackage = packages.get(order);
		Package nextPackage = packages.get(order+1);
		
		Point start = thePackage.getStart();
		Point end = nextPackage.getEnd();
		
		double startX = start.x;
		double startY = start.y;
		
		double endX = end.x;
		double endY = end.y;
				
		
		double xd = endX-startX;
		double yd = endY- startY;
		double distance = SquareRoot(xd*xd + yd*yd);
		
		return distance;
		
		
	}
	
	

}
