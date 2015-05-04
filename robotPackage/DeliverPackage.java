package robotPackage;

import world.Pack;

public class DeliverPackage extends Goal{
	
	private Pack packageToDel;
	
	public DeliverPackage(Pack packageToDeliver){
		this.setPackageToDel(packageToDeliver);
	}

	public Pack getPackageToDel() {
		return packageToDel;
	}

	public void setPackageToDel(Pack packageToDel) {
		this.packageToDel = packageToDel;
	}
	
	
	
	
	
	
	
	

}
