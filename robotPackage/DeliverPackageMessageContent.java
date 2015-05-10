package robotPackage;

import world.Pack;

public class DeliverPackageMessageContent extends ContractNetMessageContent{
	
	

	Pack packageToDel;
	
	public DeliverPackageMessageContent(Pack packageToDeliver, int contractID){
	this.packageToDel = packageToDeliver;	
	setType("DeliverMessage");
	setContractID(contractID);
	
	}
	
	public Pack getPackageToDel() {
		return packageToDel;
	}

	public void setPackageToDel(Pack packageToDel) {
		this.packageToDel = packageToDel;
	}
	
	

}
