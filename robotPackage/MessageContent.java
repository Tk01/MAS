package robotPackage;

import com.github.rinde.rinsim.core.model.comm.CommUser;
import com.github.rinde.rinsim.core.model.comm.MessageContents;

public class MessageContent implements MessageContents{
	
	private String type;
	private CommUser user;
	public MessageContent(CommUser user, String type){
		this.user = user;
		this.type=type;
	}
	
	public String getType(){
		return type;
	}
	
	public void setType(String type){
		this.type = type;
	}

	public CommUser getUser() {
		return user;
	}
	
	

}
