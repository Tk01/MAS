package world;

import java.io.IOException;

public class FullSetup {

	public static void main(String[] args) throws IOException {
		for(int i =0; i<60;i++){
			System.out.println("sim "+i+" started.");
			Simulation.sim("sim_"+i+".txt", "sim_"+i+"_noRes_result.txt", false);
			Simulation.sim("sim_"+i+".txt", "sim_"+i+"_Res_result.txt", true);
			System.out.println("sim "+i+" done.");
		}

	}

}
