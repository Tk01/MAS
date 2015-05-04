package robotPackage;

import com.github.rinde.rinsim.core.model.comm.MessageContents;

import world.Pack;


public class BidMessage extends MessageContent{
	
	String rec;
	
	double bid;
	
	Pack packageToDel;
	
	public BidMessage(String receiver, double bid, Pack packageToDeliver){
		rec = receiver;
		this.bid = bid;
		this.packageToDel = packageToDeliver;
		
	}
	
	public String getReceiver(){
		return rec;
	}
	
	public double getBid(){
		return bid;
	}
	
	public Pack getPackge(){
		return packageToDel;
	}
	
	

}
