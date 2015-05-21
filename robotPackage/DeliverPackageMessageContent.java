package robotPackage;

import com.github.rinde.rinsim.core.model.comm.CommUser;

import world.Package;

public class DeliverPackageMessageContent extends ContractNetMessageContent{
	
	

	Package packageToDel;
	long endTime;

	public long getEndTime() {
		return endTime;
	}

	public DeliverPackageMessageContent(CommUser receiver,Package packageToDeliver, int contractID, long endTime){
	super(receiver,"DeliverMessage");
	this.packageToDel = packageToDeliver;	
	setContractID(contractID);
	this.endTime = endTime;
	}
	
	public Package getPackageToDel() {
		return packageToDel;
	}

	public void setPackageToDel(Package packageToDel) {
		this.packageToDel = packageToDel;
	}
	
	

}
