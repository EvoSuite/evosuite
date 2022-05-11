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
 * 1 - Initialize the smell counter with the number of test cases in the test suite
 * 2 - Retrieve all coverage goals covered by the test suite chromosome
 * 3 - Verify if the number of covered goals is equal to zero
 * 4 (3 is True):
 *    4.1 - Return NaN
 * 5 - Iterate over the test cases of a test suite
 * [5: Start loop]
 * 6 - Remove the coverage goals covered by the current test case from the total set of goals
 * 7 - If at least one coverage goal is removed: decrement the smell counter.
 * [5: Start loop]
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
}
