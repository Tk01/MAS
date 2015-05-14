package robotPackage;


public class ChargeMessageContent extends MessageContent {
	private long start;
	

	private long end;
	private String mesType;

	public ChargeMessageContent(long start, long end, String mesType){
		super();
		this.start = start;
		this.end = end;
		this.mesType = mesType;
		this.setType("ChargeMessage");
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
