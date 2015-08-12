package world;

import java.util.ArrayList;

import Messages.ChargeMessageContent;
import Messages.MessageContent;
import Messages.MessageTypes;
import Messages.ReturnChargestationMessageContents;

import com.github.rinde.rinsim.core.TickListener;
import com.github.rinde.rinsim.core.TimeLapse;
import com.github.rinde.rinsim.core.model.comm.CommDevice;
import com.github.rinde.rinsim.core.model.comm.CommDeviceBuilder;
import com.github.rinde.rinsim.core.model.comm.CommUser;
import com.github.rinde.rinsim.core.model.comm.Message;
import com.github.rinde.rinsim.core.model.pdp.Depot;
import com.github.rinde.rinsim.core.model.pdp.PDPModel;
import com.github.rinde.rinsim.core.model.road.RoadModel;
import com.github.rinde.rinsim.geom.Point;
import com.github.rinde.rinsim.util.TimeWindow;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

public class ChargingStation extends Depot implements CommUser,TickListener{
	ArrayList<Long[]> schedule = new ArrayList<Long[]>();
	Point pos;
	Optional<CommDevice> device;
	private TimeLapse timeLapse;
	public ChargingStation(Point p) {
		setStartPosition(p);
		pos = p;
	}
	/**
	 * implement a tick
	 */
	@Override
	public void tick(TimeLapse timeLapse) {
		this.timeLapse = timeLapse;
		ImmutableList<Message> list = this.device.get().getUnreadMessages();
		//handle the delete messages
		for (Message message : list){
			MessageContent m = (MessageContent) message.getContents();
			if(m.getType() == MessageTypes.ChargeMessage){
				ChargeMessageContent l =(ChargeMessageContent) m;
				if(l.getMesType().equals("delete")) this.delete(l.getStart(),l.getEnd());
			}
		}
		//handle the reservation messages
		for (Message message : list){
			MessageContent m = (MessageContent) message.getContents();
			if(m.getType() == MessageTypes.ChargeMessage){
				ChargeMessageContent l =(ChargeMessageContent) m;
				if(l.getMesType().equals("reserve")) this.reserve(l.getStart(),l.getEnd(),message.getSender());
			}
		}
		//handle the check messages
		for (Message message : list){
			MessageContent m = (MessageContent) message.getContents();
			if(m.getType() == MessageTypes.ChargeMessage){
				ChargeMessageContent l =(ChargeMessageContent) m;
				if(l.getMesType().equals("check")) this.check(l.getStart(),l.getEnd(),message.getSender());
			}
		}
		
	}
	/**
	 * delete a reserved slot.
	 */
	private void delete(long start, long end) {
		for( Long[] slot:schedule){
			if(start== slot[0] & end== slot[1]){
				schedule.remove(slot);
				return;
			}
		}
		
	}
	/**
	 * check if a slot is open for reservation and send a message to the sender with the result.
	 */
	private void check(long start, long end, CommUser sender) {
		if(start>=end){
			checkM(start,end,false,sender);
			return;
		}
		for( Long[] slot:schedule){
			if( start<= slot[0] & end>= slot[0]){
				checkM(start,end,false,sender);
				return;
			}
			if( start >= slot[0] & start<= slot[1]){
				checkM(start,end,false,sender);
				return;
			}
		}
		checkM(start,end,true,sender);
	}
	/**
	 * try to reserve a slot and send a message to the sender with the result.
	 */
	private void reserve(long start, long end, CommUser sender) {
		if(start>=end){
			reserveM(start,end,false,sender);
			return;
		}
		for( Long[] slot:schedule){
			if( start<= slot[0] & end>= slot[0]){
				reserveM(start,end,false,sender);
				return;
			}
			if( start<= slot[1] & end>= slot[1]){
				reserveM(start,end,false,sender);
				return;
			}
		}
		reserveM(start,end,true,sender);
		this.schedule.add(new Long[]{start,end});
	}
	private void reserveM(long start, long end, boolean b, CommUser sender) {
		this.device.get().send(new ReturnChargestationMessageContents(sender,start,end,b,true,getFreeSlots()), sender);
		
	}

	private void checkM(long start, long end, boolean b, CommUser sender) {
		this.device.get().send(new ReturnChargestationMessageContents(sender,start,end,b,false,getFreeSlots()), sender);
		
	}

	@Override
	public void afterTick(TimeLapse timeLapse) {
		
	}
	@Override
	public Optional<Point> getPosition() {
		return Optional.of(pos);
	}
	@Override
	public void setCommDevice(CommDeviceBuilder builder) {
		builder.setMaxRange(10);

		device = Optional.of(builder
				.setReliability(1)
				.build());
		
	}
	@Override
	public void initRoadPDP(RoadModel pRoadModel, PDPModel pPdpModel) {
		
	}
	/**
	 * find the timewindows which aren't reserved by robots.
	 */
	private ArrayList<TimeWindow> getFreeSlots() {
		ArrayList<TimeWindow> freeSlots = new ArrayList<TimeWindow>();
		freeSlots.add(new TimeWindow(timeLapse.getStartTime(),Long.MAX_VALUE));
		for(Long[] slot:schedule){
			for(TimeWindow freeslot:freeSlots){
				if(freeslot.isIn(slot[0]) && freeslot.isBeforeEnd(timeLapse.getStartTime()) ){
					freeSlots.remove(freeslot);
					TimeWindow w1 = new TimeWindow(Math.max(timeLapse.getStartTime(),freeslot.begin),slot[0]);
					TimeWindow w2 = new TimeWindow(slot[1],freeslot.end);
					if(w1.length() !=0)freeSlots.add(w1);
					if(w2.length() !=0)freeSlots.add(w2);
					break;
				}
			}
		}
		return freeSlots;
	}
	
}
