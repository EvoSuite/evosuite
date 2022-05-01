package org.evosuite.testsmells.smells;

import org.evosuite.ga.FitnessFunction;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testsmells.AbstractTestCaseSmell;
import org.evosuite.testsuite.TestSuiteChromosome;

/**
 * Definition:
 * Tests that take a long time to run.
 *
 * Metric:
 * Duration of the last execution of the test case.
 *
 * 1 - Verify if the last execution result is different from null
 * 2 (1 is True):
 *    2.1 - Get the duration of the last execution of the test case
 *    2.2 - Return the duration
 * 3 (1 is False):
 *    3.1 - Return 0
 */
public class SlowTests extends AbstractTestCaseSmell {

    private static final long serialVersionUID = -628338602818547221L;

    public SlowTests() {
        super("TestSmellSlowTests");
    }

    @Override
    public double computeTestSmellMetric(TestChromosome chromosome) {
        if(chromosome.getLastExecutionResult() != null){
            return FitnessFunction.normalize(chromosome.getLastExecutionResult().getExecutionTime());
        }

        return Double.NaN;
    }

    @Override
    public double computeTestSmellMetric(TestSuiteChromosome chromosome) {
        double smellCount = -1;

        for(TestChromosome testcase : chromosome.getTestChromosomes()){
            if(testcase.getLastExecutionResult() != null){
                if(smellCount >= 0){
                    smellCount += testcase.getLastExecutionResult().getExecutionTime();
                } else {
                    smellCount = testcase.getLastExecutionResult().getExecutionTime();
                }
            }
        }

        if(smellCount == -1){
            return Double.NaN;
        }

        return FitnessFunction.normalize(smellCount);
    }
}
