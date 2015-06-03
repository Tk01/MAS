package robotPackage;


import com.github.rinde.rinsim.core.model.comm.CommUser;

public class NegotiationBidMessageContent extends NegotiationMessage{
	

	private JPlan jointPlan;
	
	/**
	 * This is the message used to do a bif for a negotiation
	 * @param receiver
	 * @param jointPlan
	 */
	public NegotiationBidMessageContent(CommUser receiver, JPlan jointPlan){
		super(MessageTypes.NegotiationBidMessage,receiver);
		this.jointPlan=jointPlan;
	}


	public JPlan getJointPlan() {
		return jointPlan;
	}

	public void setJointPlan(JPlan jointPlan) {
		this.jointPlan = jointPlan;
	}




	

}
