package robotPackage;

import com.github.rinde.rinsim.core.model.comm.CommUser;


public class ChargeMessageContent extends MessageContent {
	private long start;
	

	private long end;
	private String mesType;

	public ChargeMessageContent(CommUser receiver,long start, long end, String mesType){
		super(receiver,MessageTypes.ChargeMessage);
		this.start = start;
		this.end = end;
		this.mesType = mesType;
	}
	public long getStart() {
		return start;
	}

	public long getEnd() {
		return end;
	}

	public String getMesType() {
		return mesType;
	}
}
