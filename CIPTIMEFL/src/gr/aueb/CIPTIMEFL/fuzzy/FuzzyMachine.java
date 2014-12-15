package gr.aueb.CIPTIMEFL.fuzzy;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import net.sourceforge.jFuzzyLogic.*;
import net.sourceforge.jFuzzyLogic.rule.Variable;

public class FuzzyMachine {
	
	private boolean showCharts = false;
	private double latestDefuzzifiedValue = -1;

	FIS fis;
	
	public FuzzyMachine(double variable1, double variable2, boolean showC) {
		
		showCharts = showC;
		
		// Load from 'FCL' file
		String fileName = "new_base.fcl";
		fis = FIS.load(fileName,true);
		// Error while loading?
		if( fis == null ) {
			System.err.println("Can't load file: '"+ fileName + "'");
			return;
		}

		// Show
		//fis.chart();

		// Set inputs
		fis.setVariable("Impact", variable1);
		fis.setVariable("Time", variable2);

		// Evaluate
		fis.evaluate();

		// Get values needed by Fuzzy Output
		Variable fOutput = fis.getVariable("Impact_T");
		latestDefuzzifiedValue = fOutput.getLatestDefuzzifiedValue();
		
	}//FuzzyMachine ()

	
	public void plotCharts() {
		// Show output variable's chart
		fis.getVariable("Impact_T").chartDefuzzifier(showCharts);
	}
	
	
	/****************************************
	 * Getter for defuzzified output value	*
	 ****************************************/
	public double getLatestDefuzzifiedValue() {
		return latestDefuzzifiedValue;
	}
	
	
		/****************************************************************************
	 * TODO: Add a function that can create '.fcl' files based on user input	*
	 ****************************************************************************/
	private void fclCreator () {

		String line = "";

		try {
			BufferedReader base = new BufferedReader(new FileReader(new File("base.fcl")));
			BufferedWriter fcl = new BufferedWriter(new FileWriter(new File("loggic.fcl")));

			while ((line = base.readLine()) != null)   {

				if ((line.indexOf("") != -1) || (line.indexOf("") != -1)){

				}
				else {
					fcl.write(line);
					fcl.newLine();
				}
			}

		}catch (IOException z){System.err.println("ERROR: Could not create fcl file: fclCreator() exception");}
	}
}