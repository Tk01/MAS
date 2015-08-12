package WorldModel;

import com.github.rinde.rinsim.core.model.comm.CommUser;


import world.Package;

public class Bid {
	
	private CommUser bidSender;
	private Package packageToDeliver;
	private double value;
	
	public Bid(CommUser bidSender,Package pack, double value ){
		this.bidSender = bidSender;
		this.packageToDeliver = pack;
		this.value= value;
		
	}

	public Bid(double value) {
		// TODO Auto-generated constructor stub
	}

	public CommUser getBidSender() {
		return bidSender;
	}

	public Package getPackageToDeliver() {
		return packageToDeliver;
	}

	public double getValue() {
		return value;
	}
	
	
	

}
