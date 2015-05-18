package robotPackage;

import com.github.rinde.rinsim.core.model.comm.CommUser;

public class NegotiationMessage extends MessageContent {
	
	int negotiationID;
	
	public NegotiationMessage(String type, CommUser receiver){
		super(receiver,type);
		
	}

	public int getNegotiationID() {
		return negotiationID;
	}

	public void setNegotiationID(int negotiationID) {
		this.negotiationID = negotiationID;
	}

}
