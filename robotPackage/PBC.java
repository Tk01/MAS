package robotPackage;

import java.util.ArrayList;
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
		
		Plan plan = knwoledgeBase.getCurrentPlan();
		
		ArrayList<Plan> plans = generatePlans(pack);
		double bid = 0;
		for (int i = 0; i<plans.size();i++){
					
		}
		
		return bid;
		
	}
	
	//Makes a list of plans with the new pack that can be possible to achieve
	private ArrayList<Plan> generatePlans(Pack pack){
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
	
}
