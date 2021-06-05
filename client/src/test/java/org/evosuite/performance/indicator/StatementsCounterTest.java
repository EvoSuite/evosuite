package org.evosuite.performance.indicator;

import com.examples.with.different.packagename.symbolic.Foo;
import org.evosuite.classpath.ClassPathHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
        double indicatorValue = counter.getIndicatorValue(buildChromosome(
                Foo.class.getName(), "bar"
        ));
        assertEquals(indicatorValue, 13, 0);
    }

    @Test
    void getIndicatorId() {
        assertEquals("org.evosuite.performance.indicator.StatementsCounter", counter.getIndicatorId());
    }
}