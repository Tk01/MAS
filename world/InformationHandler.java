package world;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;

import robotPackage.Robot;

public class InformationHandler {
	private static InformationHandler handler;
	private HashMap<Robot,Long> idleTimes = new HashMap<Robot,Long>();
	private HashMap<Robot,Long> FailedTimes= new HashMap<Robot,Long>();
	ArrayList<Package> packages = new ArrayList<Package>();
	public static InformationHandler getInformationHandler(){
		if (handler ==null) handler = new InformationHandler();
		return handler;
		
	}
	public void finish(String string) throws FileNotFoundException, UnsupportedEncodingException {
		PrintWriter writer = new PrintWriter(string, "UTF-8");
		String line="failed RobotTimes: ";
		for(long p:FailedTimes.values()){
			line+=p+", ";
		}
		writer.println(line);
		int i=0;
		for(Package p:packages){
			if(p.getStage() == 404) i++;
		}
		writer.println("failed packages: "+i+";" +  (((double)i*100) / packages.size()) + "%;");
		line="Idle Times: ";
		for(long p:idleTimes.values()){
			line+=p+"("+((double)p/24d/60d/60d/1000d)+")"+", ";
		}
		writer.println(line);
		writer.close();
	}
	public void addTime(long timeLeft,Robot robot) {
		if(!idleTimes.containsKey(robot)) idleTimes.put(robot, timeLeft);
		else{
			idleTimes.put(robot,idleTimes.get(robot)+timeLeft);
		}
		
	}
	public void batteryEmpty(Robot robot, long time) {
		if(!FailedTimes.containsKey(robot))FailedTimes.put(robot, time);
		
	}
	public void addPackage(Package p) {
		packages.add(p);
	}
	public void clear() {
		idleTimes = new HashMap<Robot,Long>();
		FailedTimes= new HashMap<Robot,Long>();
		packages = new ArrayList<Package>();
		
	}
}
