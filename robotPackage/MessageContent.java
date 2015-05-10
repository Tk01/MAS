package robotPackage;

import com.github.rinde.rinsim.core.model.comm.MessageContents;

public class MessageContent implements MessageContents{
	
	private String type;
	
	public MessageContent(){
		
	}
	
	public String getType(){
		return type;
	}
	
	public void setType(String type){
		this.type = type;
	}
	
	

}
