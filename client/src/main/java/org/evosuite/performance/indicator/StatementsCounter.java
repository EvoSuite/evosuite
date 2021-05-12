package org.evosuite.performance.indicator;

import org.evosuite.performance.AbstractIndicator;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.statements.Statement;

/**
 * @author Giovanni Grano
 * <p>
 * This class implements a static performance indicator;
 * It measures the number of statements in a test case.
 * Thus, it does not look at production code.
 */
public class StatementsCounter extends AbstractIndicator {
    private static final String INDICATOR = StatementsCounter.class.getName();

    @Override
    public double getIndicatorValue(TestChromosome test) {
        // if the test has already its indicator values, we don't need to re-compute them
        if (test.getIndicatorValues().containsKey(INDICATOR))
            return test.getIndicatorValue(INDICATOR);

        // assumption: if not a method call, is a statement
        double statements = 0;
        for (Statement statement : test.getTestCase()) {
            if (!MethodCallCounter.isMethodCall(statement))
                statements++;
        }
        if (statements < 10)
            statements = 0.0;

        test.setIndicatorValues(getIndicatorId(), statements);
        return statements;
    }

    @Override
    public String getIndicatorId() {
        return INDICATOR;
    }
}
