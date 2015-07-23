package world;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import jxl.Workbook;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;
import jxl.write.Number;

public class FullSetup {

	public static void main(String[] args) throws IOException, RowsExceededException, WriteException {
		WritableWorkbook wworkbook;
		wworkbook = Workbook.createWorkbook(new File("output2.xls"));
	      WritableSheet wsheet = wworkbook.createSheet("First Sheet", 0);
	      Label label = new Label(0, 0, "Testnr.");
	      Label label1 = new Label(1, 0, "FailedNonRes");
	      Label label2 = new Label(2, 0, "FailedRes");
	      Label label3 = new Label(3, 0, "FailedPackagesNonRes");
	      Label label4 = new Label(4, 0, "FailedPackageRes");
	      Label label5 = new Label(5, 0, "IdleTimeR1_NonRes");
	      Label label6 = new Label(6, 0, "IdleTimeR2_NonRes");
	      Label label7 = new Label(7, 0, "IdleTimeR3_NonRes");
	      Label label8 = new Label(8, 0, "IdleTimeR4_NonRes");
	      Label label9 = new Label(9, 0, "IdleTimeR1_Res");
	      Label label10 = new Label(10, 0, "IdleTimeR2_Res");
	      Label label11 = new Label(11, 0, "IdleTimeR3_Res");
	      Label label12 = new Label(12, 0, "IdleTimeR4_Res");
	      Label label13 = new Label(13, 0, "SuccesfullNegotiations_NonRes");
	      Label label14 = new Label(14, 0, "SuccesfullNegotiations_Res");
	      Label label15 = new Label(15, 0, "chargeswapped_NonRes");
	      Label label16 = new Label(16, 0, "chargeswapped_Res");
	      wsheet.addCell(label);
	      wsheet.addCell(label1);
	      wsheet.addCell(label2);
	      wsheet.addCell(label3);
	      wsheet.addCell(label4);
	      wsheet.addCell(label5);
	      wsheet.addCell(label6);
	      wsheet.addCell(label7);
	      wsheet.addCell(label8);
	      wsheet.addCell(label9);
	      wsheet.addCell(label10);
	      wsheet.addCell(label11);
	      wsheet.addCell(label12);
	      wsheet.addCell(label13);
	      wsheet.addCell(label14);
	      wsheet.addCell(label15);
	      wsheet.addCell(label16);
		for(int i =0; i<60;i++){
			System.out.println("sim "+i+" started.");
			Number number = new Number(0, i+1, i);
			wsheet.addCell(number);
			Simulation.sim("sim_"+i+".txt", "sim_"+i+"_noRes_result_neg2.txt", false);
			Number number2 = new Number(1, i+1, InformationHandler.getInformationHandler().failed());
			Number number4 = new Number(3, i+1, InformationHandler.getInformationHandler().failedPackages());
			wsheet.addCell(number4);
		    wsheet.addCell(number2);
		    ArrayList<Long> idleTimes = InformationHandler.getInformationHandler().sortedValues(InformationHandler.getInformationHandler().getIdleTimes());
		    Number number6 = new Number(5, i+1, idleTimes.get(0));
			wsheet.addCell(number6);
			Number number7 = new Number(6, i+1, idleTimes.get(1));
			wsheet.addCell(number7);
			Number number8 = new Number(7, i+1, idleTimes.get(2));
			wsheet.addCell(number8);
			Number number9 = new Number(8, i+1, idleTimes.get(3));
			wsheet.addCell(number9);
			Number number14= new Number(13, i+1,InformationHandler.getInformationHandler().getNegotiations() );
			wsheet.addCell(number14);
			Number number16 = new Number(15, i+1,InformationHandler.getInformationHandler().getlostcharge() );
			wsheet.addCell(number16);
			Simulation.sim("sim_"+i+".txt", "sim_"+i+"_Res_result_neg2.txt", true);
			System.out.println("sim "+i+" done.");
			Number number3 = new Number(2, i+1, InformationHandler.getInformationHandler().failed());
		    wsheet.addCell(number3);
		    Number number5 = new Number(4, i+1, InformationHandler.getInformationHandler().failedPackages());
			wsheet.addCell(number5);
			idleTimes = InformationHandler.getInformationHandler().sortedValues(InformationHandler.getInformationHandler().getIdleTimes());
		    Number number10 = new Number(9, i+1, idleTimes.get(0));
			wsheet.addCell(number10);
			Number number11 = new Number(10, i+1, idleTimes.get(1));
			wsheet.addCell(number11);
			Number number12 = new Number(11, i+1, idleTimes.get(2));
			wsheet.addCell(number12);
			Number number13 = new Number(12, i+1, idleTimes.get(3));
			wsheet.addCell(number13);
			Number number15 = new Number(14, i+1,InformationHandler.getInformationHandler().getNegotiations() );
			wsheet.addCell(number15);
			Number number17 = new Number(16, i+1,InformationHandler.getInformationHandler().getlostcharge() );
			wsheet.addCell(number17);
		}
		 wworkbook.write();
	     wworkbook.close();
	}

}
