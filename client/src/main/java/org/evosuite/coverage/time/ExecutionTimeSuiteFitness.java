package org.evosuite.coverage.time;

import org.evosuite.testcase.ExecutableChromosome;
import org.evosuite.testcase.execution.ExecutionResult;
import org.evosuite.testsuite.AbstractTestSuiteChromosome;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.evosuite.testsuite.TestSuiteFitnessFunction;

import java.util.List;

public class ExecutionTimeSuiteFitness extends TestSuiteFitnessFunction {
    @Override
    public double getFitness(AbstractTestSuiteChromosome<? extends ExecutableChromosome> suite) {
        double sum = 0;
        double coverage = 0;
        if(suite.size() == 0 || suite.totalLengthOfTestCases() == 0  ||  suite.getLastExecutionResults().get(0) == null ){
            List<ExecutionResult> results = runTestSuite(suite);
            for (ExecutionResult er :  results) {
                sum+=er.getExecutionTime();
            }
            coverage = 1.0;
        }else{
            sum = Double.MAX_VALUE;
            coverage = 0.0;
        }

        double average = sum / (suite.getTestChromosomes().size() * (1.0));

        updateIndividual(this, suite,average);
//        suite.setCoverage(this, coverage);
//        suite.setNumOfCoveredGoals(this, (int) coverage);

        return average;
    }

    @Override
    public boolean isMaximizationFunction() {
        return false;
    }
}
