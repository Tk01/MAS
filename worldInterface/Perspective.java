package worldInterface;

import java.util.ArrayList;

import WorldModel.Robot;
import WorldModel.WorldModel;

import com.github.rinde.rinsim.core.TimeLapse;
import com.github.rinde.rinsim.geom.Point;

public class Perspective {
	
	WorldModel model;
	
	public Perspective(WorldModel model){
		this.model = model;
		
	}
	
	public void gatherInfo(TimeLapse time) {
		
		model.setCoordinates( model.getRoadModel().get().getPosition(model.getThisRobot()));
		
		ArrayList<Point> list = new ArrayList<Point>();
		
		for(Robot r: model.getRoadModel().get().getObjectsOfType( Robot.class)){
			if(!r.equals(this.model.getThisRobot()) &&Point.distance( model.getRoadModel().get().getPosition(r),this.model.coordinates()) < 1){
				list.add( model.getRoadModel().get().getPosition(r));
			}
		}

		model.setRobots(list);
		model.setTime(time);
	}

}
