package world;

import java.io.IOException;

public class NegotiationOccasion {

	public static void main(String[] args) throws IOException {
		int i=0;
		while(true){
		Simulation.sim("NegTest_"+i+".txt", "NegTest_"+i+"_result.txt", false);
		if(InformationHandler.getInformationHandler().getNegotiations()>0){
			System.out.println("succeeded "+i);
		}else{
			System.out.println("failed "+i);
		}
		i++;
		}
	}

}
