/**
 * 
 */
package de.unisb.cs.st.evosuite.OUM;

import de.unisb.cs.st.evosuite.testsuite.BranchCoverageFitnessFunction;
import de.unisb.cs.st.ga.Chromosome;

/**
 * @author Gordon Fraser
 *
 */
public class OUMBranchFitnessFunction extends BranchCoverageFitnessFunction {


	public double getFitness(Chromosome individual) {
		double branch_fitness = super.getFitness(individual);

		// TODO - patterns should influence fitness?
		// Alternative: make all insertions / changes / deletions apply on pattern level rather than on statement level
		// Potential problem: Minimization step
		
		return 0;
	}

}
