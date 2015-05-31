package robotPackage;

import com.github.rinde.rinsim.core.model.comm.CommUser;

	abstract class ContractNetMessageContent extends MessageContent{
	
	int contractID;
	
	public ContractNetMessageContent(CommUser receiver, MessageTypes string){
		super(receiver,string);
		
	}

	public int getContractID() {
		return contractID;
	}

	public void setContractID(int contractID) {
		this.contractID = contractID;
	}

}
