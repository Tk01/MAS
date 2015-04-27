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
	public void Run(){
		bbc.done(action,moveTo,true);
		bbc.msg(new ArrayList<Message>());
		bbc.run();
	}
	public void Action(Action action){
		  
	  }

}
