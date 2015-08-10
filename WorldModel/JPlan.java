package WorldModel;

import java.util.ArrayList;

import com.github.rinde.rinsim.core.model.comm.CommUser;

public class JPlan {
	
	ArrayList<Goal> ownPlan;
	ArrayList<Goal> otherPlan;
	
	double ownValue;
	double otherValue;
	
	int negotiationID;
	
	CommUser JPlanAgent;
	
	public JPlan(){
		
	}
	/**
	 * The jointplan consists of 2 sets of goals which will be used for negotiation
	 * @param ownPlan
	 * @param otherPlan
	 */
	public JPlan(ArrayList<Goal> ownPlan, ArrayList<Goal> otherPlan){
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



	public ArrayList<Goal> getOwnPlan() {
		return ownPlan;
	}

	public void setOwnPlan(ArrayList<Goal> ownPlan) {
		this.ownPlan = ownPlan;
	}

	public ArrayList<Goal> getOtherPlan() {
		return otherPlan;
	}

	public void setOtherPlan(ArrayList<Goal> otherPlan) {
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
