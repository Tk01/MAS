package robotPackage;

public class NegotiationBidMessageContent extends NegotiationMessage{
	
	private JPlan jointPlan;
	
	public NegotiationBidMessageContent(){
		
	}

	public JPlan getJointPlan() {
		return jointPlan;
	}

	public void setJointPlan(JPlan jointPlan) {
		this.jointPlan = jointPlan;
	}

	
	
	

}
