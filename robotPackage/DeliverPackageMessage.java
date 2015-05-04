package robotPackage;

import world.Pack;

public class DeliverPackageMessage extends MessageContent{
	
	

	Pack packageToDel;
	
	public DeliverPackageMessage(Pack packageToDeliver){
	this.packageToDel = packageToDeliver;	
	}
	
	public Pack getPackageToDel() {
		return packageToDel;
	}

	public void setPackageToDel(Pack packageToDel) {
		this.packageToDel = packageToDel;
	}
	
	

}
