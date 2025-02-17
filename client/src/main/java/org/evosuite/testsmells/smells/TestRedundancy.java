package org.evosuite.testsmells.smells;

import org.evosuite.ga.FitnessFunction;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.TestFitnessFunction;
import org.evosuite.testsmells.AbstractTestSmell;
import org.evosuite.testsuite.TestSuiteChromosome;

import java.util.Set;

/**
 * Definition:
 * A test case that can be removed without affecting the fault detection effectiveness of the test suite.
 *
 * Metric:
 * The number of test cases that can be removed from the test suite without decreasing the code coverage.
 *
 * Computation:
 * 1 - Create a smell counter and initialize the variable with the number of test cases in the test suite (n)
 * 2 - Retrieve all coverage goals covered by the test suite chromosome (total set of goals)
 * 3 - If the number of covered goals is equal to 0: return NaN
 * 4 - Let T = {T1,...,Tn} be the set of n test cases in a test suite
 * 5 - Iterate over T and, for each test case Ti:
 * [5: Start loop]
 * 6 - Remove the coverage goals covered by Ti from the total set of goals
 * 7 - If at least one coverage goal is removed: decrement the smell counter
 * [5: End loop]
 * 8 - Return the normalized value for the smell counter
 */
public class TestRedundancy extends AbstractTestSmell {

    private static final long serialVersionUID = 8506203484124866711L;

    public TestRedundancy() {
        super("TestSmellTestRedundancy");
    }

    @Override
    public double computeTestSmellMetric(TestSuiteChromosome chromosome) {
        int number_of_redundant_tests = chromosome.size();
        Set<TestFitnessFunction> suiteGoals = chromosome.getCoveredGoals();

        if(suiteGoals.size() == 0){
            return Double.NaN;
        }

        for (TestChromosome testChromosome : chromosome.getTestChromosomes()) {
            Set<TestFitnessFunction> testGoals = testChromosome.getTestCase().getCoveredGoals();
            if (suiteGoals.removeAll(testGoals)) {
                number_of_redundant_tests--;
            }
        }

        return FitnessFunction.normalize(number_of_redundant_tests);
    }

    @Override
    public String computeTestSmellMetricForEachTestCase(TestSuiteChromosome chromosome) {
        return String.valueOf(computeTestSmellMetric(chromosome));
    }
}
