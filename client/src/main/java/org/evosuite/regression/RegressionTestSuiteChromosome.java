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
import org.evosuite.testsuite.factories.TestSuiteChromosomeFactory;

/**
 * @author Gordon Fraser
 * 
 */
public class RegressionTestSuiteChromosome extends
       TestSuiteChromosome {

	private static final long serialVersionUID = 2279207996777829420L;
	
	public String fitnessData = "";

	public double objDistance = 0.0;

	public int diffExceptions = 0;
	
	//protected List<RegressionTestChromosome> regressionTests = new ArrayList<RegressionTestChromosome>();

	public RegressionTestSuiteChromosome() {
		super();
	}

	public RegressionTestSuiteChromosome(ChromosomeFactory<TestChromosome> testChromosomeFactory) {
		this.testChromosomeFactory = testChromosomeFactory;
	}

	protected RegressionTestSuiteChromosome(RegressionTestSuiteChromosome source) {
		super(source);
		/*for (TestChromosome test : source.tests) {
			RegressionTestChromosome rtc = (RegressionTestChromosome) test;
			//addTest((TestChromosome) rtc.clone());
			tests.add((RegressionTestChromosome) rtc.clone());
		}*/
	}
	
	@Override
	public void addTest(TestChromosome test) {
		if(test instanceof RegressionTestChromosome){
			tests.add(test);
		} else {
			RegressionTestChromosome rtc = new RegressionTestChromosome();
			rtc.setTest(test);
			tests.add(rtc);
		}
		this.setChanged(true);
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
	public TestSuiteChromosome clone() {
		RegressionTestSuiteChromosome c = new RegressionTestSuiteChromosome(
				this);
		c.fitnessData = fitnessData;
		//assert (c.testChromosomeFactory != null): "Chromosome Factory was null";
		return c;
	}
	
	public List<TestCase> getTests() {
		List<TestCase> tests = new ArrayList<TestCase>();
		for (TestChromosome test : this.tests) {
			tests.add(test.getTestCase());
		}
		return tests;
	}

	public AbstractTestSuiteChromosome<TestChromosome> getTestSuite() {
		AbstractTestSuiteChromosome<TestChromosome> suite = new TestSuiteChromosome();
		for (TestChromosome regressionTest : tests) {
			RegressionTestChromosome rtc = (RegressionTestChromosome)regressionTest;
			suite.addTest(rtc.getTheTest());
		}
		return suite;
	}

	public AbstractTestSuiteChromosome<TestChromosome> getTestSuiteForTheOtherClassLoader() {
		AbstractTestSuiteChromosome<TestChromosome> suite = new TestSuiteChromosome();
		for (TestChromosome regressionTest : tests) {
			RegressionTestChromosome rtc = (RegressionTestChromosome)regressionTest;
			suite.addTest(rtc.getTheSameTestForTheOtherClassLoader());
		}
		return suite;
	}

	@Override
	public String toString() {
		String testSuiteString = "";
		for (TestChromosome test : tests) {
			testSuiteString += ((RegressionTestChromosome)test).getTheTest().getTestCase().toCode();
			testSuiteString += "\n";
		}
		return testSuiteString;
	}
	
	public List<TestChromosome> getTestChromosomes() {
		return tests;
	}
	

}
