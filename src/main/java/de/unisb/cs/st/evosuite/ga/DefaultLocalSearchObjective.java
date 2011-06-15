/**
 * 
 */
package de.unisb.cs.st.evosuite.ga;

/**
 * @author Gordon Fraser
 * 
 */
public class DefaultLocalSearchObjective implements LocalSearchObjective {

	private final FitnessFunction fitness;

	public DefaultLocalSearchObjective(FitnessFunction fitness) {
		this.fitness = fitness;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.unisb.cs.st.evosuite.ga.LocalSearchObjective#getFitnessFunction()
	 */
	@Override
	public FitnessFunction getFitnessFunction() {
		return fitness;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.unisb.cs.st.evosuite.ga.LocalSearchObjective#hasImproved(de.unisb.
	 * cs.st.evosuite.ga.Chromosome)
	 */
	@Override
	public boolean hasImproved(Chromosome individual) {
		// TODO Auto-generated method stub
		return false;
	}
}
