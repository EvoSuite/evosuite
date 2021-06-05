package org.evosuite.performance.indicator;

import org.evosuite.performance.AbstractIndicator;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.statements.ConstructorStatement;
import org.evosuite.testcase.statements.FunctionalMockStatement;
import org.evosuite.testcase.statements.MethodStatement;
import org.evosuite.testcase.statements.Statement;

/**
 * @author Annibale Panichella, Sebastiano Panichella
 * <p>
 * This class implements a static performance indicator: it measures the number of method calls contained
 * in a test case. Therefore, it does not look at the covered production code
 */
public class MethodCallCounter extends AbstractIndicator {

    private static final String INDICATOR = MethodCallCounter.class.getName();

    @Override
    public double getIndicatorValue(TestChromosome test) {
        // if the test has already its indicator values, we don't need to re-compute them
        if (test.getIndicatorValues().containsKey(INDICATOR))
            return test.getIndicatorValue(INDICATOR);

        double nMethodCalls = 0;
        TestCase tc = test.getTestCase();

        for (Statement stmt : tc) {
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
        return stmt instanceof ConstructorStatement || stmt instanceof MethodStatement || stmt instanceof FunctionalMockStatement;
    }
}
