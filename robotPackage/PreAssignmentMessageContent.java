package robotPackage;

import world.Package;

import com.github.rinde.rinsim.core.model.comm.CommUser;

public class PreAssignmentMessageContent extends ContractNetMessageContent{
	
CommUser sender;
	
	double bid;
	
	Package packageToDel;
	
	boolean assigned;

	private long endTime;
	
	public PreAssignmentMessageContent(CommUser sender, double bid, boolean assigned, Package packageToDeliver,int contractID, long endTime){
		super(sender,"PreAssignment");
		this.bid = bid;
		this.packageToDel = packageToDeliver;
		this.assigned = assigned;
		setContractID(contractID);
		this.endTime =endTime;
		
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

	public long getEndTime() {
		// TODO Auto-generated method stub
		return endTime;
	}

}
