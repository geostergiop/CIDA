package gr.aueb.CIPTIMEFL.graphics;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.neo4j.graphdb.*;

import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author mh
 * @since 22.04.12
 */
public class DisplayGraph {
    private final GraphDatabaseService gdb;
    private Gson gson = new GsonBuilder().disableHtmlEscaping().create();

    public DisplayGraph(GraphDatabaseService gdb) {
        this.gdb = gdb;
    }

    public String export() {
        Transaction tx = gdb.beginTx();
        try {
            StringBuilder sb = new StringBuilder();
            init(sb);
            int count = appendNodes(sb);
            count = appendRelationships(sb, count);
            if (count > 0) return sb.toString();
            return "";
        } finally {
            tx.success();
            tx.close();
        }
    }
    
    private void init(StringBuilder sb) {
        sb.append("{ \n");
    }

    private int appendRelationships(StringBuilder sb, int count) {
    	sb.append("\"edges\":[ \n");
    	
    	Iterator<Node> nodes = gdb.getAllNodes().iterator();
        while (nodes.hasNext()) {
        	Node node = nodes.next();
        	Iterator<Relationship> rels = node.getRelationships(Direction.OUTGOING).iterator();
        	if (!rels.hasNext() && !nodes.hasNext()) {
        		sb = sb.delete(sb.length()-2, sb.length()-1);
        	}
        	while (rels.hasNext()) {
        		Relationship rel = rels.next();
        		long source = rel.getStartNode().getId();
        		long end = rel.getEndNode().getId();
                count++;
                appendRelationship(sb, rel, source, end);
                if (rels.hasNext() || nodes.hasNext())
                	sb.append(",\n");
            }
        }
        sb.append("] \n }");
        return count;
    }

    private void appendRelationship(StringBuilder sb, Relationship rel, long source, long end) {
        formatProperties(sb, rel, source, end);
    }

    private int appendNodes(StringBuilder sb) {
        int count = 0;
        sb.append("\"nodes\":[ \n");
        Iterator<Node> nodes = gdb.getAllNodes().iterator();
        while (nodes.hasNext()) {
        	Node node = nodes.next();
        	count++;
            appendNode(sb, node);
            if (nodes.hasNext())
            	sb.append(",\n");
        }
        sb.append("\n],\n");
        return count;
    }

    private void appendNode(StringBuilder sb, Node node) {
        //sb.append("(");
        formatProperties(sb, node);
        //sb.append(")");
    }

    private void formatLabels(StringBuilder sb, Node node) {
        for (Label label : node.getLabels()) {
            sb.append(":").append(label.name());
        }
    }

    private void formatNode(StringBuilder sb, Node n) {
        sb.append("_").append(n.getId());
    }

    private void formatProperties(StringBuilder sb, PropertyContainer pc) {
        final Map<String, Object> properties = toMap(pc);
        if (properties.isEmpty()) return;
        sb.append(" ");
        final String jsonString = gson.toJson(properties);
        sb.append(correctSyntax4JSON(jsonString));
    }
    
    private void formatProperties(StringBuilder sb, PropertyContainer pc, long source, long end) {
        final Map<String, Object> properties = toMap(pc, source, end);
        if (properties.isEmpty()) return;
        sb.append(" ");
        final String jsonString = gson.toJson(properties);
        sb.append(correctSyntax4JSON(jsonString));
    }

    private String correctSyntax4JSON(String json) {
        return json.replaceAll("CI_ID","name");
    }

    Map<String, Object> toMap(PropertyContainer pc) {
        Map<String, Object> result = new TreeMap<String, Object>();
        for (String prop : pc.getPropertyKeys()) {
            result.put(prop, pc.getProperty(prop));
        }
        return result;
    }
    
    Map<String, Object> toMap(PropertyContainer pc, long source, long end) {
        Map<String, Object> result = new TreeMap<String, Object>();
        result.put("source", source);
        result.put("target", end);
        for (String prop : pc.getPropertyKeys()) {
            result.put(prop, pc.getProperty(prop));
        }
        return result;
    }

}