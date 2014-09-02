package gr.aueb.CIPGUI2014;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.TreeMap;

import javax.swing.JOptionPane;

import org.apache.commons.io.FileUtils;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.neo4j.Neo4jVertex;
import com.tinkerpop.blueprints.impls.neo4j.Neo4jGraph;

import gr.aueb.CIPGUI2014.graphs.BasicGraph;
import gr.aueb.CIPGUI2014.misc.IO;

public class App {
	
	private Neo4jGraph graph;
	// Create a TREEMAP Object containing all CIs generated
	private TreeMap<String, Neo4jVertex> CIs = new TreeMap<String, Neo4jVertex>();
	BasicGraph g = new BasicGraph();
 	private int i = 0;
 	GraphDatabaseService graphDb;
 	
 	/****************************************
 	 * Add a new Vertex to current graph	*
 	 ****************************************/
 	public String addVertexToGraph(String dropdownSectors, String inputSubsector,
 			String inputSubstation, String inputLatitude, String inputLongtitude, boolean isInit) {
 		double longtitude, latitude;
 		try {
 			latitude = Double.parseDouble(inputLatitude);
 			longtitude = Double.parseDouble(inputLongtitude);
 		}catch (NumberFormatException n) { return null; }
 		try {
 			Transaction tx = graphDb.beginTx();
	 		Neo4jVertex ci = (Neo4jVertex) graph.addVertex(null);
	 		i++;
	 		ci.setProperty("CI_ID", ("CI_"+(i)));
	 		ci.setProperty("ci_sector", dropdownSectors);
	 		ci.setProperty("ci_subsector", inputSubsector);
	 		String substation_id = ci.getProperty("CI_ID")+"-"+Integer.toString(i);
	 		ci.setProperty("substation_id", substation_id); // Format is: "CI-ID"-X, where X an incremental number
	 		ci.setProperty("substation_Name", inputSubstation);
	 		ci.setProperty("CI_OPERATOR", ("Infrastructure_"+(i)));
	 		ci.setProperty("location_latitude", latitude);
	 		ci.setProperty("location_longtitude", longtitude);
	 		ci.setProperty("maxPath", false);
	 		ci.setProperty("init", isInit);
	
	 		CIs.put(substation_id, ci);
	 		tx.success();
	 		tx.finish();
	 		
	 		Iterator it = graph.getVertices().iterator();
	 		while (it.hasNext())
	 			System.out.println(((Neo4jVertex)it.next()).getProperty("substation_Name"));
	 		
	 		return (inputSubstation+"|"+substation_id);
 		}catch (Exception z)
 			{ return null;}
 	}
 	
 	
 	/************************************
 	 * 		Initialize a new graph		*
 	 ************************************/
 	public boolean newGraph(String path) {
 		File dir;
 		if (path != null)
 			dir = new File(path);
 		else
 			return false;
		
		try {
			FileUtils.deleteDirectory(dir);
		} catch (IOException e1) {
			e1.printStackTrace();
		}		
		// Create a Neo4J graph store info inside his database
		graphDb = new GraphDatabaseFactory().newEmbeddedDatabase(path);
		graph = new Neo4jGraph(graphDb);
		
		Iterator<Vertex> it = graph.getVertices().iterator();
 		while (it.hasNext())
 			graph.removeVertex(it.next());
		
		return true;
 	}

 	
 	/************************************
 	 * 		Load a previous graph		*
 	 ************************************/
 	public boolean loadGraph(String path) {
 		File dir;
 		if (path != null)
 			dir = new File(path);
 		else
 			return false;	
		try {
			// Load a Neo4J graph
			graphDb = new GraphDatabaseFactory().newEmbeddedDatabase(path);
			graph = new Neo4jGraph(graphDb);
			
			CIs = new TreeMap<String, Neo4jVertex>();
			for (Vertex vertex : graph.getVertices()) {
				Neo4jVertex v = (Neo4jVertex) vertex;
				CIs.put(v.getProperty("substation_id").toString(), v);
			}
			// Reset all max path flags in graph edges from previous analysis
			for (Edge edge : graph.getEdges()) {
				edge.setProperty("maxPath", false); 
			}
		} catch (Exception e1) {
			JOptionPane.showMessageDialog(null, "Cannot load graph from given path", "Error", JOptionPane.ERROR_MESSAGE);
			e1.printStackTrace();
			return false;
		}		
		return true;
 	}
 	
 	
 	/********************************************
 	 * 	Call createConnection() from BasicGraph *
 	 * 	to create a new connection				*
 	 ********************************************/
 	public boolean addEdgeToCIs(String sourceCI, String destCI, String connType, double impact, double likelihood) {
 		// Break String into 2 parts and send (substation_id) to create Vertex connection
 		String delims = "[|]+";
 		String[] src_array = sourceCI.split(delims);
 		String[] dst_array = destCI.split(delims);
 		
 		if (g == null)
 			return false;
 		else if(g.createConnection(src_array[1], dst_array[1], impact, likelihood, connType, CIs, graphDb))
 			return true;
 		
 		return false;
 	}

 	
 	/****************************************************
 	 * 		Analyze a ready graph and plot diagrams		*
 	 ****************************************************/
 	public boolean startGraphAnalysis(){
 		if (!CIs.isEmpty()) {
 			if (g.createGraph(graph, CIs, graphDb)) {
 				return true;
 			}
 			else
 				return false;
 		}
 		else
 			return false;
 	}
 
 	
 	/****************************
 	 * 		Reset the graph		*
 	 ****************************/
 	public void resetGraphAndDatabase() {
 		graph.stopTransaction(null);
 		graph.shutdown();
 	}
 	
 	public TreeMap<String, Neo4jVertex> getCIs () {
 		return CIs;
 	}
}
