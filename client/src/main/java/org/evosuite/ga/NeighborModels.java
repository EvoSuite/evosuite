package org.evosuite.ga;

import java.util.List;

/**
 * An interface that defines the four neighbourhood models used with the cGA
 * 
 * @author Nasser Albunian
 */
public interface NeighborModels<T extends Chromosome<T>> {

	List<T> ringTopology(List<T> collection, int position);
	
	List<T> linearFive(List<T> collection, int position);
	
	List<T> compactNine(List<T> collection, int position);
	
	List<T> CompactThirteen(List<T> collection, int position);

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
		SE
	}
}
