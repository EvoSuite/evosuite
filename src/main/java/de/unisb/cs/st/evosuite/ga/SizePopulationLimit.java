/**
 * 
 */
package de.unisb.cs.st.evosuite.ga;

import java.util.List;

import de.unisb.cs.st.evosuite.Properties;

/**
 * @author fraser
 * 
 */
public class SizePopulationLimit implements PopulationLimit {

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.ga.PopulationLimit#isPopulationFull(java.util.List)
	 */
	@Override
	public boolean isPopulationFull(List<Chromosome> population) {
		int size = 0;
		for (Chromosome chromosome : population)
			size += chromosome.size();

		return size >= Properties.POPULATION;
	}

}
