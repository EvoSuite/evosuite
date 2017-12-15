package org.evosuite.ga;

import java.util.List;

/**
 * An interface that defines the four neighbourhood models used with the cGA
 * 
 * @author Nasser Albunian
 */
public interface NeighborModels {


	public List<?> ringTopology(List<? extends Chromosome> collection, int position);
	
	public List<?> linearFive(List<? extends Chromosome> collection, int position);
	
	public List<?> compactNine(List<? extends Chromosome> collection, int position);
	
	public List<?> CompactThirteen(List<? extends Chromosome> collection, int position);

	/*
	 * Neighbourhood positions
	 */
	public enum Positions {

		N,
		S,
		E,
		W,
		NW,
		SW,
		NE,
		SE;
		
	}
}
