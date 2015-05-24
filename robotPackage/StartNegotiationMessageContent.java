package robotPackage;

import com.github.rinde.rinsim.core.model.comm.CommUser;


public class StartNegotiationMessageContent extends NegotiationMessage{

	
	Plan plan;
	long endTime;
	


	
	public StartNegotiationMessageContent(CommUser receiver, Plan plan, long endTime){
		super("StartNegotiation",receiver);
		this.plan = plan;
		this.endTime = endTime;
		
	}


	public Plan getPlan() {
		return plan;
	}

	public void setPlan(Plan plan) {
		this.plan = plan;
	}

	public long getEndTime() {
		return endTime;
	}
	
	
	
	

}
