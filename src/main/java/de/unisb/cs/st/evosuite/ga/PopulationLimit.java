/**
 * 
 */
package de.unisb.cs.st.evosuite.ga;

import java.io.Serializable;
import java.util.List;

/**
 * @author Gordon Fraser
 * 
 */
public interface PopulationLimit extends Serializable {
	public boolean isPopulationFull(List<Chromosome> population);
}
