package robotPackage;

public class NegotiationReplyMessageContent extends NegotiationMessage{
	
private boolean accepted;
	
	public NegotiationReplyMessageContent(){
		
	}

	public boolean isAccepted() {
		return accepted;
	}

	public void setAccepted(boolean accepted) {
		this.accepted = accepted;
	}

}
