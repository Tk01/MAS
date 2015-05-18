package robotPackage;


import com.github.rinde.rinsim.core.model.comm.CommUser;

public class NegotiationBidMessageContent extends MessageContent{
	

	private JPlan jointPlan;
	public NegotiationBidMessageContent(CommUser receiver, JPlan jointPlan){
		super(receiver,"NegotiationBidMessage");
		this.jointPlan=jointPlan;
	}


	public JPlan getJointPlan() {
		return jointPlan;
	}

	public void setJointPlan(JPlan jointPlan) {
		this.jointPlan = jointPlan;
	}




	

}
