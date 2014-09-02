package gr.aueb.CIPGUI2014.graphs;

import org.neo4j.graphdb.Path;

/*
 * 	Just a simple object that contains a path and its weight.
 * 	Created in order to return to stuff from StringFormatting.java
 * 	and sort them afterwards in BasicGraph.java.
 * 
 * */

public class WP {
	
	private double weight;
	private Path path;
	private String strPath;
	
	public WP(Path p, String strP, double w) {
		path = p;
		strPath = strP;
		weight = w;
	}
	
	public double getWeight() { return weight; }
	public Path getPath() { return path; }
	public String getStrPath() { return strPath; }

}