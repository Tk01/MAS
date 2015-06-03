package robotPackage;

import com.github.rinde.rinsim.core.model.comm.CommUser;

import world.Package;

public class DeliverPackageMessageContent extends ContractNetMessageContent{
	
	

	Package packageToDel;
	long endTime;

	
	
	/**
	 * The message to request the delivery of a package
	 * @param receiver: the package
	 * @param packageToDeliver: the package
	 * @param contractID
	 * @param endTime: when the bidding will end
	 */
	public DeliverPackageMessageContent(CommUser receiver,Package packageToDeliver, int contractID, long endTime){
	super(receiver,MessageTypes.DeliverMessage);
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
	
	public long getEndTime() {
		return endTime;
	}
	
	

}
