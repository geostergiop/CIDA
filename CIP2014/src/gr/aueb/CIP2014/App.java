package gr.aueb.CIP2014;

import gr.aueb.CIP2014.graphs.BasicGraph;
import gr.aueb.CIP2014.misc.MathMethods;

import java.util.Random;

public class App {
	
	int MAX_NO_CIS = 50;
	int MIN_NO_CIS = 50;
	
	public static void main(String[] args) {
		App run = new App();
		run.run();
	}
	
	private void run() {
		
		Random intRandom = new Random();
		int number = intRandom.nextInt(MAX_NO_CIS - MIN_NO_CIS + 1) + MIN_NO_CIS;
		
		Random doubleRandom = new Random();
		
		int[][] CI_impact = new int[number][number];
		double[][] CI_likelihood = new double[number][number];
		
		// Fill Impact and Likelihood tables with random numbers according to their range
		// CI_impact is Impact table with range: [1, 5]
		// CI_likelihood is Likelihood table with range: [0.1, 1]
		for (int i=0; i<number; i++) {
			for (int j=0; j<number; j++) {
				CI_impact [i][j] = intRandom.nextInt(5 - 1 + 1) + 1;
				
				double temp = (doubleRandom.nextDouble() * (1 - 0.1)) + 0.1;
				// Limit digits to two in generated double
				CI_likelihood [i][j] = MathMethods.limitDecimals(temp);
			}
		}

		// Create a Neo4J graph store info inside his database
		BasicGraph g = new BasicGraph();
		g.createGraph(number, CI_impact, CI_likelihood);
	}
}
