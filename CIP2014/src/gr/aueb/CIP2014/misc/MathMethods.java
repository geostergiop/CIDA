package gr.aueb.CIP2014.misc;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

public class MathMethods {

	// Limit decimal digits to two in double number 'temp'
	public static double limitDecimals(double temp) {
		
		DecimalFormat noOfDigits = new DecimalFormat("0.00");
		DecimalFormatSymbols dfs = new DecimalFormatSymbols();
	    dfs.setDecimalSeparator('.');
	    noOfDigits.setDecimalFormatSymbols(dfs);
	    
	    temp = Double.valueOf(noOfDigits.format(temp));
	    
	    return temp;
	}
}
