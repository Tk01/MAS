package robotPackage;



import com.github.rinde.rinsim.core.model.comm.CommUser;

public class DefBidMessageContent extends ContractNetMessageContent{
	

	
	double bid;
	
	
	public DefBidMessageContent(CommUser sender, double bid){
		super(sender,MessageTypes.DefBidMessage);
		this.bid = bid;

		
		
	}
	
	
	
	public double getBid(){
		return bid;
	}
	


}
