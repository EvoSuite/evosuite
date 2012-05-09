/**
 * 
 */
package de.unisb.cs.st.evosuite.testcase;

import de.unisb.cs.st.evosuite.ga.Chromosome;
import de.unisb.cs.st.evosuite.ga.SecondaryObjective;

/**
 * @author Gordon Fraser
 * 
 */
public class MinimizeExceptionsSecondaryObjective extends SecondaryObjective {

	private static final long serialVersionUID = -4405276303273532040L;

	private int getNumExceptions(Chromosome chromosome) {
		ExecutionResult result = ((ExecutableChromosome) chromosome).getLastExecutionResult();
		if (result != null)
			return result.getNumberOfThrownExceptions();
		else
			return 0;
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.ga.SecondaryObjective#compareChromosomes(de.unisb.cs.st.evosuite.ga.Chromosome, de.unisb.cs.st.evosuite.ga.Chromosome)
	 */
	@Override
	public int compareChromosomes(Chromosome chromosome1, Chromosome chromosome2) {
		return getNumExceptions(chromosome1) - getNumExceptions(chromosome2);
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.ga.SecondaryObjective#compareGenerations(de.unisb.cs.st.evosuite.ga.Chromosome, de.unisb.cs.st.evosuite.ga.Chromosome, de.unisb.cs.st.evosuite.ga.Chromosome, de.unisb.cs.st.evosuite.ga.Chromosome)
	 */
	@Override
	public int compareGenerations(Chromosome parent1, Chromosome parent2,
	        Chromosome child1, Chromosome child2) {
		return Math.min(getNumExceptions(parent1), getNumExceptions(parent2))
		        - Math.min(getNumExceptions(child1), getNumExceptions(child2));
	}

}
