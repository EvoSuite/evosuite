package org.evosuite.performance.indicator;

import com.examples.with.different.packagename.performance.ObjectsInstantiations;
import org.evosuite.TestGenerationContext;
import org.evosuite.symbolic.TestCaseBuilder;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.execution.ExecutionResult;
import org.evosuite.testcase.execution.TestCaseExecutor;
import org.evosuite.testcase.variable.VariableReference;
import org.junit.jupiter.api.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ObjectInstantiationsTest extends AbstractIndicatorTest {

    @BeforeEach
    void setUp() {
        setTargetClass(ObjectsInstantiations.class.getName());
    }

    @Test
    @Order(1)
    void getIndicatorValue() throws ClassNotFoundException, NoSuchMethodException {
        TestChromosome chromosome = buildChromosomeWithObjects();
        ExecutionResult result = TestCaseExecutor.runTest(
                chromosome.getTestCase()
        );
        chromosome.setLastExecutionResult(result);

        ObjectInstantiations counter = new ObjectInstantiations();
        double indicatorValue = counter.getIndicatorValue(chromosome);
        assertEquals(10, indicatorValue, 0);
    }

    @Test
    @Order(2)
    void getIndicatorId() {
        ObjectInstantiations counter = new ObjectInstantiations();
        assertEquals("org.evosuite.performance.indicator.ObjectInstantiations", counter.getIndicatorId());
    }

    private TestChromosome buildChromosomeWithObjects()
            throws NoSuchMethodException, SecurityException, ClassNotFoundException {
        TestCaseBuilder builder = new TestCaseBuilder();

        Class<?> objectsClass = TestGenerationContext.getInstance().getClassLoaderForSUT()
                .loadClass(ObjectsInstantiations.class.getName());

        Constructor<?> constructor = objectsClass.getConstructor();
        VariableReference callee = builder.appendConstructor(constructor);
        Method barMethod = objectsClass.getMethod("createObjects");
        builder.appendMethod(callee, barMethod);

        TestChromosome offspring = new TestChromosome();
        offspring.setTestCase(builder.getDefaultTestCase());
        return offspring;
    }
}