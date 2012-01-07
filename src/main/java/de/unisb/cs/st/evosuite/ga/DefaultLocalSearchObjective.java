/**
 * 
 */
package de.unisb.cs.st.evosuite.ga;

import java.io.Serializable;

/**
 * @author Gordon Fraser
 * 
 */
public class DefaultLocalSearchObjective implements LocalSearchObjective, Serializable {

	private static final long serialVersionUID = -8640106627078837108L;

	private final FitnessFunction fitness;

	public DefaultLocalSearchObjective(FitnessFunction fitness) {
		this.fitness = fitness;
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.ga.LocalSearchObjective#hasImproved(de.unisb.cs.st.evosuite.ga.Chromosome)
	 */
	@Override
	public boolean hasImproved(Chromosome individual) {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.ga.LocalSearchObjective#getFitnessFunction()
	 */
	@Override
	public FitnessFunction getFitnessFunction() {
		return fitness;
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.ga.LocalSearchObjective#hasChanged(de.unisb.cs.st.evosuite.ga.Chromosome)
	 */
	@Override
	public int hasChanged(Chromosome individual) {
		// TODO Auto-generated method stub
		return 0;
	}

}
