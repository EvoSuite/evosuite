/**
 * 
 */
package de.unisb.cs.st.evosuite.ga;

import java.util.List;

import de.unisb.cs.st.evosuite.Properties;

/**
 * @author Gordon Fraser
 * 
 */
public class IndividualPopulationLimit implements PopulationLimit {

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.ga.PopulationLimit#isPopulationFull(java.util.List)
	 */
	@Override
	public boolean isPopulationFull(List<Chromosome> population) {
		return population.size() >= Properties.POPULATION;
	}

}
