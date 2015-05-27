package world;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;

import com.github.rinde.rinsim.geom.Point;

import robotPackage.Robot;

public class InformationHandler {
	private static InformationHandler handler;
	private long l=0;
	private HashMap<Robot,Long> map= new HashMap<Robot,Long>();
	ArrayList<Package> packages = new ArrayList<Package>();
	public static InformationHandler getInformationHandler(){
		if (handler ==null) handler = new InformationHandler();
		return handler;
		
	}
	public void finish(String string) throws FileNotFoundException, UnsupportedEncodingException {
		// TODO Auto-generated method stub
		PrintWriter writer = new PrintWriter(string, "UTF-8");
		String line="failed RobotTimes: ";
		for(long p:map.values()){
			line+=p+", ";
		}
		writer.println(line);
		int i=0;
		for(Package p:packages){
			if(p.getStage() == 404) i++;
		}
		writer.println("failed packages: "+i+";" +  (((double)i*100) / packages.size()) + "%;");
		writer.println("Total Idle Time: "+l+";"+ (((double)l*100) /4/24/60/60/1000) + "%;");
		writer.close();
	}
	public void addTime(long timeLeft) {
		// TODO Auto-generated method stub
		l+=timeLeft;
		
	}
	public void batteryEmpty(Robot robot, long time) {
		// TODO Auto-generated method stub
		if(!map.containsKey(robot))map.put(robot, time);
		
	}
	public void addPackage(Package p) {
		// TODO Auto-generated method stub
		packages.add(p);
	}
}
