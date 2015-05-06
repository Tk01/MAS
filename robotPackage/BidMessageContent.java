package robotPackage;

import com.github.rinde.rinsim.core.model.comm.CommUser;
import com.github.rinde.rinsim.core.model.comm.MessageContents;

import world.Pack;


public class BidMessageContent extends MessageContent{
	
	CommUser sender;
	
	double bid;
	
	Pack packageToDel;
	
	public BidMessageContent(CommUser sender, double bid, Pack packageToDeliver){
		this.sender = sender;
		this.bid = bid;
		this.packageToDel = packageToDeliver;
		
	}
	
	public CommUser getReceiver(){
		return sender;
	}
	
	public double getBid(){
		return bid;
	}
	
	public Pack getPackge(){
		return packageToDel;
	}
	
	

}
