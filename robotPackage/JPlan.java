package robotPackage;

import java.util.ArrayList;

import com.github.rinde.rinsim.core.model.comm.CommUser;

public class JPlan {
	
	Plan ownPlan;
	Plan otherPlan;
	
	double ownValue;
	double otherValue;
	
	int negotiationID;
	
	CommUser JPlanAgent;
	
	public JPlan(){
		
	}
	
	public JPlan(Plan ownPlan, Plan otherPlan){
		this.ownPlan = ownPlan;
		this.otherPlan = otherPlan;
		
	}

	
	
	public double getOwnValue() {
		return ownValue;
	}



	public void setOwnValue(double ownValue) {
		this.ownValue = ownValue;
	}



	public double getOtherValue() {
		return otherValue;
	}



	public void setOtherValue(double otherValue) {
		this.otherValue = otherValue;
	}



	public Plan getOwnPlan() {
		return ownPlan;
	}

	public void setOwnPlan(Plan ownPlan) {
		this.ownPlan = ownPlan;
	}

	public Plan getOtherPlan() {
		return otherPlan;
	}

	public void setOtherPlan(Plan otherPlan) {
		this.otherPlan = otherPlan;
	}

	public int getNegotiationID() {
		return negotiationID;
	}

	public void setNegotiationID(int negotiationID) {
		this.negotiationID = negotiationID;
	}

	public CommUser getJPlanAgent() {
		return JPlanAgent;
	}

	public void setJPlanAgent(CommUser jPlanAgent) {
		JPlanAgent = jPlanAgent;
	}
	
	
	
	
	
	
}
