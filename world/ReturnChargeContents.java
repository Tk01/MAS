package world;

import robotPackage.MessageContent;

import com.github.rinde.rinsim.core.model.comm.CommUser;
import com.github.rinde.rinsim.core.model.comm.MessageContents;

public class ReturnChargeContents extends MessageContent implements
MessageContents {
	private long start;
	private boolean succeeded;

	private long end;
	private boolean reserved;

	public ReturnChargeContents(CommUser receiver ,long start, long end,boolean succeeded, boolean reserved){
		super(receiver,"ChargeMessage");
		this.start = start;
		this.end = end;
		this.reserved = reserved;
		this.succeeded = succeeded;
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
