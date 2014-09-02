package gr.aueb.CIPGUI2014.graphs;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

public class Relation {

	boolean isActive;
	double impact;
	double likelihood;
	double risk;
	Node startNode;
	Node endNode;
	Relationship relationshipObject;
	
	public Relation (boolean isA, double imp, double like, double r, Node st, Node end, Relationship e) {
		isActive = isA;
		impact = imp;
		likelihood = like;
		risk = r;
		startNode = st;
		endNode = end;
		relationshipObject = e;
	}

	public boolean isActive() {
		return isActive;
	}

	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}

	public double getImpact() {
		return impact;
	}

	public void setImpact(double impact) {
		this.impact = impact;
	}

	public double getLikelihood() {
		return likelihood;
	}

	public void setLikelihood(double likelihood) {
		this.likelihood = likelihood;
	}

	public double getRisk() {
		return risk;
	}

	public void setRisk(double risk) {
		this.risk = risk;
	}
	
	public void deleteRelationshipFromGraph(Relationship e) {
		e.delete();
		relationshipObject = null;
	}
}
