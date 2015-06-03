package robotPackage;

import com.github.rinde.rinsim.core.model.comm.CommUser;


public class ChargeMessageContent extends MessageContent {
	private long start;
	

	private long end;
	private String mesType;
	
	/**
	 * This is the content of a charge message.
	 * @param receiver: the receiver of the message
	 * @param start: the start of the charging
	 * @param end: the end of the charge window
	 * @param mesType: the type of the message
	 */
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
