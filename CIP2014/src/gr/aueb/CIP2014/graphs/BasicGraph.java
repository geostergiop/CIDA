package gr.aueb.CIP2014.graphs;

import gr.aueb.CIP2014.graphics.Rendering;
import gr.aueb.CIP2014.misc.IO;
import gr.aueb.CIP2014.misc.MathMethods;
import gr.aueb.CIP2014.misc.StringFormatting;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

import javax.swing.JOptionPane;

import org.apache.commons.io.FileUtils;
import org.neo4j.graphalgo.CostEvaluator;
import org.neo4j.graphalgo.GraphAlgoFactory;
import org.neo4j.graphalgo.PathFinder;
import org.neo4j.graphalgo.WeightedPath;
import org.neo4j.graphalgo.impl.util.DoubleEvaluator;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipExpander;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.kernel.Traversal;

import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.impls.neo4j.Neo4jGraph;
import com.tinkerpop.blueprints.impls.neo4j.Neo4jVertex;
import com.tinkerpop.blueprints.oupls.jung.GraphJung;

public class BasicGraph {
	
	
	enum CIRelationships implements RelationshipType
	{
	    PHYSICAL, INFORMATIONAL
	}
	
	Neo4jGraph graph;

	// CI_impact is Impact table with range: [1, 5]
	// CI_likelihood is Likelihood table with range: [0.1, 1]
	public void createGraph(int number, int[][] CI_impact, double[][] CI_likelihood) {
		String path = "CIP_graph.db";
		File dir = new File(path);
		
		try {
			FileUtils.deleteDirectory(dir);
		} catch (IOException e1) {
			e1.printStackTrace();
		}		
		graph = new Neo4jGraph(path);

		// Create a TREEMAP Object containing all CIs generated
		TreeMap<String, Neo4jVertex> CIs = new TreeMap<String, Neo4jVertex>();

		// Create random CIs and fill them with information
		CIs = createRandomCIsWithInfo(number, CIs, graph);

		// Create random connections between CIs
		createRandomConnectionsToCIs(number, CI_impact, CI_likelihood, CIs);
		
		long graphAnalysisStart = System.currentTimeMillis();
		// Get all paths and export them to Excel file
		if (!allPathsToExcel(CIs)) {
			JOptionPane.showMessageDialog(null, "ERROR: Excel: Paths were not exported");
			System.exit(-1);
		}
		
		// Get the cheapest path for inverseRisk using Dijkstra algorithm. 
		// This is the maximum-weight path for normal Risk.
		PathFinder<WeightedPath> finder = mydijkstra(
				Traversal.expanderForAllTypes(Direction.OUTGOING), //PathExpander.(CIRelationships.PHYSICAL, Direction.BOTH ), 
				new DoubleEvaluator("inverseRisk"));		
		
		WeightedPath p = minPathofAllDijkstraPaths(CIs, finder);
		
		System.out.println("START: " + p.startNode() + " END: " + p.endNode());
		
		
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
	
		graph.commit();
		
		// Render created graph online
		Rendering render = new Rendering();
		render.visualize(new GraphJung<Neo4jGraph>(graph));

		// Compute total time spent to analyze paths in graph
		long graphAnalysisEnd = System.currentTimeMillis();
		float totalAnalysisTime =  (float)(graphAnalysisEnd - graphAnalysisStart) / 1000f;
		JOptionPane.showMessageDialog(null, "Graph Analysis Time: " + totalAnalysisTime + " seconds");
		//graph.shutdown();
	}
	
	
	
	
	/****************************************************************************
	 *	Create random CIs, fill them with info and, thus, form the graph		*
	 ****************************************************************************/
	private TreeMap<String, Neo4jVertex> createRandomCIsWithInfo (int number, TreeMap<String, Neo4jVertex> CIs, Neo4jGraph graph) {
		
		/* Generate a list of CI_SECTORS and randomly choose one for each CI
		 *
		 * ============================================================
		 * Critical Infrastructure Sectors - USA Homeland Security List 
		 * (https://www.dhs.gov/critical-infrastructure-sectors)
		 * ============================================================
		 * PPD-21 identifies 16 critical infrastructure sectors:
		 * 
		 * - Chemical Sector
		 * - Commercial Facilities Sector
		 * - Communications Sector
		 * - Critical Manufacturing Sector
		 * - Dams Sector
		 * - Defense Industrial Base Sector
		 * - Emergency Services Sector
		 * - Energy Sector
		 * - Financial Services Sector
		 * - Food and Agriculture Sector
		 * - Government Facilities Sector
		 * - Healthcare and Public Health Sector
		 * - Information Technology Sector
		 * - Nuclear Reactors, Materials, and Waste Sector
		 * - Transportation Systems Sector
		 * - Water and Waste-water Systems Sector
		 */
		List<String> randomSector = new LinkedList<String>();
		randomSector.add("Chemical");
		randomSector.add("Commercial Facilities");
		randomSector.add("Communications");
		randomSector.add("Critical Manufacturing");
		randomSector.add("Dams");
		randomSector.add("Defense Industrial Base");
		randomSector.add("Emergency Services");
		randomSector.add("Energy");
		randomSector.add("Financial Services");
		randomSector.add("Food and Agriculture");
		randomSector.add("Government Facilities");
		randomSector.add("Healthcare and Public Health");
		randomSector.add("Information Technology");
		randomSector.add("Nuclear Reactors, Materials, and Waste");
		randomSector.add("Transportation Systems");
		randomSector.add("Water and Wastewater Systems");
		
		// Create random CIs
		for (int i=0; i<number; i++) {
			Neo4jVertex ci = (Neo4jVertex) graph.addVertex(null);
			ci.setProperty("CI_ID", ("CI_"+(i+1)));
			
			Collections.shuffle(randomSector);
			ci.setProperty("ci_sector", randomSector.get(0));
			
			ci.setProperty("ci_subsector", "NULL");
			
			String substation_id = ci.getProperty("CI_ID")+"-"+Integer.toString(i+1);
			ci.setProperty("substation_id", substation_id); //Μορφή "CI-ID"-X όπου Χ αύξοντας αριθμός
			
			ci.setProperty("substation_Name", "CI_Name_"+(i+1));
			ci.setProperty("CI_OPERATOR", ("Infrastructure_"+(i+1)));
			ci.setProperty("location_latitude", "15");
			ci.setProperty("location_longtitude", "51");
			ci.setProperty("maxPath", false);
			
			CIs.put("CI_"+i, ci);
		}
		return CIs;
	}
	
	
	
	/****************************************************************************
	 *	Create random connections between CIs and, thus, form the graph			*
	 *																			*
	 *	The weight of each edge is: Risk = Likelihood * Impact.					*
	 *	According to CIP-2013, the Cascading Risk is: C_Risk = Sum(Risk) of all *
	 *	Risks in edges of a given path.											*
	 ****************************************************************************/
	private void createRandomConnectionsToCIs
		(int number, int[][] CI_impact, double[][] CI_likelihood, TreeMap<String, Neo4jVertex> CIs) {
		
		Random intRandom = new Random();
		int numOfEdgesPerGraph;
		
		for (int i=0; i<number; i++) {
			numOfEdgesPerGraph = intRandom.nextInt(3 - 1 + 1) + 1;
			for (int j=0; j<numOfEdgesPerGraph; j++) {
				// Randomly choose another CI to create an Edge in graph
				int ciToConnect = intRandom.nextInt((number-1) - 0 + 1) + 0; //number-1 so as not to produce a '10', since CI_9 is the last one if number is 10
				// Don't connect a CI to itself
				while (ciToConnect == i)
					ciToConnect = intRandom.nextInt((number-1) - 0 + 1) + 0;
				
				try {
					Node n1 = CIs.get("CI_"+i).getRawVertex();
					Node n2 = CIs.get("CI_"+ciToConnect).getRawVertex();
					double risk = CI_impact[i][ciToConnect] * CI_likelihood[i][ciToConnect];
					
					// Different way to create Edges using TinkerPop
					// Edge e = graph.addEdge(null, CIs.get("CI_"+i), CIs.get("CI_"+ciToConnect), ("Risk="+Double.toString(risk)));
					Relationship e = (n1).createRelationshipTo(n2, CIRelationships.PHYSICAL);
					
					e.setProperty("isActive", 1);
					e.setProperty("impact", (double)(CI_impact[i][ciToConnect]));
					e.setProperty("likelihood", (double)(CI_likelihood[i][ciToConnect]));
					// We will use an "inverseRisk" property, since searching for the maximum weight path, 
					// is the path with the smallest negative weight (negation of the normal Risk values).
					e.setProperty("risk", risk);
					e.setProperty("inverseRisk", -risk);
					// A flag to use for the max-weight path when determined
					e.setProperty("maxPath", false);
					
				} catch(NullPointerException e) {
					    System.out.println("NullPointerException in: BasicGraph.createGraph() | Instruction: graph.addEdge()");
				}
			}
		}
	}
	
	
	
	
	/****************************************************************************
	 * Get all paths in the graph and export them to an Excel (.xls) file	 	*
	 ****************************************************************************/
	private boolean allPathsToExcel(TreeMap<String, Neo4jVertex> CIs) {

		List<String> pathList = new ArrayList<String>();
		List<WP> allPathsWithWeight = new ArrayList<WP>();
		PathFinder<Path> finder = GraphAlgoFactory.allPaths(Traversal.expanderForAllTypes(Direction.OUTGOING), 5);
		
		for(Map.Entry<String,Neo4jVertex> entry1 : CIs.entrySet()) {
			
			Neo4jVertex startvertex = entry1.getValue();
			// For each node, check paths to all other nodes
			for(Map.Entry<String,Neo4jVertex> entry2 : CIs.entrySet()) {
				  Neo4jVertex endvertex = entry2.getValue();
				  
				  // Get ALL paths between 2 nodes
				  if (!startvertex.equals(endvertex)) {
					  for (Path allPaths : finder.findAllPaths(startvertex.getRawVertex(), endvertex.getRawVertex())) {
						  StringFormatting s = new StringFormatting();
						  s.setThePath(allPaths);
						  String p = s.renderAndCalculateWeight(allPaths, "substation_id", "cRisk");
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
			IO.writeBuffered(allPathsWithWeight, 8192);
			JOptionPane.showMessageDialog(null, "All paths were saved to Excel at the user's Temporary Directory"
					+ "\n(Usually in: C:\\Users\\*USERNAME*\\AppData\\Local\\Temp)");
		}catch (IOException z) {
			System.err.println("ERROR: while writing Excel files");
		}
		return true;
	}


	
	/****************************************************************************
	 * Call a custom implemented Dijkstra based on org.neo4j.examples.dijkstra 	*
	 ****************************************************************************/
	public static PathFinder<WeightedPath> mydijkstra( RelationshipExpander expander,
            CostEvaluator<Double> costEvaluator )
    {
        return new MyDijkstra( expander, costEvaluator );
    }
	
	
	
	/********************************************************************************
	 * Find the cheapest (dijkstra) path for each Vertex.							*
	 * This path is the maximum weight path since actual Edge weights are inversed.	*
	 * (weight = 1/actual_weight)													*
	 * ******************************************************************************/
	private WeightedPath minPathofAllDijkstraPaths(TreeMap<String, Neo4jVertex> CIs, PathFinder<WeightedPath> finder) {
		
		TreeMap<String, WeightedPath> paths = new TreeMap<String, WeightedPath>();
		long index = 1;
		
		for(Map.Entry<String,Neo4jVertex> entry1 : CIs.entrySet()) {

			Neo4jVertex startvertex = entry1.getValue();

			for(Map.Entry<String,Neo4jVertex> entry2 : CIs.entrySet()) {
				String key = Long.toString(index);	// Create a key for each path based on a counter
				index++;
				Neo4jVertex endvertex = entry2.getValue();

				WeightedPath p = finder.findSinglePath( startvertex.getRawVertex(), endvertex.getRawVertex());
				
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
			System.exit(-1);
		}
		
		return p;
	}
}