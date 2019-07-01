package org.evosuite.performance.indicator;

import org.evosuite.ga.Chromosome;
import org.evosuite.performance.AbstractIndicator;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.execution.ExecutionResult;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * @author annibale.panichella
 * @author sebastiano.panichella
 *
 * This class implements a dynamic performance indicator: it measures the number of method calls
 * executed by a test case. This indicator also considers the indirected method calls, i.e., methods
 * that are called by the covered production code.
 */
public class CoveredMethodCallCounter extends AbstractIndicator {

    private static final Logger logger = LoggerFactory.getLogger(CoveredMethodCallCounter.class);
    private static final String INDICATOR_NAME = CoveredMethodCallCounter.class.getName();

    @Override
    public double getIndicatorValue(Chromosome test) {
        if (test instanceof TestSuiteChromosome)
            throw new IllegalArgumentException("This indicator work at test case level");

        // get the latest execution results
        TestChromosome tch = (TestChromosome) test;
        //we take the information of the last execution results, stored in each chromosome
        ExecutionResult results = tch.getLastExecutionResult();

        // if the test has already its indicator values, we don't need to re-compute them
        if (test.getIndicatorValues().keySet().contains(INDICATOR_NAME))
            return test.getIndicatorValue(INDICATOR_NAME);

        Map<String, Integer> executedMethods = results.getTrace().getMethodExecutionCount();

        // determine the covered lines
        double nMethodCalls = 0.0;
        for (Integer frequency : executedMethods.values()){
            if (frequency >= 2)
                nMethodCalls += frequency;
        }

        test.setIndicatorValues(this.getIndicatorId(), nMethodCalls);
        return nMethodCalls;
    }

    @Override
    public String getIndicatorId() {
        return INDICATOR_NAME;
    }
}
