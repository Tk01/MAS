package robotPackage;

import java.util.List;

import com.github.rinde.rinsim.core.model.comm.CommUser;

import world.Pack;

public class PBC {
	
	PBCKB knwoledgeBase;
	
	public PBC(){
		knwoledgeBase = new PBCKB();
	}
	
	boolean chargingInPlan = true;
	
	public void interpret(Plan plan){
		
	}
	public void done(Goal g, Plan p, Boolean b){
		
	}
	
	public void planned(Goal g,JPlan plan){
		
	}
	public void evaled(List<JPlan> JLIst, Eval eval){
		
	}
	
	public double doBid(Pack pack, CommUser sender){
		double bid = 0;
		Plan plan = knwoledgeBase.getCurrentPlan();
		
		ArrayList<Plan> plans = generatePlans();
		double bid = 0;
		for (int i = 0; i<plans.size();i++){
					
		}
		
		return bid;
		
	}
	
}
