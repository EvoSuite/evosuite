package org.evosuite.coverage.length;

import org.evosuite.testcase.ExecutableChromosome;
import org.evosuite.testcase.execution.ExecutionResult;
import org.evosuite.testsuite.AbstractTestSuiteChromosome;
import org.evosuite.testsuite.TestSuiteFitnessFunction;

import java.util.List;

public class LengthSuiteFitness extends TestSuiteFitnessFunction {

    @Override
    public double getFitness(AbstractTestSuiteChromosome<? extends ExecutableChromosome> suite) {
        double fitness = 0.0;
        int length = (int) Double.MAX_VALUE;
        // Run all tests and gather the execution results.
        List<ExecutionResult> results = runTestSuite(suite);

        // Penalize fitness if the test suite times out.
        for (ExecutionResult result : results) {
            if (result.hasTimeout() || result.hasTestException()) {
                fitness = Double.MAX_VALUE;
                break;
            }
        }

        if(fitness !=  Double.MAX_VALUE ){
            length = suite.totalLengthOfTestCases();
            if(suite.totalLengthOfTestCases() < 1){
                fitness = Double.MAX_VALUE;
                length = (int) fitness;
            }else{
                fitness = 0.0;
            }
        }

        updateIndividual(this, suite, fitness);
        if(fitness ==  Double.MAX_VALUE){
            suite.setNumOfCoveredGoals(this, 0);
            suite.setCoverage(this, 0.0);
        }else{
            suite.setNumOfCoveredGoals(this, 1);
            suite.setCoverage(this, 1.0);
        }

        return fitness ;
    }

    /**
     *
     */
    @Override
    public boolean isMaximizationFunction() {
        return false;
    }

}
