package robotPackage;


import com.github.rinde.rinsim.core.model.comm.CommUser;

public class NegotiationBidMessageContent extends NegotiationMessage{
	

	private JPlan jointPlan;
	long endTime;
	
	
	public NegotiationBidMessageContent(CommUser receiver, JPlan jointPlan, long endTime){
		super("NegotiationBidMessage",receiver);
		this.jointPlan=jointPlan;
		this.endTime = endTime;
	}


	public JPlan getJointPlan() {
		return jointPlan;
	}

	public void setJointPlan(JPlan jointPlan) {
		this.jointPlan = jointPlan;
	}


	public long getEndTime() {
		return endTime;
	}
	
	




	

}
