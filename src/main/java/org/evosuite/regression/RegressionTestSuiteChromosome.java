/**
 * 
 */
package org.evosuite.regression;

import org.evosuite.ga.Chromosome;
import org.evosuite.ga.ChromosomeFactory;
import org.evosuite.ga.GeneticAlgorithm;
import org.evosuite.ga.LocalSearchObjective;
import org.evosuite.testsuite.AbstractTestSuiteChromosome;
import org.evosuite.testsuite.TestSuiteChromosome;

/**
 * @author Gordon Fraser
 * 
 */
public class RegressionTestSuiteChromosome extends
        AbstractTestSuiteChromosome<RegressionTestChromosome> {

	private static final long serialVersionUID = 2279207996777829420L;

	public RegressionTestSuiteChromosome() {
		super();
	}

	/**
	 * <p>
	 * Constructor for RegressionTestSuiteChromosome.
	 * </p>
	 * 
	 * @param testChromosomeFactory
	 *            a {@link org.evosuite.ga.ChromosomeFactory} object.
	 */
	public RegressionTestSuiteChromosome(
	        ChromosomeFactory<RegressionTestChromosome> testChromosomeFactory) {
		super(testChromosomeFactory);
	}

	/**
	 * <p>
	 * Constructor for TestSuiteChromosome.
	 * </p>
	 * 
	 * @param source
	 *            a {@link org.evosuite.testsuite.TestSuiteChromosome} object.
	 */
	protected RegressionTestSuiteChromosome(RegressionTestSuiteChromosome source) {
		super(source);
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testsuite.AbstractTestSuiteChromosome#localSearch(org.evosuite.ga.LocalSearchObjective)
	 */
	@Override
	public void localSearch(LocalSearchObjective objective) {
		// Ignore for now
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testsuite.AbstractTestSuiteChromosome#clone()
	 */
	@Override
	public Chromosome clone() {
		return new RegressionTestSuiteChromosome(this);
	}

	/* (non-Javadoc)
	 * @see org.evosuite.ga.Chromosome#compareSecondaryObjective(org.evosuite.ga.Chromosome)
	 */
	@Override
	public int compareSecondaryObjective(Chromosome o) {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.evosuite.ga.Chromosome#applyDSE(org.evosuite.ga.GeneticAlgorithm)
	 */
	@Override
	public void applyDSE(GeneticAlgorithm algorithm) {
		// ignore for now
	}

}
