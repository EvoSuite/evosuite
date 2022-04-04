package org.evosuite.testsmells.smells;

import org.evosuite.ga.FitnessFunction;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.TestFitnessFunction;
import org.evosuite.testsmells.AbstractTestSmell;
import org.evosuite.testsuite.TestSuiteChromosome;

import java.util.List;
import java.util.Set;

/**
 * Definition:
 * A test case that can be removed without affecting the fault detection effectiveness of the test suite.
 *
 * Metric:
 * The number of test cases that can be removed from the test suite without decreasing the code coverage.
 *
 * Detection:
 * 1 - Retrieve all coverage goals covered by the test suite chromosome
 * 2 - Iterate over the test cases of the test suite
 * [2: Start loop]
 * 3 - Clone the initial test suite
 * 4 - Get the test chromosomes of the clone
 * 5 - Delete the current test case of the clone test suite
 * 6 - Verify if the clone test suite covers the same number of goals as the initial test suite:
 * 7 (6 is True):
 *    7.1 - This indicates that it is possible to remove the current test case from the test suite without decreasing
 *          the code coverage: increment the smell counter
 * [2: End loop]
 * 8 - Return the normalized value for the smell counter
 */
public class TestRedundancy extends AbstractTestSmell {

    public TestRedundancy() {
        super("TestSmellTestRedundancy");
    }

    @Override
    public double computeTestSmellMetric(TestSuiteChromosome chromosome) {
        int count = 0;

        int size = chromosome.size();

        TestSuiteChromosome currentTestSuite;
        List<TestChromosome> currentTestSuiteList;

        Set<TestFitnessFunction> goals = chromosome.getCoveredGoals();

        for (int i = 0; i < size; i++){

            currentTestSuite = chromosome.clone();
            currentTestSuiteList = currentTestSuite.getTestChromosomes();
            currentTestSuite.deleteTest(currentTestSuiteList.get(i));

            if(currentTestSuite.getCoveredGoals().size() == goals.size()){
                count++;
            }
        }

        return FitnessFunction.normalize(count);
    }
}
