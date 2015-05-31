package robotPackage;

import java.util.ArrayList;

import com.github.rinde.rinsim.core.model.comm.CommUser;
import com.github.rinde.rinsim.geom.Point;


public class StartNegotiationMessageContent extends NegotiationMessage{

	
	private ArrayList<Goal> plan;
	private long endTime;
	private Point position;
	private long battery;
	private double minValue;
	


	
	public StartNegotiationMessageContent(CommUser receiver,Point pos,  ArrayList<Goal> plan,long battery, long endTime, double minValue){
		super(MessageTypes.StartNegotiationMessage,receiver);
		this.plan = plan;
		this.endTime = endTime;
		this.position = pos;
		this.battery = battery;
		this.minValue = minValue;
		
	}


	public ArrayList<Goal> getPlan() {
		return plan;
	}

	public void setPlan(ArrayList<Goal> plan) {
		this.plan = plan;
	}

	public long getEndTime() {
		return endTime;
	}


	public Point getPosition() {
		return position;
	}


	public long getBattery() {
		return battery;
	}
	
	public double getMinValue(){
		return minValue;
	}
	
	
	
	
	
	

}
