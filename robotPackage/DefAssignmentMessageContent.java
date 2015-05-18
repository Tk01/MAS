package robotPackage;



import com.github.rinde.rinsim.core.model.comm.CommUser;

public class DefAssignmentMessageContent extends ContractNetMessageContent {
	
	double bid;
	

	
	boolean assigned;
	

	
	public DefAssignmentMessageContent(CommUser sender, double bid, boolean assigned, int contractID){
		super(sender,"DefAssignment");
		this.bid = bid;
		this.assigned = assigned;
		setContractID(contractID);
		
	}
	

	public double getBid(){
		return bid;
	}

	
	public boolean getAssigned(){
		return assigned;
	}


}
