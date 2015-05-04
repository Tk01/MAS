package robotPackage;

public class Goal {
	
	String Action;
	
	Package package;
	
	public Goal(String action, package ){
		this.action = action;
		this.package = package;		
		
	}
	
	public String getAction(){
		return action;
	}
	
	public package getPackage(){
		return package;
	}

}
