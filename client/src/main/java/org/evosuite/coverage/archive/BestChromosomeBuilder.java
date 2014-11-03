package org.evosuite.coverage.archive;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.TestFitnessFunction;
import org.evosuite.testsuite.TestSuiteChromosome;

public class BestChromosomeBuilder implements Serializable {
	private static final long serialVersionUID = 6665770735812413289L;

	private final TestSuiteChromosome bestChromo;
	//necessary to avoid having a bilion of redundant test cases
	private final Set<Integer> coveredGoals;
	
	public BestChromosomeBuilder() {
		bestChromo = new TestSuiteChromosome();
		coveredGoals = new HashSet<>();
	}

	public void putTest(TestFitnessFunction goal, TestCase test) {
		if (!coveredGoals.contains(goal.hashCode())) {
			coveredGoals.add(goal.hashCode());
			bestChromo.addTest(test);
		}
	}

	public void registerAllTests(Collection<TestChromosome> tests) {
		bestChromo.addTests(tests);
	} 
	
	public  TestSuiteChromosome getBestChromosome() {
		return bestChromo;
	}

}
