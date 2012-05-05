/**
 * 
 */
package de.unisb.cs.st.evosuite.testsuite;

import de.unisb.cs.st.evosuite.ga.Chromosome;
import de.unisb.cs.st.evosuite.ga.SecondaryObjective;
import de.unisb.cs.st.evosuite.testcase.ExecutableChromosome;

/**
 * @author Gordon Fraser
 * 
 */
public class MinimizeExceptionsSecondaryObjective extends SecondaryObjective {

	private static final long serialVersionUID = -4405276303273532040L;

	private int getNumExceptions(Chromosome chromosome) {
		int sum = 0;
		for (ExecutableChromosome test : ((TestSuiteChromosome) chromosome).tests) {
			if (test.getLastExecutionResult() != null)
				sum += test.getLastExecutionResult().getNumberOfThrownExceptions();
		}
		return sum;
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
