package org.evosuite.performance.indicator;

import com.examples.with.different.packagename.symbolic.Foo;
import org.evosuite.classpath.ClassPathHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MethodCallCounterTest extends AbstractIndicatorTest {

    private MethodCallCounter counter;

    @BeforeEach
    void setUp() {
        ClassPathHandler.getInstance().changeTargetCPtoTheSameAsEvoSuite();
        counter = new MethodCallCounter();
    }

    @Test
    void getIndicatorValue() throws ClassNotFoundException, NoSuchMethodException {
        double indicatorValue = counter.getIndicatorValue(buildChromosome(
                Foo.class.getName(), "bar"
        ));
        assertEquals(indicatorValue, 10, 0);
    }

    @Test
    void getIndicatorId() {
        assertEquals("org.evosuite.performance.indicator.MethodCallCounter", counter.getIndicatorId());
    }
}