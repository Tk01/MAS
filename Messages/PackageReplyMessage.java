package Messages;

import com.github.rinde.rinsim.core.model.comm.CommUser;

public class PackageReplyMessage extends ContractNetMessageContent {
	
	private boolean accepted;
	
	public PackageReplyMessage(CommUser sender,boolean accepted){
		super(sender,MessageTypes.PackageReplyMessage);
		this.accepted = accepted;
		
	}

	public boolean isAccepted() {
		return accepted;
	}
	
	

}
