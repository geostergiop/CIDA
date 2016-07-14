package gr.aueb.CIPTIMEFL.misc;

import gr.aueb.CIPTIMEFL.graphs.WP;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.util.List;
import java.util.TreeMap;

public class IO {
	
	public static void writeBuffered(List<WP> records, int bufSize, int impactTimeSlot) throws IOException {
	    File file; 
	    file = File.createTempFile("AllPaths_"+impactTimeSlot+"_Time", ".xls");

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
	
	
	public static void writeArrayBuffered(double[][] records, int bufSize, String name) throws IOException {
	    File file; 
	    file = File.createTempFile(name, ".xls");

	    try {
	        FileWriter writer = new FileWriter(file);
	        BufferedWriter bufferedWriter = new BufferedWriter(writer, bufSize);

	        System.out.print("Writing array buffered (buffer size: " + bufSize + ")... ");
	        writeArray(records, bufferedWriter);
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
	
	
private static void writeArray(double[][] records, Writer writer) throws IOException {
		
		for (int i = 0; i < records.length; i++) {
			for (int j = 0; j < records[i].length; j++)
				writer.write(records[i][j] + "\t");
			writer.write("\n");
	    }
		writer.write("\n");
	    writer.flush();
	    writer.close();
	}

}
