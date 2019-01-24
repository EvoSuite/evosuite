package org.evosuite.performance.indicator;

import org.evosuite.ga.Chromosome;
import org.evosuite.performance.AbstractIndicator;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.statements.*;
import org.evosuite.testcase.statements.reflection.PrivateMethodStatement;
import org.evosuite.testsuite.TestSuiteChromosome;

/**
 * @author annibale.panichella
 * @author sebastiano
 *
 * This class implements a static performance indicator: it measures the number of method calls contained
 * in a test case. Therefore, it does not look at the covered production code
 */
public class MethodCallCounter extends AbstractIndicator {

    private static String INDICATOR = MethodCallCounter.class.getName();

    @Override
    public double getIndicatorValue(Chromosome test) {
        if (test instanceof TestSuiteChromosome)
            throw new IllegalArgumentException("This indicator work at test case level");

        // if the test has already its indicator values, we don't need to re-compute them
        if (test.getIndicatorValues().keySet().contains(INDICATOR))
            return test.getIndicatorValue(INDICATOR);

        double nMethodCalls = 0;
        TestChromosome tch = (TestChromosome) test;
        TestCase tc = tch.getTestCase();

        for (Statement stmt : tc){
            if (isMethodCall(stmt))
                nMethodCalls++;
        }

        test.setIndicatorValues(this.getIndicatorId(), nMethodCalls);
        return nMethodCalls;
    }

    public String getIndicatorId() {
        return INDICATOR;
    }

    public static boolean isMethodCall(Statement stmt) {
        return (stmt instanceof ConstructorStatement
                || stmt instanceof MethodStatement
                || stmt instanceof PrivateMethodStatement
                || stmt instanceof FunctionalMockStatement
                || stmt instanceof FunctionalMockForAbstractClassStatement);
    }
}
