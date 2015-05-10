package robotPackage;

import world.Package;

public class DeliverPackageMessage extends MessageContent{
	
	

	Package packageToDel;
	
	public DeliverPackageMessage(Package packageToDeliver){
	this.packageToDel = packageToDeliver;	
	}
	
	public Package getPackageToDel() {
		return packageToDel;
	}

	public void setPackageToDel(Package packageToDel) {
		this.packageToDel = packageToDel;
	}
	
	

}
