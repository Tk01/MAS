package worldInterface;

import java.util.NoSuchElementException;

import world.InformationHandler;
import world.Package;
import WorldModel.WorldModel;

import com.github.rinde.rinsim.core.model.road.MoveProgress;
import com.github.rinde.rinsim.geom.Point;

public class Actions {
	
	private WorldModel model;
	
	public Actions(WorldModel model){
		this.model = model;
		
	}
	
	public void MoveTo(Point x){
		@SuppressWarnings("unused")
		MoveProgress consumed = model.getRoadModel().get().moveTo(model.getThisRobot(), x, model.getTime());

	}
	
	
	public void charge(long l) {
		double toCharge = model.getMaxBattery()-model.battery();
		
		double chargeTime =(long) (toCharge/(model.getChargeRate()+1));
		if(chargeTime ==0) chargeTime =1; 
		long timeSpend = (long) Math.min(Math.min(chargeTime , model.getTime().getTimeLeft()), l-model.getTime().getTime());
		model.charge((model.getChargeRate()+1)*timeSpend);
		model.getTime().consume(timeSpend);
	}
	
	public void drop() {
		try{
		model.getPdpModel().get().deliver(model.getThisRobot(), model.getCarriedPackage(), model.getTime());
		model.getCarriedPackage().deliver();
		model.dropPackage();
		model.getTime().consume(0);
		}
		catch(IllegalArgumentException e){
			throw e;
		}
	}
	public void pickup() throws NoSuchElementException{
		try{
		Package parcel = this.getPackageHere();
		model.getPdpModel().get().pickup(model.getThisRobot(), parcel, model.getTime());
		model.pickupPackage(parcel);
		model.getTime().consume(0);}
		catch(IllegalArgumentException e){
			throw e;
		}
		catch(NoSuchElementException e){
			throw e;
		}
	}
	/**
	 * returns the first package at the location of robot
	 * @throws NoSuchElementException
	 */
	private Package getPackageHere() throws NoSuchElementException{
		try{
			return model.getRoadModel().get().getObjectsAt(model.getThisRobot(), world.Package.class).iterator().next();
		}catch(NoSuchElementException e){
			throw e;
		}
		
	}
	public void waitMoment(boolean b) {
		//model.batteryDrop(model.getTime().getTimeLeft()*SpendingRate);
		InformationHandler.getInformationHandler().addTime(model.getTime().getTimeLeft(), model.getThisRobot());
		model.getTime().consumeAll();;

	}

}
