package robotPackage;

import java.util.ArrayList;
import java.util.List;

import com.github.rinde.rinsim.core.model.comm.CommUser;

import world.Pack;

public class PBC {
	
	PBCKB knowledgeBase;
	
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
	
	
	//A bid will be done when the pack can be fit in the plan and no other task is being bid on which has a better value.
	// when no bid is done, the bid will be -1
	public double doPreBid(Pack pack, CommUser sender){
		
		
		double bid = -1;
		ArrayList<Plan> plans = generatePlans(pack);
		Plan bestPlan=getBestPlan(plans);
		double currentBestValue = knowledgeBase.getProvisionalPlan().getPlanValue();
		double bestPlanValue = bestPlan.getPlanValue();
		if(bestPlan != null&&bestPlanValue > currentBestValue){
			bid = bestPlan.getPlanValue();
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
	
	public double checkIfBetterBid(Pack pack, double currentBestBid){
		double bid = -1;
		
		ArrayList<Plan> plans = generatePlans(pack);
		Plan bestPlan=getBestPlan(plans);
		double currentBestValue = knwoledgeBase.getProvisionalPlan().getPlanValue();
		double bestPlanValue = bestPlan.getPlanValue();
		if(bestPlan != null&&bestPlanValue > currentBestValue){
			bid = bestPlan.getPlanValue();
		}
		
		if(bid == -1){
			
		}
		
		/* Add check here for negotation during the DynCNET as here the communication with the CC should start*/
		
		if(bid>-1 && currentBestBid < bid ){
			bid = -1;
			
		}
		
		return bid;
	}
	
}
