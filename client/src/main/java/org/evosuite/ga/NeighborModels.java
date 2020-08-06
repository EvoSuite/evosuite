package org.evosuite.ga;

import java.util.List;

/**
 * An interface that defines the four neighbourhood models used with the cGA
 * 
 * @author Nasser Albunian
 */
public interface NeighborModels {


	List<?> ringTopology(List<? extends Chromosome> collection, int position);
	
	List<?> linearFive(List<? extends Chromosome> collection, int position);
	
	List<?> compactNine(List<? extends Chromosome> collection, int position);
	
	List<?> CompactThirteen(List<? extends Chromosome> collection, int position);

	/*
	 * Neighbourhood positions
	 */
    enum Positions {

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
