package world;



import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

















import robotPackage.*;

import javax.annotation.Nullable;
import javax.measure.quantity.Duration;
import javax.measure.unit.Unit;

import com.github.rinde.rinsim.core.Simulator;
import com.github.rinde.rinsim.core.TickListener;
import com.github.rinde.rinsim.core.TimeLapse;
import com.github.rinde.rinsim.core.model.comm.CommModel;
import com.github.rinde.rinsim.core.model.pdp.DefaultPDPModel;
import com.github.rinde.rinsim.core.model.road.PlaneRoadModel;
import com.github.rinde.rinsim.core.model.road.RoadModel;
import com.github.rinde.rinsim.core.model.road.RoadUnits;
import com.github.rinde.rinsim.geom.Point;
import com.github.rinde.rinsim.ui.View;
import com.github.rinde.rinsim.ui.renderers.PlaneRoadModelRenderer;
import com.github.rinde.rinsim.ui.renderers.RoadUserRenderer;
import com.github.rinde.rinsim.util.TimeWindow;

public class Simulation {



	

	public static void main(@Nullable String[] args) throws IOException {
		InformationHandler.getInformationHandler().clear();
		SimulationGenerator gen = new SimulationGenerator("sim_0.txt");
		final double VEHICLE_SPEED_KMH = gen.getVEHICLE_SPEED_KMH();
		final ArrayList<Point> RList = gen.getRList();
		Point Cloc = gen.getChargeStation();
		final ArrayList<Point> PList = gen.getPList();
		final ArrayList<Point> PLocation = gen.getPLocation();
		final ArrayList<Long> PTime = gen.getPTime();
		final long SERVICE_DURATION =gen.getSERVICE_DURATION();
		final long endTime = gen.getEndTime();
		final long delay = gen.getDelay();
		final boolean reserveChargingStation = false;
		long contractNetDelay = gen.getContractNetDelay(reserveChargingStation);
		final RoadModel roadModel = gen.getRoadModel();
		Plan.setLimit(gen.getLimit());
		final DefaultPDPModel pdpModel = DefaultPDPModel.create();

		final Simulator simulator = Simulator.builder()
				.addModel(roadModel)
				.addModel(pdpModel)
				.addModel(CommModel.builder()
						.build())
						.build();

		;

		ChargingStation chargingStation = new ChargingStation(Cloc);
		simulator.register(chargingStation);

		for (Point p:RList) {
			
			Robot rob = new Robot(p, chargingStation, VEHICLE_SPEED_KMH, gen.getBatterySize(), gen.getChargeRateSize(), reserveChargingStation, contractNetDelay);
			simulator.register(rob);
		}

		simulator.addTickListener(new TickListener() {
			@Override
			public void tick(TimeLapse time) {
				if (time.getStartTime() > endTime) {
					simulator.stop();
					try {
						InformationHandler.getInformationHandler().finish("testje2.txt");
					} catch (FileNotFoundException e) {

						e.printStackTrace();
					} catch (UnsupportedEncodingException e) {

						e.printStackTrace();
					}
					
				} else while (!PTime.isEmpty() && time.getTime() >= PTime.get(0)) {
					Package p =new Package(PList.get(0),PLocation.get(0),0,0,new TimeWindow(PTime.get(0), PTime.get(0)+SERVICE_DURATION),new TimeWindow(PTime.get(0)+time(PList.get(0),PLocation.get(0),time.getTimeUnit()), PTime.get(0)+2*SERVICE_DURATION+time(PList.get(0),PLocation.get(0),time.getTimeUnit())), delay);
					InformationHandler.getInformationHandler().addPackage(p);
					simulator.register(p);
					PList.remove(0);
					PLocation.remove(0);
					PTime.remove(0);
				}
			}

			private Long time(Point point, Point point2, Unit<Duration> t) {
				RoadUnits r= new RoadUnits(roadModel.getDistanceUnit(),roadModel.getSpeedUnit());
				return (long) r.toExTime(r.toInDist(Point.distance(point,point2))/r.toInSpeed(VEHICLE_SPEED_KMH),t);
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
						.with(new ProblemRenderer())
						.setTitleAppendix("Taxi Demo");



		view.show();
	}

	public static void sim(String input,final String output,Boolean reserveChargingStation) throws IOException {
		InformationHandler.getInformationHandler().clear();
		SimulationGenerator gen;
		if( new File(input).exists()){
			gen = new SimulationGenerator(input);
		}else{
			gen = new SimulationGenerator(input,4,0.0035);
		}
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
		final long delay = gen.getDelay();
		Plan.setLimit(gen.getLimit());
		long contractNetDelay = gen.getContractNetDelay(reserveChargingStation);
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


		final ChargingStation chargingStation = new ChargingStation(Cloc);
		simulator.register(chargingStation);
		
		for (Point p:RList) {
			Robot rob = new Robot(p, chargingStation, VEHICLE_SPEED_KMH, gen.getBatterySize(), gen.getChargeRateSize(), reserveChargingStation,contractNetDelay);
			simulator.register(rob);
		}

		simulator.addTickListener(new TickListener() {
			@Override
			public void tick(TimeLapse time) {
				if (time.getStartTime() > endTime) {
					simulator.stop();
					try {
						InformationHandler.getInformationHandler().finish(output);
						
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}
				} else while (!PTime.isEmpty() && time.getTime() >= PTime.get(0)) {
					Package p =new Package(PList.get(0),PLocation.get(0),0,0,new TimeWindow(PTime.get(0), PTime.get(0)+SERVICE_DURATION),new TimeWindow(PTime.get(0)+time(PList.get(0),PLocation.get(0),time.getTimeUnit()), PTime.get(0)+SERVICE_DURATION+2*time(PList.get(0),PLocation.get(0),time.getTimeUnit())),delay);
					InformationHandler.getInformationHandler().addPackage(p);
					simulator.register(p);
					PList.remove(0);
					PLocation.remove(0);
					PTime.remove(0);
				}
			}

			private Long time(Point point, Point point2, Unit<Duration> t) {
				RoadUnits r= new RoadUnits(roadModel.getDistanceUnit(),roadModel.getSpeedUnit());
				return (long) r.toExTime(r.toInDist(Point.distance(point,point2))/r.toInSpeed(VEHICLE_SPEED_KMH),t);
			}

			@Override
			public void afterTick(TimeLapse timeLapse) {}
		});
		simulator.start();
	}


}
