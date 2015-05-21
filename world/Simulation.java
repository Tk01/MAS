package world;



import java.io.IOException;
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
 
	

	public static void main(@Nullable String[] args) throws IOException {
		// TODO sceneraio loader
		SimulationGenerator gen = new SimulationGenerator("sim1.txt",4,0.0035);
		final Point MIN_POINT = gen.getMIN_POINT();
		final Point MAX_POINT = gen.getMAX_POINT();
		final double VEHICLE_SPEED_KMH = gen.getVEHICLE_SPEED_KMH();
		final ArrayList<Point> RList = gen.getRList();
		Point Cloc = gen.getChargeStation();
		final ArrayList<Point> PList = gen.getPList();
		final ArrayList<Point> PLocation = gen.getPLocation();
		final ArrayList<Long> PTime = gen.getPTime();
		final long SERVICE_DURATION =gen.getSERVICE_DURATION();
		final long endTime = gen.getEndTime();
		
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
	  
	    
	   
		ChargingStation chargingStation = new ChargingStation(Cloc);
		simulator.register(chargingStation);
		  
		for (Point p:RList) {
		      Robot rob = new Robot(p, chargingStation, VEHICLE_SPEED_KMH);
		      simulator.register(rob);
		}

		simulator.addTickListener(new TickListener() {
		      @Override
		      public void tick(TimeLapse time) {
		        
				if (time.getStartTime() > endTime) {
		          simulator.stop();
		        } else while (!PTime.isEmpty() && time.getTime() >= PTime.get(0)) {
		          simulator.register(new Package(PList.get(0),PLocation.get(0),0,0));
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
