package robotPackage;

import com.github.rinde.rinsim.core.model.comm.CommUser;


import world.Pack;


public class PreBidMessageContent extends ContractNetMessageContent{
	
	CommUser sender;
	
	double bid;
	
	Pack packageToDel;
	
	public PreBidMessageContent(CommUser sender, double bid, Pack packageToDeliver, int contractID){
		this.sender = sender;
		this.bid = bid;
		this.packageToDel = packageToDeliver;
		setType("BidMessage");
		setContractID(contractID);
		
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
