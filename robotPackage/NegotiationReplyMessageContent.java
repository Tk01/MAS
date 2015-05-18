package robotPackage;

import com.github.rinde.rinsim.core.model.comm.CommUser;

public class NegotiationReplyMessageContent extends NegotiationMessage{
	
private boolean accepted;
	
	public NegotiationReplyMessageContent(String type, CommUser receiver){
		super(type,receiver);
	}

	public boolean isAccepted() {
		return accepted;
	}

	public void setAccepted(boolean accepted) {
		this.accepted = accepted;
	}

}
