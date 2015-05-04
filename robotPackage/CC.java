package robotPackage;

import java.util.List;

import com.github.rinde.rinsim.core.model.comm.Message;

public class CC {
	
	CKnowledge cKnowledge;
	
	public CC(){
		
	}
	
	public void controller (Message message){
		if(message.getContents().  .equals("request")){
			if(message.getContents().)
		}
	}
	
	public void Do(Goal goal){
		
	
	}
	
	public ArrayList<Plan> planGenerator(){
		
	}
	
	// Will be needed in case the negotiation during CNET is needed and another agent requests to plan a task
	// The goal will be the task which the other agent wants to lose so for it to be picked up by another agent
	// Wil return the new plan to the PCB if the new plan is good enough
	private void plan(Goal goal){
		
	}
	
	public void eval(List<JPlan> lis){
		
	}
	
	public void interpret(JPlan plan){
		
	}
	
	public void done(Plan plan, Boolean stat){
		
	}
	
	public void etract(Goal g){
		
	}
	
	private ArrayList jointPlanGenerator(Goal goal){
		
	}
}
