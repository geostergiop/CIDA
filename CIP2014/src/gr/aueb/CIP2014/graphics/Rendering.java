package gr.aueb.CIP2014.graphics;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Paint;

import javax.swing.JFrame;

import org.apache.commons.collections15.Transformer;

import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.neo4j.Neo4jGraph;
import com.tinkerpop.blueprints.oupls.jung.GraphJung;

import edu.uci.ics.jung.algorithms.layout.CircleLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.visualization.BasicVisualizationServer;

public class Rendering {

	public void visualize(GraphJung<Neo4jGraph> graphjung) {
		
		Layout<Vertex, Edge> layout = new CircleLayout<Vertex, Edge>(graphjung);
		layout.setSize(new Dimension(800, 800));
		BasicVisualizationServer<Vertex, Edge> viz = new BasicVisualizationServer<Vertex, Edge>(layout);
		viz.setPreferredSize(new Dimension(900, 900));

		// Set the labels in Vertices.
		Transformer<Vertex, String> vertexLabelTransformer = new Transformer<Vertex, String>() {
			public String transform(Vertex vertex) {
				String name = (String)vertex.getProperty("substation_id") +"|"+ (String)vertex.getProperty("substation_Name");
				return name;
			}
		};

		// Set the labels in Edges.
		Transformer<Edge, String> edgeLabelTransformer = new Transformer<Edge, String>() {
			public String transform(Edge edge) {
				return edge.getLabel() + "| RISK=" + edge.getProperty("Risk");
			}
		};

		// Set the Vertex color in maximum path.	
		Transformer<Vertex, Paint> vertexPaintRed  = new Transformer<Vertex, Paint>() {
			public Paint transform(Vertex vertex) {
				if (vertex.getProperty("maxPath"))
					return Color.RED;
				else
					return Color.GREEN;
			}
		};
		
		// Set the Vertex color in maximum path.	
		Transformer<Edge, Paint> edgePaintRed  = new Transformer<Edge, Paint>() {
			public Paint transform(Edge edge) {
				if (edge.getProperty("maxPath"))
					return Color.RED;
				else
					return null;
				}
		};

		viz.getRenderContext().setEdgeLabelTransformer(edgeLabelTransformer);
		viz.getRenderContext().setVertexLabelTransformer(vertexLabelTransformer);
		viz.getRenderContext().setVertexFillPaintTransformer(vertexPaintRed);
		viz.getRenderContext().setEdgeFillPaintTransformer(edgePaintRed);

		JFrame frame = new JFrame("CIP2014 Infrastructure Representation");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().add(viz);
		frame.pack();
		frame.setVisible(true);		
	}
}
