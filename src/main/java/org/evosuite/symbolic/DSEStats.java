package org.evosuite.symbolic;

/**
 * This class is used to store statistics on DSE.
 * 
 * @author galeotti
 *
 */
public abstract class DSEStats {

	private static long nrOfUNSATs = 0;
	private static long nrOfSATs = 0;
	private static long nrOfSolutionWithNoImprovement =0;
	
	public static void clear() {
		nrOfUNSATs = 0;
		nrOfSATs = 0;
		nrOfSolutionWithNoImprovement = 0;
	}
	
	public static void reportUNSAT() {
		nrOfUNSATs++;
	}
	
	public static void reportSAT() {
		nrOfSATs++;
	}
	
	public static void reportSolutionWithNoFitnessImprovement() {
		nrOfSolutionWithNoImprovement++;
	}
	
	
	
}
