/**
 * 
 */
package org.evosuite.regression;

import org.evosuite.ga.Chromosome;
import org.evosuite.testsuite.TestSuiteFitnessFunction;

/**
 * @author Gordon Fraser
 * 
 */
public class RegressionSuiteFitness extends TestSuiteFitnessFunction {

	/* (non-Javadoc)
	 * @see org.evosuite.ga.FitnessFunction#getFitness(org.evosuite.ga.Chromosome)
	 */
	@Override
	public double getFitness(Chromosome individual) {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.evosuite.ga.FitnessFunction#isMaximizationFunction()
	 */
	@Override
	public boolean isMaximizationFunction() {
		// TODO Auto-generated method stub
		return false;
	}

}
