package org.evosuite.performance.indicator;

import org.evosuite.classpath.ClassPathHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

class StatementsCounterTest extends AbstractIndicatorTest {

    private StatementsCounter counter;

    @BeforeEach
    void setUp() {
        ClassPathHandler.getInstance().changeTargetCPtoTheSameAsEvoSuite();
        counter = new StatementsCounter();
    }

    @Test
    void getIndicatorValue() throws ClassNotFoundException, NoSuchMethodException {
        double indicatorValue = counter.getIndicatorValue(buildChromosome());
        assertEquals(indicatorValue, 13, 0);
    }

    @Test
    void getIndicatorId() {
        assertTrue(counter.getIndicatorId().equals("org.evosuite.performance.indicator.StatementsCounter"));
    }
}