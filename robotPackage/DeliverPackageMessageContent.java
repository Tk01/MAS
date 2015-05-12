package robotPackage;

import world.Package;

public class DeliverPackageMessageContent extends ContractNetMessageContent{
	
	

	Package packageToDel;
	

	public DeliverPackageMessageContent(Pack packageToDeliver, int contractID){

	this.packageToDel = packageToDeliver;	
	setType("DeliverMessage");
	setContractID(contractID);
	
	}
	
	public Package getPackageToDel() {
		return packageToDel;
	}

	public void setPackageToDel(Package packageToDel) {
		this.packageToDel = packageToDel;
	}
	
	

}
