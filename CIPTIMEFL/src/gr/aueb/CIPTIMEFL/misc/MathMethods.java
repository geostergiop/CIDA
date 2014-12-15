package gr.aueb.CIPTIMEFL.misc;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

public class MathMethods {

	// Limit decimal digits to two in double number 'temp'
	public static double limitDecimals(double temp, String format) {
		// e.g. format = "0.00"
		DecimalFormat noOfDigits = new DecimalFormat(format);
		DecimalFormatSymbols dfs = new DecimalFormatSymbols();
	    dfs.setDecimalSeparator('.');
	    noOfDigits.setDecimalFormatSymbols(dfs);
	    // Round numbers upwards from decimals >= .5
	    noOfDigits.setRoundingMode(noOfDigits.getRoundingMode().HALF_UP);
	    
	    temp = Double.valueOf(noOfDigits.format(temp));
	    
	    return temp;
	}
}
