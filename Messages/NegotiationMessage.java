package Messages;

import com.github.rinde.rinsim.core.model.comm.CommUser;

abstract class NegotiationMessage extends MessageContent {
	
	int negotiationID;
	
	public NegotiationMessage(MessageTypes type, CommUser receiver){
		super(receiver,type);
		
	}

	public int getNegotiationID() {
		return negotiationID;
	}

	public void setNegotiationID(int negotiationID) {
		this.negotiationID = negotiationID;
	}

}
