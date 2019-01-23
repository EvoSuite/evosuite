package org.evosuite.performance.indicator;

import org.evosuite.ga.Chromosome;
import org.evosuite.performance.AbstractIndicator;

public class TestLength extends AbstractIndicator {

    private static String INDICATOR = TestLength.class.getName();


    @Override
    public double getIndicatorValue(Chromosome test) {
        test.setIndicatorValues(this.getIndicatorId(), (double) test.size());
        return test.size();
    }

    @Override
    public String getIndicatorId() {
        return INDICATOR;
    }
}
