package world;

import robotPackage.MessageContent;

import com.github.rinde.rinsim.core.model.comm.MessageContents;

public class ReturnChargeContents extends MessageContent implements
MessageContents {
	private long start;
	private boolean succeeded;

	private long end;
	private boolean reserved;

	public ReturnChargeContents(long start, long end,boolean succeeded, boolean reserved){
		super();
		this.start = start;
		this.end = end;
		this.reserved = reserved;
		this.succeeded = succeeded;
		this.setType("ChargeMessage");
	}
	public long getStart() {
		return start;
	}

	public long getEnd() {
		return end;
	}

	public boolean isReserved() {
		return reserved;
	}
	public boolean hasSucceeded() {
		return succeeded;
	}
}
