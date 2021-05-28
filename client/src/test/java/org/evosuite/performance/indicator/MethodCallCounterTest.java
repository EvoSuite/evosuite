package org.evosuite.performance.indicator;

import org.evosuite.classpath.ClassPathHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

class MethodCallCounterTest extends AbstractIndicatorTest {

    private MethodCallCounter counter;

    @BeforeEach
    void setUp() {
        ClassPathHandler.getInstance().changeTargetCPtoTheSameAsEvoSuite();
        counter = new MethodCallCounter();
    }

    @Test
    void getIndicatorValue() throws ClassNotFoundException, NoSuchMethodException {
        double indicatorValue = counter.getIndicatorValue(buildChromosome());
        assertEquals(indicatorValue, 10, 0);
    }

    @Test
    void getIndicatorId() {
        assertTrue(counter.getIndicatorId().equals("org.evosuite.performance.indicator.MethodCallCounter"));
    }
}