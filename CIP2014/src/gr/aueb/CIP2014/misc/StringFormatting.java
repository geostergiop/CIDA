package gr.aueb.CIP2014.misc;

import gr.aueb.CIP2014.graphs.WP;
import gr.aueb.CIP2014.misc.MathMethods;

import java.util.ArrayList;
import java.util.List;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.PropertyContainer;
import org.neo4j.graphdb.Relationship;

public class StringFormatting {
	
	double weight = 0;
	Path thePath = null;
	String strPath = null;

	private String toString(Node n, String prop) { return "("+n.getProperty(prop)+")"; }
	
	private String toString(Relationship r, String prop) {
		Double d = (double)r.getProperty(prop);
		weight = weight + d;
		return "--["+r.getType().name()+" - " + r.getProperty(prop) + "]-->"; 
	}
	
	public void setThePath(Path path) {
		thePath = path;
	}
	
	public void setStringListPath(String strP) {
		strPath = strP;
	}
	
	// Runs for each Path separately
	public String renderAndCalculateWeight(Path path, String nodeProp, String edgeProp) {
		List<Double> likelihoods = new ArrayList<Double>();
		StringBuilder result=new StringBuilder();
		for (PropertyContainer pc : path) {
			
			if (pc instanceof Node) 
				result.append(toString((Node)pc,nodeProp));
			else {
				Relationship r = (Relationship)pc;
				likelihoods.add((double)(r.getProperty("likelihood")));
				double currLikelihood = 1;
				// From start of path till current relationship, compute current risk
				for (double l : likelihoods){
					currLikelihood = currLikelihood * l;
				}
				double cRisk = currLikelihood * ((double)r.getProperty("impact"));
				r.setProperty("cRisk", cRisk);
				
				result.append(toString(r, edgeProp));				
			}
		}
		weight = MathMethods.limitDecimals(weight);
		//System.out.println("Path weight = " + weight);
		return result.toString();
	}
	
	public List<String> editStringData(List<WP> records) {
		List<String> edited = new ArrayList<String>();
		
		for (WP record: records) {
			String strPath = record.getStrPath();
			strPath = strPath.replaceAll("\\(", "");
			strPath = strPath.replaceAll("\\)", "");
			strPath = strPath.replaceAll("-->", "--");
			strPath = strPath.replaceAll("--", "\t");
			strPath = strPath.concat("\t"+record.getWeight());
	        edited.add(strPath);
	    }
		return edited;
	}
	
	public WP addWP() {
		WP wp = new WP(thePath, strPath, weight);
		return wp;
	}
}
