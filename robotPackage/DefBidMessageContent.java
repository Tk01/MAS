package robotPackage;



import com.github.rinde.rinsim.core.model.comm.CommUser;

public class DefBidMessageContent extends ContractNetMessageContent{
	

	
	double bid;
	
	/**
	 * When a drone send a bid to a package
	 * @param sender: the package
	 * @param bid: the bid
	 */
	public DefBidMessageContent(CommUser sender, double bid){
		super(sender,MessageTypes.DefBidMessage);
		this.bid = bid;

		
		
	}
	
	
	
	public double getBid(){
		return bid;
	}
	


}
