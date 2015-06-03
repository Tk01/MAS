package world;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import robotPackage.Robot;

public class InformationHandler {
	private static InformationHandler handler;
	private int negotiation =0;
	private HashMap<Robot,Long> idleTimes = new HashMap<Robot,Long>();
	private HashMap<Robot,Long> FailedTimes= new HashMap<Robot,Long>();
	ArrayList<Package> packages = new ArrayList<Package>();
	private int lostcharge=0;
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
		for(long p:sortedValues(idleTimes)){
			line+=p+"("+((double)p/24d/60d/60d/1000d)+")"+", ";
		}
		writer.println(line);
		writer.println("#succesfull Negotiations: "+negotiation);
		writer.println("#succesfull swapped charges: "+lostcharge);
		writer.close();
	}
	public ArrayList<Long> sortedValues(HashMap<Robot, Long> idleTimes2) {
		ArrayList<Robot> r =new ArrayList<Robot>(idleTimes2.keySet());
		Collections.sort(r, new Comparator<Robot>(){

			@Override
			public int compare(Robot arg0, Robot arg1) {
				if(arg0.getStartLocation().x>arg1.getStartLocation().x)return 1;
				if(arg0.getStartLocation().x==arg1.getStartLocation().x && arg0.getStartLocation().y>arg1.getStartLocation().y)return 1;
				if(arg0.getStartLocation().x==arg1.getStartLocation().x && arg0.getStartLocation().y==arg1.getStartLocation().y)return 0;
				return -1;
			}
			
		});
		ArrayList<Long> result =new ArrayList<Long>();
		for(Robot key:r){
			result.add(idleTimes2.get(key));
		}
		return result;
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
		lostcharge=0;
		this.negotiation = 0;
		idleTimes = new HashMap<Robot,Long>();
		FailedTimes= new HashMap<Robot,Long>();
		packages = new ArrayList<Package>();
		
	}
	public int failed() {
		return FailedTimes.size();
	}
	public int failedPackages() {
		int i=0;
		for(Package p:packages){
			if(p.getStage() == 404) i++;
		}
		return i;
	}
	public HashMap<Robot, Long> getIdleTimes() {
		return this.idleTimes;
	}
	public void completedNegatiation() {
		negotiation++;
		
	}
	public int getNegotiations() {
		return negotiation;
	}
	public void setlostcharge() {
		lostcharge++;		
	}
	public int getlostcharge() {
		return lostcharge;		
	}
}
