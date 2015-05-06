package robotPackage;

public class PBCKB {
	
	Plan currentPlan;
	
	Plan provisionalPlan;
	
	public PBCKB(){
		currentPlan = new Plan();
		provisionalPlan = new Plan();
		
	}

	public Plan getCurrentPlan() {
		return currentPlan;
	}

	public void setCurrentPlan(Plan currentPlan) {
		this.currentPlan = currentPlan;
	}

	public Plan getProvisionalPlan() {
		return provisionalPlan;
	}

	public void setProvisionalPlan(Plan provisionalPlan) {
		this.provisionalPlan = provisionalPlan;
	}
	
	

}
