package robotPackage;

import com.github.rinde.rinsim.core.model.comm.CommUser;

public class NegotiationBidMessageContent extends MessageContent{
	
	private boolean accepted;
	private JPlan jointPlan;
	public NegotiationBidMessageContent(CommUser receiver, JPlan jointPlan){
		super(receiver,"NegotiationBidMessage");
		this.jointPlan=jointPlan;
	}

	public boolean isAccepted() {
		return accepted;
	}

	public void setAccepted(boolean accepted) {
		this.accepted = accepted;
	}

	public JPlan getJointPlan() {
		return jointPlan;
	}


	

}
