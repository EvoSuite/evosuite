package org.evosuite.testsmells.smells;

import org.apache.commons.lang3.StringUtils;
import org.evosuite.ga.FitnessFunction;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testsmells.AbstractTestCaseSmell;
import org.evosuite.testsuite.TestSuiteChromosome;

import java.util.ArrayList;
import java.util.List;

/**
 * Definition:
 * Tests that take a long time to run.
 *
 * Metric:
 * Duration of the last execution of the test case.
 *
 * Computation - Test Case:
 * 1 - Verify if the last execution result is different from null
 * 2 (1 is True):
 *    2.1 - Return the normalized duration of the last execution of the test case
 * 3 (1 is False):
 *    3.1 - Return NaN
 *
 * Computation - Test Suite:
 * 1 - Create a smell counter and initialize the variable with the value -1
 * 2 - Let T = {T1,...,Tn} be the set of n test cases in a test suite
 * 3 - Iterate over T and, for each test case Ti:
 * [3: Start loop]
 * 4 - Verify if the last execution result of Ti is different from null
 * 5 (4 is True):
 *    5.1 - If the smell counter is greater than or equal to zero, increment the smell counter by the duration of the last
 *          execution of Ti; otherwise, change the value of the smell counter to the duration of the last execution of Ti
 * [3: End loop]
 * 6 - If the smell counter is equal to -1: return NaN
 * 7 - Return the normalized value for the smell counter
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

    @Override
    public String computeTestSmellMetricForEachTestCase(TestSuiteChromosome chromosome) {
        List<Double> smellCountList = new ArrayList<>();

        for(TestChromosome testcase : chromosome.getTestChromosomes()){
            smellCountList.add(computeTestSmellMetric(testcase));
        }

        return StringUtils.join(smellCountList, "|");
    }
}
