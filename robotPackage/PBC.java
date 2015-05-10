package robotPackage;

import java.util.ArrayList;
import java.util.List;

import com.github.rinde.rinsim.core.model.comm.CommUser;

import world.Package;

public class PBC {
	
	PBCKB knwoledgeBase;
	BBC bbc;
	public PBC(){
		knwoledgeBase = new PBCKB();
	}
	
	boolean chargingInPlan = true;
	

	public void done(Goal g){
		knwoledgeBase.getCurrentPlan().remove(g);
		
		bbc.setGoal(knwoledgeBase.getCurrentPlan().getNextgoal());
		
	}

	public double doBid(Package pack, CommUser sender){
		
		Plan plan = knwoledgeBase.getCurrentPlan();
		
		ArrayList<Plan> plans = generatePlans(pack);
		double bid = 0;
		for (int i = 0; i<plans.size();i++){
					
		}
		
		return bid;
		
	}
	
	//Makes a list of plans with the new pack that can be possible to achieve
	private ArrayList<Plan> generatePlans(Package pack){
		ArrayList<Plan> generatedPlans = new ArrayList<Plan>();
		Plan plan = knwoledgeBase.getCurrentPlan();
		for(int i=0; i<plan.getPlan().size();i++){
			Plan tempPlan = plan;
			tempPlan.addPackage(pack, i);
			
			if(tempPlan.isPossiblePlan(i)){
				generatedPlans.add(tempPlan);
			}
		}
		return generatedPlans;
		
	}
	
	private Plan getBestPlan(ArrayList<Plan> plans){
		Plan bestPlan = null;
		double bestValue = -1;
		for(int i=0; i<plans.size();i++){
			double value = plans.get(i).getPlanValue();
			if(value == -1){
				bestValue = value;
				bestPlan = plans.get(i);
			}
			if(value<bestValue){
				bestValue = value;
				bestPlan = plans.get(i);
			}
		}
		return bestPlan;
	}
	public void plan() {
		// TODO Auto-generated method stub
		
	}

	public void plan(Charging charging) {
		// TODO Auto-generated method stub
		
	}
	
}
