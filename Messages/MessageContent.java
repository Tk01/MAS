package Messages;

import com.github.rinde.rinsim.core.model.comm.CommUser;
import com.github.rinde.rinsim.core.model.comm.MessageContents;

public class MessageContent implements MessageContents{
	
	private MessageTypes type;
	private CommUser user;
	
	/**
	 * The messagecontent will
	 * @param user
	 * @param type
	 */
	public MessageContent(CommUser user, MessageTypes type){
		this.user = user;
		this.type=type;
	}
	
	public MessageTypes getType(){
		return type;
	}
	
	public void setType(MessageTypes type){
		this.type = type;
	}

	public CommUser getUser() {
		return user;
	}
	
	

}