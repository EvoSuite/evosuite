package org.evosuite.performance.indicator;

import org.evosuite.performance.AbstractIndicator;
import org.evosuite.testcase.TestChromosome;

public class TestLength extends AbstractIndicator {

    private static final String INDICATOR = TestLength.class.getName();


    @Override
    public double getIndicatorValue(TestChromosome test) {
        test.setIndicatorValues(this.getIndicatorId(), (double) test.size());
        return test.size();
    }

    @Override
    public String getIndicatorId() {
        return INDICATOR;
    }
}
