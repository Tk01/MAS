package robotPackage;

public class NegotiationBidMessageContent extends MessageContent{
	
	private boolean accepted;
	
	public NegotiationBidMessageContent(){
		
	}

	public boolean isAccepted() {
		return accepted;
	}

	public void setAccepted(boolean accepted) {
		this.accepted = accepted;
	}
	
	

}
