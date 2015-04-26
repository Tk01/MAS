package world;

	import java.util.ArrayList;

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
	class Package extends Parcel implements CommUser,TickListener {
		Package(Point startPosition, Point pDestination,
				long pLoadingDuration, long pUnloadingDuration) {
			super(pDestination, pLoadingDuration, TimeWindow.ALWAYS,
					pUnloadingDuration, TimeWindow.ALWAYS, 1);
			setStartPosition(startPosition);
		}

		@Override
		public void afterTick(TimeLapse arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void tick(TimeLapse arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public Optional<Point> getPosition() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void setCommDevice(CommDeviceBuilder arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void initRoadPDP(RoadModel arg0, PDPModel arg1) {
			// TODO Auto-generated method stub
			
		}
		

	}

