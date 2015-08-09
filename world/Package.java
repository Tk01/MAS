package world;


import java.util.ArrayList;















import Messages.DefAssignmentMessageContent;
import Messages.DefBidMessageContent;
import Messages.DeliverPackageMessageContent;
import Messages.MessageContent;
import Messages.MessageTypes;
import WorldModel.Robot;

import com.github.rinde.rinsim.core.TickListener;
import com.github.rinde.rinsim.core.TimeLapse;
import com.github.rinde.rinsim.core.model.comm.CommDevice;
import com.github.rinde.rinsim.core.model.comm.CommDeviceBuilder;
import com.github.rinde.rinsim.core.model.comm.CommUser;
import com.github.rinde.rinsim.core.model.comm.Message;
import com.github.rinde.rinsim.core.model.pdp.PDPModel;
import com.github.rinde.rinsim.core.model.pdp.Parcel;
import com.github.rinde.rinsim.core.model.pdp.Vehicle;
import com.github.rinde.rinsim.core.model.road.RoadModel;
import com.github.rinde.rinsim.geom.Point;
import com.github.rinde.rinsim.util.TimeWindow;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
	
	/**
	 * A customer with very permissive time windows.
	 */
	public class Package extends Parcel implements CommUser,TickListener {
		private PDPModel pdpModel;
		static int contractId =0;
		Long delay;
		Long timeLastAction;
		int mycontractId ;
		Point start;
		Point end;
		Optional<CommDevice> translator;
		private int stage =0;
		RoadModel roadModel;
		private boolean isDeliverd = false;
		public void deliver(){
			isDeliverd =true;
		}
		Package(Point startPosition, Point pDestination,
				long pLoadingDuration, long pUnloadingDuration, TimeWindow timeWindow, TimeWindow timeWindow2,long delay) {
			super(pDestination, pLoadingDuration, timeWindow,
					pUnloadingDuration, timeWindow2, 1);
			start =startPosition;
			setStartPosition(startPosition);
			end = pDestination;
			mycontractId=contractId;
			contractId++;
			this.delay= delay;
		}

		public void setStart(Point start) {
			this.start = start;
		}

		public void setEnd(Point end) {
			this.end = end;
		}

		@Override
		public void afterTick(TimeLapse arg0) {
			
		}
		/**
		 * implements a single tick.
		 */
		@Override
		public void tick(TimeLapse time) {
			// hard pickupwindow
			if(super.getPickupTimeWindow().isAfterEnd(time.getTime()) && !this.isDeliverd && this.isCarried() == null){
				this.pdpModel.unregister(this);
				this.roadModel.unregister(this);
				stage = 404;
			}
			// soft dropwindow
			if(super.getDeliveryTimeWindow().isAfterEnd(time.getTime()) && !this.isDeliverd){
				stage = 404;
			}
			// initialise contractnet
			if(stage ==0 && time.getTimeLeft()>0){
				this.translator.get().broadcast(new DeliverPackageMessageContent(null, this, mycontractId,time.getStartTime()+delay));
				stage++;
				timeLastAction=time.getStartTime();
				if(time.getEndTime()<timeLastAction+delay){
					time.consumeAll();
				}else{
					time.consume(timeLastAction+delay-1-time.getStartTime());
				}
			}
			ImmutableList<Message> list;
			// accept bids and select the best
			if(stage ==1 && time.getTimeLeft()>0){
				if(time.getEndTime()>=timeLastAction+delay){
					list = this.translator.get().getUnreadMessages();
					ArrayList<Message> DefBidList = new ArrayList<Message>();
					double bid = Double.MAX_VALUE;
					CommUser bestUser = null;
					for(Message m:list){
						if(((MessageContent) m.getContents()).getType() == MessageTypes.DefBidMessage){
							DefBidList.add( m);
							if(((DefBidMessageContent) m.getContents()).getBid()<bid){
								bid = ((DefBidMessageContent) m.getContents()).getBid();
								bestUser = m.getSender();
							}
						}
					}
					for(Message m:DefBidList){
						if(m.getSender()==bestUser){
							this.translator.get().send(new DefAssignmentMessageContent(bestUser, bid, true , mycontractId), m.getSender());
						}else{
							this.translator.get().send(new DefAssignmentMessageContent(bestUser, bid, false , mycontractId), m.getSender());
						}
					}
					if(!DefBidList.isEmpty()){
						stage++;
					}
					else{
						stage =0;
						}
					timeLastAction=time.getStartTime();
					if(time.getEndTime()<timeLastAction+delay){
						time.consumeAll();
					}else{
						time.consume(timeLastAction+delay-1-time.getStartTime());
					}
				}else{
					
					time.consumeAll();
				}
			}
		}

		@Override
		public Optional<Point> getPosition() {
			if(stage == 404){
				if(this.isCarried() !=null)return Optional.of(roadModel.getPosition(this.isCarried() ));
				return Optional.absent();
			}
			if(stage ==2) {
				if(this.isCarried() !=null)return Optional.of(roadModel.getPosition(this.isCarried() ));
				if(roadModel.containsObject(this))return Optional.of(roadModel.getPosition(this));
				else{
					return Optional.absent();
				}
			}
			return Optional.of(roadModel.getPosition(this));
			
		}

		private Vehicle isCarried() {
			for(Robot user: roadModel.getObjectsOfType(Robot.class))
				if(pdpModel.containerContains(user, this))return user;
			return null;
		}

		@Override
		public void setCommDevice(CommDeviceBuilder builder) {
			builder.setMaxRange(20);

			translator = Optional.of(builder
					.setReliability(1)
					.build()); 
			
		}

		@Override
		public void initRoadPDP(RoadModel arg0, PDPModel arg1) {
			this.roadModel=arg0;
			this.pdpModel = arg1;
		}
		
		public Point getStart (){
			return start;
		}
		
		public Point getEnd(){
			return end;
			
		}
		
		public Long getDelay() {
			return delay;
		}

		public void setDelay(Long delay) {
			this.delay = delay;
		}

		public Long getTimeLastAction() {
			return timeLastAction;
		}

		public void setTimeLastAction(Long timeLastAction) {
			this.timeLastAction = timeLastAction;
		}
		public Boolean canBeDelivered(Vehicle v, Long time){
			return super.canBeDelivered(v, time);
		}
		public Boolean canBePickedUp(Vehicle v, Long t){
			return super.getPickupTimeWindow().isIn(t);
			
		}

		public int getStage() {
			return stage;
		}
		

	}

