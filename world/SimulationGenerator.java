package world;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import com.github.rinde.rinsim.geom.Point;

public class SimulationGenerator {
	final Point MIN_POINT = new Point(0,0);
	final Point MAX_POINT = new Point(10,10);
	final double VEHICLE_SPEED_KMH = 50d;
	final ArrayList<Point> RList = new ArrayList<Point>();
	final Point ChargeStation = new Point(5,5);
	final ArrayList<Point> PList = new ArrayList<Point>();
	final ArrayList<Point> PLocation = new ArrayList<Point>();
	final ArrayList<Long> PTime = new ArrayList<Long>();
	final long SERVICE_DURATION =0;
	final long endTime = 10;
	public  SimulationGenerator(String filename) throws IOException{
		BufferedReader in = new BufferedReader(new FileReader(filename));
		String[] robots = in.readLine().split(";");
		for(String r:robots){
			String[] split = r.split(",");
			RList.add(new Point(Integer.parseInt(split[0]),Integer.parseInt(split[1])));
		}
		String[] Packages = in.readLine().split(";");
		for(String r:Packages){
			String[] split = r.split(",");
			PList.add(new Point(Integer.parseInt(split[0]),Integer.parseInt(split[1])));
		}
		String[] Destinations = in.readLine().split(";");
		for(String r:Destinations){
			String[] split = r.split(",");
			PLocation.add(new Point(Integer.parseInt(split[0]),Integer.parseInt(split[1])));
		}
		String[] times = in.readLine().split(";");
		for(String r:times){
			PTime.add(Long.parseLong(r));
		}
	}
	public  SimulationGenerator(String filename, int nbRobots, double spawnchance) throws FileNotFoundException, UnsupportedEncodingException{
		for(int i=0;i<nbRobots;i++){
			RList.add(new Point(Math.random()*this.MAX_POINT.x,Math.random()*this.MAX_POINT.y));
		}
		for(long i=0;i<endTime;i++){
			if(Math.random()<=spawnchance){
				PList.add(new Point(Math.random()*this.MAX_POINT.x,Math.random()*this.MAX_POINT.y));
				PList.add(new Point(Math.random()*this.MAX_POINT.x,Math.random()*this.MAX_POINT.y));
				PTime.add( i);
			}
		}
		PrintWriter writer = new PrintWriter(filename+".txt", "UTF-8");
		String line="";
		for(Point p:RList){
			line+=p.x+","+p.y+";";
		}
		writer.println(line);
		line="";
		for(Point p:PList){
			line+=p.x+","+p.y+";";
		}
		writer.println(line);
		line="";
		for(Point p:PLocation){
			line+=p.x+","+p.y+";";
		}
		writer.println(line);
		line="";
		for(long p:PTime){
			line+=p+";";
		}
		writer.println(line);
		writer.close();
	}
	public Point getMIN_POINT() {
		return MIN_POINT;
	}
	public Point getMAX_POINT() {
		return MAX_POINT;
	}
	public double getVEHICLE_SPEED_KMH() {
		return VEHICLE_SPEED_KMH;
	}
	public ArrayList<Point> getRList() {
		return RList;
	}
	public Point getChargeStation() {
		return ChargeStation;
	}
	public ArrayList<Point> getPList() {
		return PList;
	}
	public ArrayList<Point> getPLocation() {
		return PLocation;
	}
	public ArrayList<Long> getPTime() {
		return PTime;
	}
	public long getSERVICE_DURATION() {
		return SERVICE_DURATION;
	}
	public long getEndTime() {
		return endTime;
	}
}
