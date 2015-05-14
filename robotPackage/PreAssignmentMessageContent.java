package robotPackage;

import world.Package;

import com.github.rinde.rinsim.core.model.comm.CommUser;

public class PreAssignmentMessageContent extends ContractNetMessageContent{
	
CommUser sender;
	
	double bid;
	
	Package packageToDel;
	
	boolean assigned;
	
	public PreAssignmentMessageContent(CommUser sender, double bid, boolean assigned, Package packageToDeliver,int contractID){
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
	
	public Package getPackge(){
		return packageToDel;
	}
	
	public boolean getAssigned(){
		return assigned;
	}

}
