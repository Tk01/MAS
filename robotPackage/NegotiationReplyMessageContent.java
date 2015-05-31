package robotPackage;

import com.github.rinde.rinsim.core.model.comm.CommUser;

public class NegotiationReplyMessageContent extends NegotiationMessage{
	
private boolean accepted;
	
	public NegotiationReplyMessageContent( CommUser receiver, boolean accepted){
		super(MessageTypes.NegotiationReplyMessage,receiver);
		this.accepted = accepted;
	}

	public boolean isAccepted() {
		return accepted;
	}

	public void setAccepted(boolean accepted) {
		this.accepted = accepted;
	}

}
