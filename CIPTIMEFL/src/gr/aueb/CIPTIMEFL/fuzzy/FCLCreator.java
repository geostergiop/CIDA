package gr.aueb.CIPTIMEFL.fuzzy;

import gr.aueb.CIPTIMEFL.misc.IO;
import gr.aueb.CIPTIMEFL.misc.MathMethods;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class FCLCreator {

	List<double[][]> linearTables = new ArrayList<double[][]>();
	List<double[][]> logTables = new ArrayList<double[][]>();
	List<double[][]> expTables = new ArrayList<double[][]>();
	
	
	public FCLCreator() {
		// Create database of Impact tables
		createImpactTables();
	}
	
	
	public boolean generateFCL(double[][] impactArray) {
	
		// impactArray[timeSlot][Impact]
		
		/*
		 *  Membership of X in for each Impact Fuzzy Set is calculated 
		 *  by dividing the occurrences of X to the sum of all cells per category columns.
		 */
		String vLow = "\tTERM Very_Low := (1, 1) (2, 0);";
		
		double d1 = (howManyInColumn(1, 1, 2, impactArray)/20);
		String low = "\tTERM Low := 	(1, "+d1+") (2, "+(howManyInColumn(2, 1, 2, impactArray)/20)+") "
									+  "(3, "+(howManyInColumn(3, 1, 2, impactArray)/20)+");";
		
		String medium = "\tTERM Medium :=  (1, "+(howManyInColumn(1, 3, 4, impactArray)/20)+") (2, "+(howManyInColumn(2, 3, 4, impactArray)/20)+") "
										+ "(3, "+(howManyInColumn(3, 3, 4, impactArray)/20)+") (4, "+(howManyInColumn(4, 3, 4, impactArray)/20)+") "
										+ "(5, "+(howManyInColumn(5, 3, 4, impactArray)/20)+");";
		
		String high = "\tTERM High :=  (1, "+(howManyInColumn(1, 5, 6, impactArray)/20)+") (2, "+(howManyInColumn(2, 5, 6, impactArray)/20)+") "
									+ "(3, "+(howManyInColumn(3, 5, 6, impactArray)/20)+") (4, "+(howManyInColumn(4, 5, 6, impactArray)/20)+") "
									+ "(5, "+(howManyInColumn(5, 5, 6, impactArray)/20)+") (6, "+(howManyInColumn(6, 5, 6, impactArray)/20)+") "
									+ "(7, "+(howManyInColumn(7, 5, 6, impactArray)/20)+");";
		
		String vHigh = "\tTERM Very_High := (1, "+(howManyInColumn(1, 7, 8, impactArray)/20)+") (2, "+(howManyInColumn(2, 7, 8, impactArray)/20)+") "
									+ "(3, "+(howManyInColumn(3, 7, 8, impactArray)/20)+") (4, "+(howManyInColumn(4, 7, 8, impactArray)/20)+") "
									+ "(5, "+(howManyInColumn(5, 7, 8, impactArray)/20)+") (6, "+(howManyInColumn(6, 7, 8, impactArray)/20)+") "
									+ "(7, "+(howManyInColumn(7, 7, 8, impactArray)/20)+") (8, "+(howManyInColumn(8, 7, 8, impactArray)/20)+") "
									+ "(9, "+(howManyInColumn(9, 7, 8, impactArray)/20)+") ;";
		
		/*
		 *  Time Fuzzy set is calculated here 
		 *  {15, 60, 180, 720, 1440, 2880, 10080, 20160, 40320, 60480};
		 */
		String earlyT = "\tTERM Early := (0, 1) (15, 1) (60, 1) (180, 1) (720, 0);";
		
		String mediumT = "\tTERM Medium := (720, 1) (1440, 1) (2880, 1) (10080, 0);";
		
		String lateT = "\tTERM Late := (10080, 1) (20160, 1) (40320, 0);";
		
		String vLateT = "\tTERM Very_Late := (40320, 1) (60480, 1);";
		
		/*
		 * Write FCL file with all information gathered from Tables and Auditors.
		 */
		try {
			BufferedWriter out = new BufferedWriter
				    (new OutputStreamWriter(new FileOutputStream("new_base.fcl"),"UTF-8"));
			
			if (!printFCL(out, vLow, low, medium, high, vHigh, earlyT, mediumT, lateT, vLateT))
				return false;
			
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}		
		return true;
	}
	
	
	
	/****************************************************************************************
	 * Count the occurrences of IMPACT on columns ranging from FIRSTCOL to LASTCOL in ARRAY	*
	 ****************************************************************************************/
	private double howManyInColumn(int impact, int firstCol, int lastCol, double[][] array) {
		
		double count = 0;
		
		for (int k = firstCol; k<= lastCol; k++) {
			// to iterate over a single column k in a two-dimensional array:
			for (int i = 0; i < array.length; i++)
				if (impact == array[i][k])
					count++;
		}
		return count;
	}
	

	
	/****************************************************************************
	 * Calculate all Impact tables, using each row (time slot) as ceiling		*
	 * We need 30 tables: 10 tables for each growth rate, logarithmic,			*
	 * exponential, linear. Each one of them has a different Tmax limit, one	*
	 * for each timeScale point.												* 
	 ****************************************************************************/
	private void createImpactTables() {
		
		double[] timeScale = {15, 60, 180, 720, 1440, 2880, 10080, 20160, 40320, 60480};
		
		for (int Tmax = 0; Tmax < 10; Tmax++) {	// Use each slot in the time-scale as a potential max time value
			double[][] impactTableLinear = new double[10][9];
			double[][] impactTableLog = new double[10][9];
			double[][] impactTableExp = new double[10][9];
			
			double dTmax = timeScale[Tmax];
			
			for (int time = 0; time < timeScale.length; time++) {
				double dtime = timeScale[time];
				
				if (dtime > dTmax)
					dtime = dTmax;	// Bind upper limit to Tmax each time
				
				for (int I=0; I < 9; I++) {	// For each row (impact from 1 to 9)
					double dI = (double) I + 1.0;
					
					double logValue = MathMethods.limitDecimals(logWithBase(dTmax, dtime) * dI, "0");	
					double linearValue = MathMethods.limitDecimals((dtime/dTmax) * dI, "0");
					double expValue = MathMethods.limitDecimals(Math.pow(dI, (dtime/dTmax)), "0");	// MathMethods = limit double decimals to two
					
					// Lowest possible impact is 1 --Change all impact < 1 to 1.
					impactTableLog[time][I] = (logValue < 1) ? 1 : logValue;
					impactTableLinear[time][I] = (linearValue < 1) ? 1 : linearValue;
					impactTableExp[time][I] = (expValue < 1) ? 1 : expValue;
				}
			} // for (time)
			
			linearTables.add(impactTableLinear);
			logTables.add(impactTableLog);
			expTables.add(impactTableExp);
		}
		/*// Print database impact tables to excel sheets
		for (double[][] lt : linearTables) {
			try {
				IO.writeArrayBuffered(lt, 512, "linearTables");
			} catch (IOException e) {}
		}
		for (double[][] lt : logTables) {
			try {
				IO.writeArrayBuffered(lt, 512, "logTables");
			} catch (IOException e) {}
		}
		for (double[][] lt : expTables) {
			try {
				IO.writeArrayBuffered(lt, 512, "expTables");
			} catch (IOException e) {}
		}*/
	}
	
	
	/****************************************************************
	 * Calculate the Logarithm of a number using any base you want	*
	 ****************************************************************/
	private double logWithBase(double base, double num) {
		//double a = Math.log(num);
		//double b = Math.log(base);
	    return Math.log(num) / Math.log(base);
	}
	
	
	
	/************************************************************************************
	 * Prints an .fcl file which is the configuration of the Fuzzy Logic system.		*
	 * Input tables and ranges are calculated earlier using exponential, logarithmic	*
	 * or linear inverse growth rates.													*
	 ************************************************************************************/
	private boolean printFCL(BufferedWriter out, String vLow, String low, String medium, String high, String vHigh, 
											  String earlyT, String mediumT, String lateT, String vLateT) 
						  throws IOException {
		
		out.write("// Block definition (there may be more than one block per file)"); out.newLine();
		out.write("FUNCTION_BLOCK Floggic"); out.newLine();
		out.newLine();
		out.write("// Define input variables"); out.newLine();
		out.write("VAR_INPUT"); out.newLine();
		out.write("\tImpact : REAL;"); out.newLine();
		out.write("\tTime : REAL;"); out.newLine();
		out.write("END_VAR"); out.newLine();
		out.newLine();
		out.write("// Define output variable"); out.newLine();
		out.write("VAR_OUTPUT"); out.newLine();
		out.write("\tImpact_T : REAL;"); out.newLine();
		out.write("END_VAR"); out.newLine();
		out.newLine();
		out.write("// Fuzzify input variable 'Impact'"); out.newLine();
		out.write("FUZZIFY Impact"); out.newLine();
		out.write("//			TERM Very_Low := gbell 2 2 2;");  out.newLine();
		out.write("//			TERM Low := gbell 1 2 3;"); out.newLine();
		out.write("//			TERM Medium :=  gbell 2 2 5;"); out.newLine();
		out.write("//			TERM High :=  gbell 1 2 6;"); out.newLine();
		out.write("//			TERM Very_High :=  gbell 2 2 8;"); out.newLine();

		out.write(vLow);  out.newLine();
		out.write(low); out.newLine();
		out.write(medium); out.newLine();
		out.write(high); out.newLine();
		out.write(vHigh); out.newLine();
		out.newLine();
		out.write("END_FUZZIFY"); out.newLine();
		out.newLine();
		out.write("// Fuzzify input variable 'Time' in hours"); out.newLine();
		out.write("FUZZIFY Time"); out.newLine();
		out.write(earlyT); out.newLine();
		out.write(mediumT); out.newLine();
		out.write(lateT); out.newLine();
		out.write(vLateT); out.newLine();
		out.write("END_FUZZIFY"); out.newLine();
		out.newLine();
		out.write("// Defuzzify output variable 'Impact_T'"); out.newLine();
		out.write("DEFUZZIFY Impact_T"); out.newLine();
		out.write("\tTERM Very_Low := (0, 1) (1, 1) (2, 0);");  out.newLine();
		out.write("\tTERM Low := (1, 0) (2, 1) (3, 1) (4, 0);"); out.newLine();
		out.write("\tTERM Medium := (3, 0) (4, 1) (5, 1) (6, 0);"); out.newLine();
		out.write("\tTERM High := (5, 0) (6, 1) (7, 1) (8, 0);"); out.newLine();
		out.write("\tTERM Very_High := (7, 0) (8, 1) (9, 1);"); out.newLine();
		out.newLine();
		out.write("// Use 'RightMostMempership' defuzzification method, since we need the worst-case scenario");out.newLine();
		out.write("\tMETHOD : RM;");out.newLine();
		out.write("\t// Default value is 0 (if no rule activates defuzzifier)");out.newLine();
		out.write("\tDEFAULT := 0;");out.newLine();
		out.write("END_DEFUZZIFY");out.newLine();
		out.newLine();
		out.write("RULEBLOCK No1"); out.newLine();
		out.write("\t// Use 'min' for 'and' (also implicit use 'max'"); out.newLine();
		out.write("\t// for 'or' to fulfill DeMorgan's Law)"); out.newLine();
		out.write("\tAND : MIN;"); out.newLine();
		out.write("\t// Use 'min' activation method"); out.newLine();
		out.write("\tACT : MIN;"); out.newLine();
		out.write("\t// Use 'max' accumulation method"); out.newLine();
		out.write("\tACCU : MAX;"); out.newLine();
		out.newLine();
		
		out.write("\tRULE 1 : IF Impact IS Very_High AND Time IS Early THEN Impact_T is Medium;"); out.newLine();
		out.write("\tRULE 2 : IF Impact IS Very_High AND Time IS Medium THEN Impact_T is High;"); out.newLine();
		out.write("\tRULE 3 : IF Impact IS Very_High AND Time IS Late THEN Impact_T is Very_High;"); out.newLine();
		out.write("\tRULE 4 : IF Impact IS Very_High AND Time IS Very_Late THEN Impact_T is Very_High;"); out.newLine();
		
		out.write("\tRULE 5 : IF Impact IS High AND Time IS Early THEN Impact_T is Medium;"); out.newLine();
		out.write("\tRULE 6 : IF Impact IS High AND Time IS Medium THEN Impact_T is Medium;"); out.newLine();
		out.write("\tRULE 7 : IF Impact IS High AND Time IS Late THEN Impact_T is High;"); out.newLine();
		out.write("\tRULE 8 : IF Impact IS High AND Time IS Very_Late THEN Impact_T is High;"); out.newLine();
		
		out.write("\tRULE 9 : IF Impact IS Medium AND Time IS Early THEN Impact_T is Low;"); out.newLine();
		out.write("\tRULE 10 : IF Impact IS Medium AND Time IS Medium THEN Impact_T is Medium;"); out.newLine();
		out.write("\tRULE 11 : IF Impact IS Medium AND Time IS Late THEN Impact_T is Medium;"); out.newLine();
		out.write("\tRULE 12 : IF Impact IS Medium AND Time IS Very_Late THEN Impact_T is Medium;"); out.newLine();
		
		out.write("\tRULE 13 : IF Impact IS Low AND Time IS Early THEN Impact_T is Very_Low;"); out.newLine();
		out.write("\tRULE 14 : IF Impact IS Low AND Time IS Medium THEN Impact_T is Very_Low;"); out.newLine();
		out.write("\tRULE 15 : IF Impact IS Low AND Time IS Late THEN Impact_T is Low;"); out.newLine();
		out.write("\tRULE 16 : IF Impact IS Low AND Time IS Very_Late THEN Impact_T is Low;"); out.newLine();
		
		out.write("\tRULE 17 : IF Impact IS Very_Low AND Time IS Early THEN Impact_T is Very_Low;"); out.newLine();
		out.write("\tRULE 18 : IF Impact IS Very_Low AND Time IS Medium THEN Impact_T is Very_Low;"); out.newLine();
		out.write("\tRULE 19 : IF Impact IS Very_Low AND Time IS Late THEN Impact_T is Very_Low;"); out.newLine();
		out.write("\tRULE 20 : IF Impact IS Very_Low AND Time IS Very_Late THEN Impact_T is Very_Low;"); out.newLine();
		out.newLine();
		out.write("END_RULEBLOCK"); out.newLine();
		out.newLine();
		out.write("END_FUNCTION_BLOCK"); out.newLine();
		
		out.flush();
		out.close();
		return true;
	}
	
	
	public List<double[][]> getLinearTables() {
		return linearTables;
	}


	public List<double[][]> getLogTables() {
		return logTables;
	}


	public List<double[][]> getExpTables() {
		return expTables;
	}

	
	/****************************************************************************
	 *	Detect correct table according to time limit, impact and growth type	*
	 ****************************************************************************/
	public double[][] getCorrespondingTable(double impact, String timeSlot,
			String growth) {
		
		//double timeLimit;
		double[][] table = null;
		//	Define the timeSlot of Relationship between Nodes.
		//	Time scale is 15, 60, 180, 720, 1440, 2880, 10080, 20160, 40320, 60480 minutes
		if (timeSlot.equalsIgnoreCase("15m")) {
			//timeLimit =  15;
			table = getTable(growth, 0);
		}else if (timeSlot.equalsIgnoreCase("1h")) {
			//timeLimit =  60;
			table = getTable(growth, 1);
		}else if (timeSlot.equalsIgnoreCase("3h")) {
			//timeLimit =  180;
			table = getTable(growth, 2);
		}else if (timeSlot.equalsIgnoreCase("12h")) {
			//timeLimit =  720;
			table = getTable(growth, 3);
		}else if (timeSlot.equalsIgnoreCase("24h")) {
			//timeLimit =  1440;
			table = getTable(growth, 4);
		}else if (timeSlot.equalsIgnoreCase("48h")) {
			//timeLimit =  2880;
			table = getTable(growth, 5);
		}else if (timeSlot.equalsIgnoreCase("1 week")) {
			//timeLimit =  10080;
			table = getTable(growth, 6);
		}else if (timeSlot.equalsIgnoreCase("2 weeks")) {
			//timeLimit =  20160;
			table = getTable(growth, 7);
		}else if (timeSlot.equalsIgnoreCase("4 weeks")) {
			//timeLimit =  40320;
			table = getTable(growth, 8);
		}else if (timeSlot.equalsIgnoreCase("more..")) {
			//timeLimit =  60480;
			table = getTable(growth, 9);
		}//else
			//timeLimit =  -1;
		
		return table;
	}
	
	
	/************************************************************************************
	 *	Helper method to retrieve correct table according to time limit and growth type	*
	 ************************************************************************************/
	private double[][] getTable(String growth, int slot) {
		
		if (growth.equalsIgnoreCase("LINEAR")) {
			return linearTables.get(slot);
		}
		else if (growth.equalsIgnoreCase("FAST")) {
			return logTables.get(slot);
		}
		else if (growth.equalsIgnoreCase("SLOW")) {
			return expTables.get(slot);
		}
		
		return null;
	}
	
	
}
