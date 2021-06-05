package org.evosuite.performance.indicator;

import com.examples.with.different.packagename.performance.CoveredCallsDummy;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.execution.ExecutionResult;
import org.evosuite.testcase.execution.TestCaseExecutor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CoveredMethodCallCounterTest extends AbstractIndicatorTest {
    CoveredMethodCallCounter counter;

    @BeforeEach
    void setUp() {
        setTargetClass(CoveredCallsDummy.class.getName());
        counter = new CoveredMethodCallCounter();
    }

    @Test
    void getIndicatorValue() throws ClassNotFoundException, NoSuchMethodException {
        TestChromosome chromosome = buildChromosomeForCoverage();
        ExecutionResult result = TestCaseExecutor.runTest(
                chromosome.getTestCase()
        );
        chromosome.setLastExecutionResult(result);

        double indicatorValue = counter.getIndicatorValue(chromosome);
        // 6 = 1 to init, 1 to callOne, 1 to CallTwo, 3 to CallThree
        assertEquals(6, indicatorValue, 0);
    }

    @Test
    void getIndicatorId() {
        assertEquals("org.evosuite.performance.indicator.CoveredMethodCallCounter", counter.getIndicatorId());
    }
}