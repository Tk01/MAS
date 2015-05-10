package robotPackage;

import world.Pack;

import com.github.rinde.rinsim.core.model.comm.CommUser;

public class DefAssignmentMessageContent extends ContractNetMessageContent {
	
	double bid;
	
	Pack packageToDel;
	
	boolean assigned;
	
	CommUser sender;
	
	public DefAssignmentMessageContent(CommUser sender, double bid, boolean assigned, Pack packageToDeliver,  int contractID){
		this.sender = sender;
		this.bid = bid;
		this.packageToDel = packageToDeliver;
		this.assigned = assigned;
		setType("PreAssignment");
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
	
	public boolean getAssigned(){
		return assigned;
	}


}
