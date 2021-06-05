package org.evosuite.performance.indicator;

import com.examples.with.different.packagename.performance.Looping;
import org.evosuite.TestGenerationContext;
import org.evosuite.symbolic.TestCaseBuilder;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.execution.ExecutionResult;
import org.evosuite.testcase.execution.TestCaseExecutor;
import org.evosuite.testcase.variable.VariableReference;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class LoopCounterTest extends AbstractIndicatorTest {

    @BeforeEach
    void setUp() {
        setTargetClass(Looping.class.getName());
    }

    @ParameterizedTest
    @Order(1)
    @CsvSource({"forLoop,10.0", "whileDo,10.0"})
    void getIndicatorValue(String loopKind, double oracle) throws ClassNotFoundException, NoSuchMethodException {
        TestChromosome chromosome = buildChromosomeWithLoop(loopKind);
        ExecutionResult result = TestCaseExecutor.runTest(
                chromosome.getTestCase()
        );
        chromosome.setLastExecutionResult(result);

        assert result.getTrace().getNoExecutionForConditionalNode().size() > 0;

        LoopCounter counter = new LoopCounter();
        double indicatorValue = counter.getIndicatorValue(chromosome);
        assertEquals(oracle, indicatorValue, 0);
    }

    @Test
    @Order(2)
    void getIndicatorId() {
        LoopCounter counter = new LoopCounter();
        assertEquals("org.evosuite.performance.indicator.LoopCounter", counter.getIndicatorId());
    }

    private TestChromosome buildChromosomeWithLoop(String kindOfLoop)
            throws NoSuchMethodException, SecurityException, ClassNotFoundException {
        TestCaseBuilder builder = new TestCaseBuilder();
        VariableReference int0 = builder.appendIntPrimitive(10);

        Class<?> loopClass = TestGenerationContext.getInstance().getClassLoaderForSUT()
                .loadClass(Looping.class.getName());

        Method barMethod = loopClass.getMethod(kindOfLoop, int.class);
        builder.appendMethod(null, barMethod, int0);

        TestChromosome offspring = new TestChromosome();
        offspring.setTestCase(builder.getDefaultTestCase());
        return offspring;
    }
}