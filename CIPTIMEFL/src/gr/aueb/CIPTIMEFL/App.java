package gr.aueb.CIPTIMEFL;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.TreeMap;

import javax.swing.JOptionPane;

import org.apache.commons.io.FileUtils;
//import org.apache.commons.configuration.BaseConfiguration; 
//import org.apache.commons.configuration.Configuration;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.factory.GraphDatabaseSettings;

import gr.aueb.CIPTIMEFL.fuzzy.FCLCreator;
import gr.aueb.CIPTIMEFL.fuzzy.FuzzyMachine;
import gr.aueb.CIPTIMEFL.graphs.BasicGraph;
import gr.aueb.CIPTIMEFL.misc.MathMethods;

public class App {
	
	//private Neo4jGraph graph;
	// Create a TREEMAP Object containing all CIs generated
	private TreeMap<String, Node> CIs = new TreeMap<String, Node>();
	BasicGraph g;
	String path = null;
 	private int i = 0;
 	GraphDatabaseService graphDb;
 	// Create the Impact table database to use for Fuzzy Logic Impact calculations.
 	FCLCreator fcl = new FCLCreator();
 	
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
 		
 		Transaction tx = graphDb.beginTx();
 		try {
	 		Node ci = graphDb.createNode();
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
	 		
	 		Iterator it = graphDb.getAllNodes().iterator();
	 		while (it.hasNext())
	 			System.out.println(((Node)it.next()).getProperty("substation_Name"));
	 		
	 		tx.success();
	 		
	 		return (inputSubstation+"|"+substation_id);
 		}
 		finally {
			tx.close();
		}
 	}
 	
 	
 	/************************************
 	 * 		Initialize a new graph		*
 	 ************************************/
 	public boolean newGraph(String path) {
 		this.path = path;
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
		graphDb = new GraphDatabaseFactory()
	    .newEmbeddedDatabaseBuilder(dir)
	    .loadPropertiesFromFile("neo4j.properties")
	    .setConfig( GraphDatabaseSettings.pagecache_memory, "512M" )
		.setConfig( GraphDatabaseSettings.string_block_size, "60" )
    	.setConfig( GraphDatabaseSettings.array_block_size, "300" )
	    .newGraphDatabase();
		registerShutdownHook( graphDb );
		
		Transaction tx = graphDb.beginTx();
		try
		{
			Iterator<Node> it = graphDb.getAllNodes().iterator();
	 		while (it.hasNext())
	 			((Node)it.next()).delete();
		    // Database operations go here
		    tx.success();
		}
		finally {
		    tx.close();
		}
		
		return true;
 	}

 	
 	/************************************
 	 * 		Load a previous graph		*
 	 ************************************/
 	public GraphDatabaseService loadGraph(String path) {
 		this.path = path;
 		File dir;
 		if (path != null)
 			dir = new File(path);
 		else
 			return null;	
		try {
			// Load a Neo4J graph
			graphDb = new GraphDatabaseFactory()
		    .newEmbeddedDatabase(dir);
			registerShutdownHook( graphDb );
			//graph = new Neo4jGraph(graphDb);
			
			g = new BasicGraph(graphDb, path);
			if (g != null) {
				CIs = new TreeMap<String, Node>();
				Transaction tx = graphDb.beginTx();
				try
				{
					// Database operations go here
					for (Node node : graphDb.getAllNodes()) {
						Node v = (Node) node;
						CIs.put(v.getProperty("substation_id").toString(), v);
					}
					// Reset all max path flags in graph edges from previous analysis
					for (Relationship edge : graphDb.getAllRelationships()) {
						edge.setProperty("maxPath", false); 
					}

					tx.success();
				}
				finally {
					tx.close();
				}
			}else {
				throw new NullPointerException("ERROR::LOAD GRAPH: Cannot create object g from given graph");
			}
		} catch (Exception e1) {
			JOptionPane.showMessageDialog(null, "Cannot load graph from given path", "Error", JOptionPane.ERROR_MESSAGE);
			e1.printStackTrace();
			return null;
		}		
		return graphDb;
 	}
 	
 	
 	/********************************************
 	 * 	Call createConnection() from BasicGraph *
 	 * 	to create a new connection				*
 	 ********************************************/
 	public boolean addEdgeToCIs(String sourceCI, String destCI, 
 								String connType, double impact, 
 								double likelihood, String timeSlot, 
 								String growth) {
 		// Break String into 2 parts and send (substation_id) to create Vertex connection
 		String delims = "[|]+";
 		//String[] timePoints = {"15m", "1h", "3h", "12h", "24h", "48h", "1 week", "2 weeks", "4 weeks", "more.."};
 		double[] timeScale = {15, 60, 180, 720, 1440, 2880, 10080, 20160, 40320, 60480};
 		String[] src_array = sourceCI.split(delims);
 		String[] dst_array = destCI.split(delims);
 		
 		// Get table according to impact and time limit
 		double[][] table = fcl.getCorrespondingTable(impact, timeSlot, growth);
 		if (fcl.generateFCL(table))
 			JOptionPane.showMessageDialog(null, "Fuzzy Logic System is now configured.", "CIDA", JOptionPane.INFORMATION_MESSAGE);
 		else
 			JOptionPane.showMessageDialog(null, "ERROR: Cannot configure the Fuzzy Logic System.", "CIDA", JOptionPane.ERROR_MESSAGE);

 		// Iterate over the impact column IMPACT in the chosen two-dimensional TABLE and get only impact_T up until Tmax (scaleMaxIndex):
 		double[] fImpact = new double[10];
 		for (int i = 0; i < 10; i++) {
 			fImpact[i] = table[i][(int) impact - 1];
 		}

 		/* ******************************************** *
 		 * Compute Fuzzy Logic Impact according to time *
 		 * 												* 
 		 * ******************************************** */
 		double[] impact_t = new double[fImpact.length];
 		int index = 0;
 		for (double fI : fImpact) {
	 		FuzzyMachine fm = new FuzzyMachine(fI, timeScale[index], false);
	 		double temp = MathMethods.limitDecimals(fm.getLatestDefuzzifiedValue(), "0.0");
	 		// Limit RightMost defuzzyfied value to maximum worst-case impact 
	 		// given by auditor (limits impact from going sky-high in certain scenarios).
	 		impact_t[index] = temp > impact ? impact : temp; 
	 		//System.out.println("\tI --> "+impact_t[index]);
	 		index++;
 		}
 		g = new BasicGraph(graphDb, path);
 		if (g == null)
 			return false;
 		else if(g.createConnection(src_array[1], dst_array[1], impact_t, likelihood, connType, CIs, graphDb, impact_t.length-1))
 			return true;
 		
 		return false;
 	}

 	
 	/****************************************************
 	 * 		Analyze a ready graph and plot diagrams		*
 	 ****************************************************/
 	public boolean startGraphAnalysis(){
 		if (!CIs.isEmpty()) {
 			if (g.createGraph(CIs, graphDb)) {
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
 		graphDb.shutdown();
 	}
 	
 	public TreeMap<String, Node> getCIs () {
 		return CIs;
 	}
 	
 	private static void registerShutdownHook( final GraphDatabaseService graphDb )
 	{
 	    // Registers a shutdown hook for the Neo4j instance so that it
 	    // shuts down nicely when the VM exits (even if you "Ctrl-C" the
 	    // running application).
 	    Runtime.getRuntime().addShutdownHook( new Thread()
 	    {
 	        @Override
 	        public void run()
 	        {
 	            graphDb.shutdown();
 	        }
 	    } );
 	}
}
