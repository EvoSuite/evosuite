package org.evosuite.performance.indicator;

import org.evosuite.Properties;
import org.evosuite.performance.AbstractIndicator;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class IndicatorsFactoryTest {

    @Test
    public void indicatorFactoryTest() {
        Properties.MOSA_SECONDARY_OBJECTIVE = new Properties.PerformanceIndicators[] {
                Properties.PerformanceIndicators.METHOD_CALL, Properties.PerformanceIndicators.COVERED_METHOD_CALL,
                Properties.PerformanceIndicators.OBJECTS_INSTANTIATIONS
        };
        System.setProperty("performance_indicators", "METHOD_CALL,COVERED_METHOD_CALL,OBJECTS_INSTANTIATIONS");
        List<AbstractIndicator> indicators = IndicatorsFactory.getPerformanceIndicator();
        assertEquals(3, indicators.size());
        assertEquals(MethodCallCounter.class, indicators.get(0).getClass());
        assertEquals(CoveredMethodCallCounter.class, indicators.get(1).getClass());
        assertEquals(ObjectInstantiations.class, indicators.get(2).getClass());
    }

}