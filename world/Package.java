package world;


import java.util.ArrayList;







import robotPackage.Robot;
import robotPackage.DefAssignmentMessageContent;
import robotPackage.DefBidMessageContent;
import robotPackage.MessageContent;
import robotPackage.DeliverPackageMessageContent;
import robotPackage.PreAssignmentMessageContent;
import robotPackage.PreBidMessageContent;

	import com.github.rinde.rinsim.core.TickListener;
import com.github.rinde.rinsim.core.TimeLapse;
import com.github.rinde.rinsim.core.model.comm.CommDevice;
import com.github.rinde.rinsim.core.model.comm.CommDeviceBuilder;
import com.github.rinde.rinsim.core.model.comm.CommUser;
import com.github.rinde.rinsim.core.model.comm.Message;
import com.github.rinde.rinsim.core.model.comm.MessageContents;
import com.github.rinde.rinsim.core.model.pdp.PDPModel;
import com.github.rinde.rinsim.core.model.pdp.Parcel;
import com.github.rinde.rinsim.core.model.road.RoadModel;
import com.github.rinde.rinsim.core.model.road.RoadUser;
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
		Long delay = 3000l;
		Long timeLastAction;
		int mycontractId ;
		Point start;
		Point end;
		Optional<CommDevice> translator;
		private int stage =0;
		RoadModel roadModel;
		Package(Point startPosition, Point pDestination,
				long pLoadingDuration, long pUnloadingDuration) {
			super(pDestination, pLoadingDuration, TimeWindow.ALWAYS,
					pUnloadingDuration, TimeWindow.ALWAYS, 1);
			start =startPosition;
			setStartPosition(startPosition);
			end = pDestination;
			mycontractId=contractId;
			contractId++;
		}

		public void setStart(Point start) {
			this.start = start;
		}

		public void setEnd(Point end) {
			this.end = end;
		}

		@Override
		public void afterTick(TimeLapse arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void tick(TimeLapse time) {
			if(stage ==0 && time.getTimeLeft()>0){
				this.translator.get().broadcast(new DeliverPackageMessageContent(null, this, mycontractId));
				stage++;
				timeLastAction=time.getStartTime();
				if(time.getEndTime()<timeLastAction+delay){
					time.consumeAll();
				}else{
					time.consume(timeLastAction+delay-1-time.getStartTime());
				}
			}
			ImmutableList<Message> list;
			if(stage ==1 && time.getTimeLeft()>0){
				if(time.getEndTime()>=timeLastAction+delay){
					list = (ImmutableList<Message>)this.translator.get().getUnreadMessages();
					ArrayList<Message> preBidList = new ArrayList<Message>();
					double bid = Double.MAX_VALUE;
					CommUser bestUser = null;
					for(Message m:list){
						if(((MessageContent) m.getContents()).getType().equals("PreBidMessage")){
							preBidList.add( m);
							if(((PreBidMessageContent) m.getContents()).getBid()<bid){
								bid = ((PreBidMessageContent) m.getContents()).getBid();
								bestUser = m.getSender();
							}
						}
					}
					for(Message m:preBidList){
						if(m.getSender()==bestUser){
							this.translator.get().send(new PreAssignmentMessageContent(bestUser, bid, true , this, mycontractId), m.getSender());
						}else{
							this.translator.get().send(new PreAssignmentMessageContent(bestUser, bid, false , this, mycontractId), m.getSender());
						}
					}
					stage++;
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
			if(stage ==2 && time.getTimeLeft()>0){
				if(time.getEndTime()>=timeLastAction+delay){
					list = this.translator.get().getUnreadMessages();
					ArrayList<Message> DefBidList = new ArrayList<Message>();
					double bid = Double.MAX_VALUE;
					CommUser bestUser = null;
					for(Message m:list){
						if(((MessageContent) m.getContents()).getType().equals("DefBidMessage")){
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
					stage++;
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
			if(stage ==3) {
				for(Robot user: roadModel.getObjectsOfType(Robot.class))
				if(pdpModel.containerContains(user, this))return Optional.of(roadModel.getPosition(user));
			}
			return Optional.of(roadModel.getPosition(this));
			
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

		

	}

