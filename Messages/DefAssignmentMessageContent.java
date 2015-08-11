package Messages;



import com.github.rinde.rinsim.core.model.comm.CommUser;

public class DefAssignmentMessageContent extends ContractNetMessageContent {
	
	double bid;
	

	
	public boolean assigned;
	

	/**
	 * The message which is send to all drones who made a bid on a package. The message will contain if the bid has been accepted
	 * @param sender: the sneder of the message
	 * @param bid: the bid
	 * @param assigned: a boolean if the bid has been accepted
	 * @param contractID
	 */
	public DefAssignmentMessageContent(CommUser sender, double bid, boolean assigned, int contractID){
		super(sender,MessageTypes.DefAssignmentMessage);
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