package gr.aueb.CIPGUI2014.graphs;

import gr.aueb.CIPGUI2014.graphs.WP;
import gr.aueb.CIPGUI2014.graphics.Rendering;
import gr.aueb.CIPGUI2014.misc.IO;
import gr.aueb.CIPGUI2014.misc.MathMethods;
import gr.aueb.CIPGUI2014.misc.StringFormatting;

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
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipExpander;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;
import org.neo4j.kernel.Traversal;

import com.tinkerpop.blueprints.impls.neo4j.Neo4jGraph;
import com.tinkerpop.blueprints.impls.neo4j.Neo4jVertex;
import com.tinkerpop.blueprints.oupls.jung.GraphJung;

public class BasicGraph {
	
	public List<Relation> relations = new ArrayList<Relation>();
	
	private enum CIRelationships implements RelationshipType
	{
	    PHYSICAL, INFORMATIONAL, SOCIAL
	}

	// CI_impact is Impact table with range: [1, 5]
	// CI_likelihood is Likelihood table with range: [0.1, 1]
	public boolean createGraph(Neo4jGraph graph, TreeMap<String, Neo4jVertex> CIs, GraphDatabaseService graphDb) {
		
		long graphAnalysisStart = System.currentTimeMillis();
		// Get all paths and export them to Excel file
		if (!allPathsToExcel(CIs)) {
			JOptionPane.showMessageDialog(null, "ERROR: Excel: Paths were not exported");
			return false;
		}
		
		// Get the cheapest path for inverseRisk using Dijkstra algorithm. 
		// This is the maximum-weight path for normal Risk.
		PathFinder<WeightedPath> finder = mydijkstra(
				Traversal.expanderForAllTypes(Direction.OUTGOING), //PathExpander.(CIRelationships.PHYSICAL, Direction.BOTH ), 
				new DoubleEvaluator("inverseRisk"));	
		
		WeightedPath p = minPathofAllDijkstraPaths(CIs, finder);
		
		if (p == null) // No paths found
			return false;
		
		System.out.println("START: " + p.startNode() + " END: " + p.endNode());
		
		// Get the weight of the path detected as maximum-weight path.
		System.out.println(p);		
		Transaction tx = graphDb.beginTx();
		for ( Relationship rel : p.relationships())
		{
		    Node node1 = rel.getStartNode();
			node1.setProperty("maxPath", true);			
			Node node2 = rel.getEndNode();
			node2.setProperty("maxPath", true);
			
			rel.setProperty("maxPath", true);
		}
		tx.success();
 		tx.finish();
		graph.commit();
		//graphDb.shutdown();
		
		// Render created graph online
		Rendering render = new Rendering();
		render.visualize(new GraphJung<Neo4jGraph>(graph));

		// Compute total time spent to analyze paths in graph
		long graphAnalysisEnd = System.currentTimeMillis();
		float totalAnalysisTime =  (float)(graphAnalysisEnd - graphAnalysisStart) / 1000f;
		JOptionPane.showMessageDialog(null, "Graph Analysis Time: " + totalAnalysisTime + " seconds");
		
		graph.shutdown();
		
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
		(String source_substation_id, String dest_substation_id, double impact, double likelihood, String connType, TreeMap<String, Neo4jVertex> CIs, GraphDatabaseService graphDb) {

				// Don't connect a CI to itself
				if (source_substation_id.equalsIgnoreCase(dest_substation_id))
					return false;
				try {
					// Calculate Risk values
					double risk = impact * likelihood;
					// Limit decimals in calculated Risk to two.
					risk = MathMethods.limitDecimals(risk);

					Transaction tx = graphDb.beginTx();
					Node n1 = CIs.get(source_substation_id).getRawVertex();
					Node n2 = CIs.get(dest_substation_id).getRawVertex();
					
					// Different way to create Edges using TinkerPop
					// Edge e = graph.addEdge(null, CIs.get("CI_"+i), CIs.get("CI_"+ciToConnect), ("Risk="+Double.toString(risk)));
					
					Relationship e;
					//	Define the type of Relationship between Nodes
					if (connType.equalsIgnoreCase("PHYSICAL")) {
						e = (n1).createRelationshipTo(n2, CIRelationships.PHYSICAL);
					}else if (connType.equalsIgnoreCase("SOCIAL")) {
						e = (n1).createRelationshipTo(n2, CIRelationships.SOCIAL);
					}else if (connType.equalsIgnoreCase("INFORMATIONAL")) {
						e = (n1).createRelationshipTo(n2, CIRelationships.INFORMATIONAL);
					}else
						return false;
					
					e.setProperty("isActive", 1);
					e.setProperty("impact", impact);
					e.setProperty("likelihood", likelihood);
					e.setProperty("Risk", Double.toString(risk));
					// We will use an "inverseRisk" property, since searching for the maximum weight path, 
					// is the path with the smallest negative weight (negation of the normal Risk values).
					e.setProperty("inverseRisk", -risk);
					//To use for max-weight path when determined
					e.setProperty("maxPath", false);
					tx.success();
			 		tx.finish();
					
					//Store new relationship in a Relation object and then in an Arraylist for future reference/changes
					Relation newRelation = new Relation(true, impact, likelihood, risk, n1, n2, e);
					relations.add(newRelation);
					
					return true;
				} catch(Exception e) {
					    System.out.println("Exception in: createConnection.createGraph(): "+e.getMessage());
					    return false;
				}
			}
	
	
	
	/****************************************************************************
	 * Get all paths in the graph and export them to an Excel (.xls) file	 	*
	 ****************************************************************************/
	private boolean allPathsToExcel(TreeMap<String, Neo4jVertex> CIs) {

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
	private static PathFinder<WeightedPath> mydijkstra( RelationshipExpander expander,
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
			return null;
		}
		
		return p;
	}
}