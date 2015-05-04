package robotPackage;

import com.github.rinde.rinsim.core.model.comm.Message;
import com.github.rinde.rinsim.core.model.comm.MessageContents;

public class Message implements Message{
	
	int ID;
	String sender;
	String receiver;
	String type;
	MessageContent content;
	
	public Message(int ID, String sender, String receiver, String type, Activity content){
		this.ID = ID;
		this.sender = sender;
		this.receiver = receiver;
		this.type = type;
		this.content = content;
	}
	

}
