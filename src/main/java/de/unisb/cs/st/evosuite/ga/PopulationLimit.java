/**
 * 
 */
package de.unisb.cs.st.evosuite.ga;

import java.util.List;

/**
 * @author Gordon Fraser
 * 
 */
public interface PopulationLimit {
	public boolean isPopulationFull(List<Chromosome> population);
}
