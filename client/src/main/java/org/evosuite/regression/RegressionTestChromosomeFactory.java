/**
 * 
 */
package org.evosuite.regression;

import org.evosuite.ga.ChromosomeFactory;
import org.evosuite.testcase.RandomLengthTestFactory;

/**
 * @author Gordon Fraser
 * 
 */
public class RegressionTestChromosomeFactory implements
        ChromosomeFactory<RegressionTestChromosome> {

	private static final long serialVersionUID = -6620991065129236086L;

	private final RandomLengthTestFactory testFactory = new RandomLengthTestFactory();

	/* (non-Javadoc)
	 * @see org.evosuite.ga.ChromosomeFactory#getChromosome()
	 */
	@Override
	public RegressionTestChromosome getChromosome() {
		RegressionTestChromosome individual = new RegressionTestChromosome();
		individual.setTest(testFactory.getChromosome());
		return individual;
	}

}
