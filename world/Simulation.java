package world;



import java.util.ArrayList;




import robotPackage.*;


import javax.annotation.Nullable;

import org.apache.commons.math3.random.RandomGenerator;

import com.github.rinde.rinsim.core.Simulator;
import com.github.rinde.rinsim.core.TickListener;
import com.github.rinde.rinsim.core.TimeLapse;
import com.github.rinde.rinsim.core.model.comm.CommModel;
import com.github.rinde.rinsim.core.model.pdp.DefaultPDPModel;
import com.github.rinde.rinsim.core.model.pdp.Parcel;
import com.github.rinde.rinsim.core.model.road.PlaneRoadModel;
import com.github.rinde.rinsim.core.model.road.RoadModel;
import com.github.rinde.rinsim.geom.Point;
import com.github.rinde.rinsim.ui.View;
import com.github.rinde.rinsim.ui.renderers.PlaneRoadModelRenderer;
import com.github.rinde.rinsim.ui.renderers.RoadUserRenderer;

public class Simulation {
 
	

	public static void main(@Nullable String[] args) {
		// TODO sceneraio loader
		final Point MIN_POINT = null;
		final Point MAX_POINT = null;
		final double VEHICLE_SPEED_KMH = 0d;
		final ArrayList<Point> RList = null;
		final ArrayList<Point> CList = null;
		final ArrayList<Point> PList = null;
		final ArrayList<Point> PLocation = null;
		final ArrayList<Double> PTime = null;
		final long SERVICE_DURATION =0;
		final long endTime = 0;
		
		final RoadModel roadModel =  PlaneRoadModel.builder()
	            .setMinPoint(MIN_POINT)
	            .setMaxPoint(MAX_POINT)
	            .setMaxSpeed(VEHICLE_SPEED_KMH)
	            .build();
		final DefaultPDPModel pdpModel = DefaultPDPModel.create();

	    final Simulator simulator = Simulator.builder()
	        .addModel(roadModel)
	        .addModel(pdpModel)
	        .addModel(CommModel.builder()
	            .build())
	        .build();
	    /*
	    for (Point p:RList) {
		      simulator.register(new Robot(p,1));
		    }
	    for (Point p:CList) {
		      simulator.register(new ChargingStation(p));
		    }
	*/
		simulator.addTickListener(new TickListener() {
		      @Override
		      public void tick(TimeLapse time) {
		        
				if (time.getStartTime() > endTime) {
		          simulator.stop();
		        } else if (PTime.isEmpty() && time.getTime() == PTime.get(0)) {
		          simulator.register(new Package(PList.get(0),PLocation.get(0),SERVICE_DURATION,SERVICE_DURATION));
		          PList.remove(0);
		          PLocation.remove(0);
		          PTime.remove(0);
		        }
		      }

		      @Override
		      public void afterTick(TimeLapse timeLapse) {}
		    });
		 final View.Builder view = View
			        .create(simulator)
			        .with(PlaneRoadModelRenderer.create())
			        .with(RoadUserRenderer.builder()
			            .addImageAssociation(
			                ChargingStation.class, "/graphics/perspective/tall-building-64.png")
			            .addImageAssociation(
			                Robot.class, "/graphics/flat/taxi-32.png")
			            .addImageAssociation(
			                Package.class, "/graphics/flat/person-red-32.png")
			        
			        )
			        .setTitleAppendix("Taxi Demo");

			   

			    view.show();
	}
	
	
	
}
