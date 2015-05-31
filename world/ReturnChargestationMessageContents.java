package world;

import java.util.ArrayList;

import robotPackage.MessageContent;
import robotPackage.MessageTypes;

import com.github.rinde.rinsim.core.model.comm.CommUser;
import com.github.rinde.rinsim.core.model.comm.MessageContents;
import com.github.rinde.rinsim.util.TimeWindow;

public class ReturnChargestationMessageContents extends MessageContent implements
MessageContents {
	private long start;
	private boolean succeeded;

	private long end;
	private boolean reserved;
	private ArrayList<TimeWindow> freeSlots;

	public ReturnChargestationMessageContents(CommUser receiver ,long start, long end,boolean succeeded, boolean reserved,ArrayList<TimeWindow> freeSlots){
		super(receiver,MessageTypes.ReturnChargestationMessage);
		this.start = start;
		this.end = end;
		this.reserved = reserved;
		this.succeeded = succeeded;
		this.freeSlots = freeSlots;
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
	public ArrayList<TimeWindow> getFreeSlots() {
		// TODO Auto-generated method stub
		return freeSlots;
	}
}
