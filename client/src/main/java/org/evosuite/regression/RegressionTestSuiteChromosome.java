/**
 * 
 */
package org.evosuite.regression;

import java.util.ArrayList;
import java.util.List;

import org.evosuite.ga.Chromosome;
import org.evosuite.ga.ChromosomeFactory;
import org.evosuite.ga.localsearch.LocalSearchObjective;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testsuite.AbstractTestSuiteChromosome;
import org.evosuite.testsuite.TestSuiteChromosome;

/**
 * @author Gordon Fraser
 * 
 */
public class RegressionTestSuiteChromosome extends
        AbstractTestSuiteChromosome<RegressionTestChromosome> {

	private static final long serialVersionUID = 2279207996777829420L;
	
	public String fitnessData = "";

	public double objDistance = 0.0;

	public int diffExceptions = 0;

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
	public boolean localSearch(LocalSearchObjective objective) {
		// Ignore for now
		return false;
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testsuite.AbstractTestSuiteChromosome#clone()
	 */
	@Override
	public Chromosome clone() {
		RegressionTestSuiteChromosome c = new RegressionTestSuiteChromosome(
				this);
		c.fitnessData = fitnessData;
		return c;
	}

	/* (non-Javadoc)
	 * @see org.evosuite.ga.Chromosome#compareSecondaryObjective(org.evosuite.ga.Chromosome)
	 */
	@Override
	public int compareSecondaryObjective(Chromosome o) {
		// TODO Auto-generated method stub
		return 0;
	}
	
	public List<TestCase> getTests() {
		List<TestCase> tests = new ArrayList<TestCase>();
		for (RegressionTestChromosome test : this.tests) {
			tests.add(test.getTheTest().getTestCase());
		}
		return tests;
	}

	public AbstractTestSuiteChromosome<TestChromosome> getTestSuite() {
		AbstractTestSuiteChromosome<TestChromosome> suite = new TestSuiteChromosome();
		for (RegressionTestChromosome regressionTest : tests) {
			suite.addTest(regressionTest.getTheTest());
		}
		return suite;
	}

	public AbstractTestSuiteChromosome<TestChromosome> getTestSuiteForTheOtherClassLoader() {
		AbstractTestSuiteChromosome<TestChromosome> suite = new TestSuiteChromosome();
		for (RegressionTestChromosome regressionTest : tests) {
			suite.addTest(regressionTest.getTheSameTestForTheOtherClassLoader());
		}
		return suite;
	}

	@Override
	public String toString() {
		String testSuiteString = "";
		for (RegressionTestChromosome test : tests) {
			testSuiteString += test.getTheTest().getTestCase().toCode();
			testSuiteString += "\n";
		}
		return testSuiteString;
	}
	
	public List<RegressionTestChromosome> getTestChromosomes() {
		return tests;
	}

}
