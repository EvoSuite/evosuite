package org.evosuite.coverage.archive;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.TestFitnessFunction;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class incrementally builds a TestSuiteChromosome with passed test cases.
 * It means to be an archive of tests that covered goals during the evolution.
 * @author mattia
 */
public enum TestsArchive implements Serializable {

	instance;
	
	private static final long serialVersionUID = 6665770735812413289L;

	private static final Logger logger = LoggerFactory.getLogger(TestsArchive.class);
	
	private TestSuiteChromosome bestChromo;
	//necessary to avoid having a billion of redundant test cases
	private final Set<Integer> coveredGoals;
	
	private TestsArchive() {
		bestChromo = new TestSuiteChromosome();
		coveredGoals = new HashSet<>();
	}

	public void putTest(TestFitnessFunction goal, TestCase test) {
		if (!coveredGoals.contains(goal.hashCode())) {
			logger.info("Adding covered goal to archive: "+goal);
			coveredGoals.add(goal.hashCode());
			bestChromo.addTest(test);
		}
	}

	public void registerAllTests(Collection<TestChromosome> tests) {
		bestChromo.addTests(tests);
	} 
	
	/**
	 * return the chromosome with the tests of the archive
	 * @return
	 */
	public  TestSuiteChromosome getBestChromosome() {
		return bestChromo;
	}
	
	@Override
	public String toString() {		
		return "Goals covered: "+coveredGoals.size()+", tests: "+bestChromo.size();
	}
	
	public void reset() {
		bestChromo = new TestSuiteChromosome();
		coveredGoals.clear();
	}

}
