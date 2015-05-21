package robotPackage;

import com.github.rinde.rinsim.core.model.comm.CommUser;





public class PreBidMessageContent extends ContractNetMessageContent{
	

	
	double bid;

	
	

	public PreBidMessageContent(CommUser sender, double bid, int contractID){
		super(sender,"PreBidMessage");
		this.bid = bid;

		setContractID(contractID);
		
	}
	

	
	public double getBid(){
		return bid;
	}



	
	

	
	

}
