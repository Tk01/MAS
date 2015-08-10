package Messages;

import com.github.rinde.rinsim.core.model.comm.CommUser;

	abstract class ContractNetMessageContent extends MessageContent{
	
	int contractID;
	
	/**
	 * The general content for a contract net message. The message will have an ID so it is known for which package the message is.
	 * @param receiver: the receiver of the message
	 * @param string: the type of the message
	 */
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