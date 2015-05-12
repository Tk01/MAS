package robotPackage;

import world.Package;

public class DeliverPackage extends Goal{
	
	private Package packageToDel;
	
	public DeliverPackage(Package packageToDeliver){
		this.setPackageToDel(packageToDeliver);
	}

	public Package getPackageToDel() {
		return packageToDel;
	}

	public void setPackageToDel(Package packageToDel) {
		this.packageToDel = packageToDel;
	}
	
	
	
	
	
	
	
	

}
