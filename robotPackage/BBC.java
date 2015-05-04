package robotPackage;

import java.util.ArrayList;

import com.github.rinde.rinsim.core.model.comm.Message;
import com.github.rinde.rinsim.geom.Point;

public class BBC {
	Goal goal;
	Boolean charging;
	boolean done;
	public BBC(WorldInterface worldInterface, WorldModel model) {
		// TODO Auto-generated constructor stub
	}

	public void msg(ArrayList<Message> arrayList) {
		// TODO Auto-generated method stub

	}

	public void placeGoal(Goal goal){
		this.goal = goal;
	}
	public void run() {
		if(done) pbc.done(goal);
		else{
			if( (model.battery() < 0.25 && goal != charging && !charging) ) pbc.plan(new Charging());
			else{
				if( model.messages().size() !=0) pbc.plan();
			}
		}
		if( goal == null){
			model.wait();
			return;
		}
		if(goal.coordinate() != worldinterface.coordinate()){
			model.moveTo(goal.coordinate());
			return;
		}
		if(goal.type() = "pickup"){
			model.pickup();
			done =true;
			return;
		}
		if(goal.type() = "drop"){
			model.drop();
			done = true;
			return;
		}
		if(goal.type= "charging" && this.chargeTaken()){
			model.wait();
			return;
		}
		if(goal.type() = "charging"){
			model.charge();
			if(model.battery() == 1) done =true;
			return;
		}


	}


}
