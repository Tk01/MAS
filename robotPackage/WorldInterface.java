package robotPackage;



import java.awt.List;
import java.util.ArrayList;

import com.github.rinde.rinsim.core.model.comm.CommDevice;
import com.github.rinde.rinsim.core.model.comm.CommUser;
import com.github.rinde.rinsim.core.model.comm.Message;
import com.github.rinde.rinsim.geom.Point;

public class WorldInterface {
	CommDevice translator;
	Action action;
	Point moveTo;
	BBC bbc;
	WorldModel model;
	public WorldInterface(CommDevice communication){
		bbc = new BBC(this,model);
	}
	public void sendMessage( Message message) {
		
	}
	public void MoveTo(Point x){
		
	}
	public void run(){
		if(battery > 0){
		this.gatherInfo();
		bbc.run();
		model.batteryDrop(0.05);
		}
	}

}
