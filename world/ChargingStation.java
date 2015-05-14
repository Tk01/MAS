package world;

import java.util.ArrayList;

import robotPackage.ChargeMessageContent;
import robotPackage.MessageContent;

import com.github.rinde.rinsim.core.TickListener;
import com.github.rinde.rinsim.core.TimeLapse;
import com.github.rinde.rinsim.core.model.comm.CommDevice;
import com.github.rinde.rinsim.core.model.comm.CommDeviceBuilder;
import com.github.rinde.rinsim.core.model.comm.CommUser;
import com.github.rinde.rinsim.core.model.comm.Message;
import com.github.rinde.rinsim.geom.Point;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

public class ChargingStation implements CommUser,TickListener{
	ArrayList<Long[]> schedule = new ArrayList<Long[]>();
	Point pos;
	Optional<CommDevice> device;
	public ChargingStation(Point p) {
		// TODO Auto-generated constructor stub
		pos = p;
	}
	@Override
	public void tick(TimeLapse timeLapse) {
		ImmutableList<Message> list = this.device.get().getUnreadMessages();
		for (Message message : list){
			MessageContent m = (MessageContent) message.getContents();
			if(m.getType().equals("ChargeMessage")){
				ChargeMessageContent l =(ChargeMessageContent) m;
				if(l.getMesType().equals("delete")) this.delete(l.getStart(),l.getEnd());
			}
		}
		for (Message message : list){
			MessageContent m = (MessageContent) message.getContents();
			if(m.getType().equals("ChargeMessage")){
				ChargeMessageContent l =(ChargeMessageContent) m;
				if(l.getMesType().equals("reserve")) this.reserve(l.getStart(),l.getEnd(),message.getSender());
			}
		}
		for (Message message : list){
			MessageContent m = (MessageContent) message.getContents();
			if(m.getType().equals("ChargeMessage")){
				ChargeMessageContent l =(ChargeMessageContent) m;
				if(l.getMesType().equals("check")) this.check(l.getStart(),l.getEnd(),message.getSender());
			}
		}
		
	}
	private void delete(long start, long end) {
		for( Long[] slot:schedule){
			if(start== slot[0] & end== slot[1]){
				schedule.remove(slot);
				return;
			}
		}
		
	}
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
		this.device.get().send(new ReturnChargeContents(start,end,b,true), sender);
		
	}
	private void checkM(long start, long end, boolean b, CommUser sender) {
		this.device.get().send(new ReturnChargeContents(start,end,b,false), sender);
		
	}

	@Override
	public void afterTick(TimeLapse timeLapse) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public Optional<Point> getPosition() {
		// TODO Auto-generated method stub
		return Optional.of(pos);
	}
	@Override
	public void setCommDevice(CommDeviceBuilder builder) {
		builder.setMaxRange(10);

		device = Optional.of(builder
				.setReliability(1)
				.build());
		
	}

	
}
