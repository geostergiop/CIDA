package gr.aueb.CIP2014.misc;

import gr.aueb.CIP2014.graphs.WP;
import gr.aueb.CIP2014.misc.StringFormatting;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.List;

public class IO {

	// If true, exports paths to Excel file (.xls), otherwise to text (.txt)
	static boolean TOEXCEL = true;
	
	
	public static void writeBuffered(List<WP> records, int bufSize) throws IOException {
	    File file; 
	    file = File.createTempFile("AllPaths", ".xls");

	    try {
	        FileWriter writer = new FileWriter(file);
	        BufferedWriter bufferedWriter = new BufferedWriter(writer, bufSize);

	        System.out.print("Writing buffered (buffer size: " + bufSize + ")... ");
	        write(records, bufferedWriter);
	    } finally {
	        // comment this out if you want to inspect the files afterward
	        //file.delete();
	    }
	}
	
	
	private static void write(List<WP> records, Writer writer) throws IOException {
		
		//Edit line to replace characters for Excel
		List<String> recordsToStr = new StringFormatting().editStringData(records);
		
	    long start = System.currentTimeMillis();
	    for (String record: recordsToStr) {
	        writer.write(record+"\n");
	    }
	    writer.flush();
	    writer.close();
	    long end = System.currentTimeMillis();
	    System.out.println((end - start) / 1000f + " seconds");
	}
}
