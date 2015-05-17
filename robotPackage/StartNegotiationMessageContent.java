package robotPackage;

public class StartNegotiationMessageContent extends MessageContent{
	
	Plan plan;
	
	public StartNegotiationMessageContent(Plan plan){
		this.plan = plan;
		
	}

	public Plan getPlan() {
		return plan;
	}

	public void setPlan(Plan plan) {
		this.plan = plan;
	}
	
	

}
