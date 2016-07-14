package gr.aueb.CIPTIMEFL.graphs;

import gr.aueb.CIPTIMEFL.fuzzy.FuzzyMachine;
import gr.aueb.CIPTIMEFL.graphics.DisplayGraph;
import gr.aueb.CIPTIMEFL.graphs.WP;
import gr.aueb.CIPTIMEFL.misc.IO;
import gr.aueb.CIPTIMEFL.misc.MathMethods;
import gr.aueb.CIPTIMEFL.misc.StringFormatting;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.JOptionPane;

import org.neo4j.graphalgo.CostEvaluator;
import org.neo4j.graphalgo.GraphAlgoFactory;
import org.neo4j.graphalgo.PathFinder;
import org.neo4j.graphalgo.WeightedPath;
import org.neo4j.graphalgo.impl.util.DoubleEvaluator;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.PathExpanders;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.PathExpander;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;


//import com.tinkerpop.blueprints.impls.neo4j.Neo4jGraph;
//import com.tinkerpop.blueprints.impls.neo4j.Neo4jVertex;

public class BasicGraph {
	
	public List<Relation> relations = new ArrayList<Relation>();
	GraphDatabaseService graphDb;
	String path;
	
	public BasicGraph(GraphDatabaseService graphDb, String path) {
		this.graphDb = graphDb;
		this.path = path;
	}
	public BasicGraph(GraphDatabaseService graphDb) {
		this.graphDb = graphDb;
	}
	
	private enum CIRelationships implements RelationshipType
	{
	    PHYSICAL, INFORMATIONAL, SOCIAL
	}

	// CI_impact is Impact table with range: [1, 5]
	// CI_likelihood is Likelihood table with range: [0.1, 1]
	public boolean createGraph(TreeMap<String, Node> CIs, GraphDatabaseService graphDb) {

		Transaction tx = graphDb.beginTx();
		try {
			long graphAnalysisStart = System.currentTimeMillis();
			// Get all paths and export them to Excel file
			if (!allPathsToExcel(CIs)) {
				JOptionPane.showMessageDialog(null, "ERROR: Excel: Paths were not exported");
				return false;
			}

			// Get the cheapest path for inverseRisk using Dijkstra algorithm. 
			// This is the maximum-weight path for normal Risk.
			WeightedPath p = null;
			for (int r=0; r<10; r++) {

				PathFinder<WeightedPath> finder = mydijkstra(
						PathExpanders.forDirection(Direction.OUTGOING ), //Traversal.expanderForAllTypes(Direction.OUTGOING), // 
						new DoubleEvaluator("inverseRisk_"+r),
						graphDb);	

				p = minPathofAllDijkstraPaths(CIs, finder);

				if (p == null) // No paths found
					return false;

				System.out.println("START: " + p.startNode().getProperty("substation_Name") + " END: " + p.endNode().getProperty("substation_Name"));
			}

			// Get the weight of the path detected as maximum-weight path.
			System.out.println(p);		

			for ( Relationship rel : p.relationships())
			{
				Node node1 = rel.getStartNode();
				node1.setProperty("maxPath", true);			
				Node node2 = rel.getEndNode();
				node2.setProperty("maxPath", true);

				rel.setProperty("maxPath", true);
			}
			tx.success();
	
			// Render created graph
			/*
		graphDb = new GraphDatabaseFactory()
	    .newEmbeddedDatabase(new File(path));
		DisplayGraph.executeDisplayQuery(graphDb);
		registerShutdownHook( graphDb );
			 */
			DisplayGraph dg = new DisplayGraph(graphDb);
			String sb = dg.export();
			
			// Create JSON file to drive
			writeJSONfile(sb);
			
			// Compute total time spent to analyze paths in graph
			long graphAnalysisEnd = System.currentTimeMillis();
			float totalAnalysisTime =  (float)(graphAnalysisEnd - graphAnalysisStart) / 1000f;
			JOptionPane.showMessageDialog(null, "Graph Analysis Time: " + totalAnalysisTime + " seconds");
			
			// Open HTML output using default web browser
			try {
				File htmlFile = new File("index2.html");
				Desktop.getDesktop().browse(htmlFile.toURI());
			}catch(IOException z) {
				JOptionPane.showMessageDialog(null, "ERROR: "+ z.getMessage());
			}
		}
		finally {
			tx.close();
		}
		graphDb.shutdown();
		// Compute Fuzzy Logic Impact according to time
		// EXAMPLE: Calculate Risk for a given time 50h, with simple Impact=6
		// FuzzyMachine fm = new FuzzyMachine(5, 120, true);
		return true;
	}

	
	
	
	/****************************************************************************
	 *	Add User-input connections between CIs and, thus, form the graph		*
	 *																			*
	 *	The weight of each edge is: Risk = Likelihood * Impact.					*
	 *	According to CIP-2013, the Cascading Risk is: C_Risk = Sum(Risk) of all *
	 *	Risks in edges of a given path.											*
	 ****************************************************************************/
	public boolean createConnection
								(String source_substation_id, String dest_substation_id, 
								double[] impact, double likelihood, String connType, 
								TreeMap<String, Node> CIs, GraphDatabaseService graphDb,
								int timeSlot) {

				// Don't connect a CI to itself
				if (source_substation_id.equalsIgnoreCase(dest_substation_id))
					return false;
				try {
					// Calculate Risk values and limit decimals in calculated Risk to two.
					double[] risk = new double[impact.length];
					double[] inverseRisk = new double[impact.length];
					for (int i=0; i<impact.length; i++) {
						risk[i] = MathMethods.limitDecimals((impact[i] * likelihood), "0.00");
						inverseRisk[i] = -risk[i];
					}
					
					Transaction tx = graphDb.beginTx();
					Node n1 = CIs.get(source_substation_id);
					Node n2 = CIs.get(dest_substation_id);
					Relationship e;
					try {
						// Different way to create Edges using TinkerPop
						// Edge e = graph.addEdge(null, CIs.get("CI_"+i), CIs.get("CI_"+ciToConnect), ("Risk="+Double.toString(risk)));

						//	Define the type of Relationship between Nodes
						if (connType.equalsIgnoreCase("PHYSICAL")) {
							e = (n1).createRelationshipTo(n2, CIRelationships.PHYSICAL);
						}else if (connType.equalsIgnoreCase("SOCIAL")) {
							e = (n1).createRelationshipTo(n2, CIRelationships.SOCIAL);
						}else if (connType.equalsIgnoreCase("INFORMATIONAL")) {
							e = (n1).createRelationshipTo(n2, CIRelationships.INFORMATIONAL);
						}else
							return false;

						e.setProperty("timeslot", timeSlot);
						e.setProperty("isActive", 1);
						e.setProperty("impact", impact);
						e.setProperty("likelihood", likelihood);
						for (int r=0; r<risk.length; r++) {
							e.setProperty("Risk_"+r, Double.toString(risk[r]));
							// We will use an "inverseRisk" property, since searching for the maximum weight path, 
							// is the path with the smallest negative weight (negation of the normal Risk values).
							e.setProperty("inverseRisk_"+r, -risk[r]);
						}
						//To use for max-weight path when determined
						e.setProperty("maxPath", false);

						tx.success();
						//Store new relationship in a Relation object and then in an Arraylist for future reference/changes
						Relation newRelation = new Relation(true, impact, likelihood, risk, n1, n2, e);
						relations.add(newRelation);
					}
					finally {
						tx.close();
					}
					return true;
				} catch(Exception e) {
					    System.out.println("Exception in: createConnection.createGraph(): "+e.getMessage());
					    return false;
				}
			}
	
	
	
	/****************************************************************************
	 * Get all paths in the graph and export them to an Excel (.xls) file	 	*
	 ****************************************************************************/
	private boolean allPathsToExcel(TreeMap<String, Node> CIs) {

		PathFinder<Path> finder = GraphAlgoFactory.allPaths(PathExpanders.forDirection(Direction.OUTGOING ), 5);
		
		// Calculate all lists of paths for each impact Time Slot
		for(int impactTimeSlot = 0; impactTimeSlot < 10; impactTimeSlot++){

			List<WP> allPathsWithWeight = new ArrayList<WP>();
			Transaction tx = graphDb.beginTx();
			try {
				for(Map.Entry<String,Node> entry1 : CIs.entrySet()) {
					Node startvertex = entry1.getValue();
					// For each node, check paths to all other nodes
					for(Map.Entry<String,Node> entry2 : CIs.entrySet()) {
						Node endvertex = entry2.getValue();

						// Get ALL paths between 2 nodes
						if (!startvertex.equals(endvertex)) {
							for (Path allPaths : finder.findAllPaths(startvertex, endvertex)) {
								StringFormatting s = new StringFormatting();
								s.setThePath(allPaths);
								String p = s.renderAndCalculateWeight(allPaths, "substation_id", "cRisk", impactTimeSlot);
								s.setStringListPath(p);
								allPathsWithWeight.add(s.addWP());	// Add object with weight and path to sort and get the maximum one
							}
						}
					}
				}
				// Sort all possible paths according to weight (double value)
				Collections.sort(allPathsWithWeight, new Comparator<WP>() {
					@Override
					public int compare(WP c1, WP c2) {
						return Double.compare(c1.getWeight(), c2.getWeight());
					}
				});

				WP maxWP = allPathsWithWeight.get(allPathsWithWeight.size()-1);
				System.out.println("Sorted max path: " + maxWP.getPath() + " Weight: " + maxWP.getWeight());

				// Write all paths found to file
				try {
					IO.writeBuffered(allPathsWithWeight, 8192, impactTimeSlot);
					JOptionPane.showMessageDialog(null, "All paths were saved to Excel at the user's Temporary Directory"
							+ "\n(For Windows: C:\\Users\\*USERNAME*\\AppData\\Local\\Temp)");
				}catch (IOException z) {
					System.err.println("ERROR: while writing Excel files");
				}
				tx.success();
			}
			finally {
				tx.close();
			}
		}	//for(int impactTimeSlot = 0)
			return true;
	}
	

	
	/****************************************************************************
	 * Call a custom implemented Dijkstra based on org.neo4j.examples.dijkstra 	*
	 ****************************************************************************/
	private static PathFinder<WeightedPath> mydijkstra(PathExpander pathExpander,
            CostEvaluator<Double> costEvaluator, GraphDatabaseService graphDb)
    {
        return new MyDijktsra(pathExpander, costEvaluator );
    }			   
	
	
	
	/********************************************************************************
	 * Find the cheapest (dijkstra) path for each Vertex.							*
	 * This path is the maximum weight path since actual Edge weights are inversed.	*
	 * (weight = 1/actual_weight)													*
	 * ******************************************************************************/
	private WeightedPath minPathofAllDijkstraPaths(TreeMap<String, Node> CIs, PathFinder<WeightedPath> finder) {
		
		TreeMap<String, WeightedPath> paths = new TreeMap<String, WeightedPath>();
		long index = 1;
		
		for(Map.Entry<String,Node> entry1 : CIs.entrySet()) {

			Node startvertex = entry1.getValue();

			for(Map.Entry<String,Node> entry2 : CIs.entrySet()) {
				String key = Long.toString(index);	// Create a key for each path based on a counter
				index++;
				Node endvertex = entry2.getValue();

				WeightedPath p = finder.findSinglePath( startvertex, endvertex);
				
				if ( p != null && !startvertex.equals(endvertex))
					paths.put(key, p);	// FIXED: KEYS must not get replaced when same nodes are being analyzed
			}
		}
		
		String maxkey = null;
		double max = 0;
		for(Map.Entry<String,WeightedPath> entry : paths.entrySet()) {
			WeightedPath tempP = entry.getValue();
			if (tempP != null) {
				double temp = tempP.weight();
				if (max > temp) {	// Since we are searching for the maximum weight path, i.e. the path with the smallest negative weight 
					max = temp;		// (smallest negative = biggest positive weight if you change the sign
					maxkey = entry.getKey();
				}
			}
		}
		WeightedPath p = null;
		try {
			p = paths.get(maxkey);
		}catch(NullPointerException z) {
			// If no paths were found in this random graph, then exit the program
			JOptionPane.showMessageDialog(null, "DIJKSTRA: ERROR: The random graph does not contain any paths with the desired depth");
			return null;
		}
		
		return p;
	}
	
	private void writeJSONfile(String str) {
		
		FileOutputStream fop = null;
		File file;

		try {
			/*
			 * Load index.html with D3.js javascript code
			 */
			BufferedReader reader = new BufferedReader(new FileReader ("index"));
		    String         line = null;
		    StringBuilder  stringBuilder = new StringBuilder();
		    String         ls = System.getProperty("line.separator");

		    try {
		        while((line = reader.readLine()) != null) {
		            stringBuilder.append(line);
		            stringBuilder.append(ls);
		        }
		    } finally {
		        reader.close();
		    }
		    /*
		     * Replace the %X% marker with JSON data inside the <script>
		     */
		    stringBuilder.replace(stringBuilder.indexOf("%X%"), stringBuilder.indexOf("%X%")+3, str);	    
		    /*
		     * Print ready D3.js html file to drive
		     */
			file = new File("index2.html");
			fop = new FileOutputStream(file);
			
			// if file doesn't exists, then create it
			if (!file.exists()) {
				file.createNewFile();
			}

			// get the content in bytes
			byte[] contentInBytes = stringBuilder.toString().getBytes();

			fop.write(contentInBytes);
			fop.flush();
			fop.close();

			System.out.println("JSON file created.");

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (fop != null) {
					fop.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}