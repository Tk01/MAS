package robotPackage;

import com.github.rinde.rinsim.core.model.comm.CommUser;

public class StartNegotiationMessageContent extends MessageContent{
	
	Plan plan;
	
	public StartNegotiationMessageContent(CommUser receiver, Plan plan){
		super(receiver,"StartNegotiationMessage");
		this.plan = plan;
		
	}

	public Plan getPlan() {
		return plan;
	}

	public void setPlan(Plan plan) {
		this.plan = plan;
	}
	
	

}
