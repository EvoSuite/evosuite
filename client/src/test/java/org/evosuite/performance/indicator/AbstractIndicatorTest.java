package org.evosuite.performance.indicator;

import com.examples.with.different.packagename.symbolic.Foo;
import org.evosuite.TestGenerationContext;
import org.evosuite.symbolic.TestCaseBuilder;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.variable.VariableReference;

import java.lang.reflect.Method;

abstract class AbstractIndicatorTest {

    /**
     * Creates a {@link TestChromosome} with 10 method calls and 13 statements
     *
     * @return
     * @throws NoSuchMethodException
     * @throws SecurityException
     * @throws ClassNotFoundException
     */
    protected TestChromosome buildChromosome()
            throws NoSuchMethodException, SecurityException, ClassNotFoundException {
        TestCaseBuilder builder = new TestCaseBuilder();
        VariableReference int0 = builder.appendIntPrimitive(10);
        VariableReference int1 = builder.appendIntPrimitive(10);
        VariableReference int2 = builder.appendIntPrimitive(10);

        for (int i = 0; i < 10; i++)
            builder.appendStringPrimitive("10");

        Class<?> fooClass = TestGenerationContext.getInstance().getClassLoaderForSUT()
                .loadClass(Foo.class.getName());

        for (int i = 0; i < 10; i++) {
            Method barMethod = fooClass.getMethod("bar", int.class, int.class, int.class);
            builder.appendMethod(null, barMethod, int0, int1, int2);
        }

        TestChromosome offspring = new TestChromosome();
        offspring.setTestCase(builder.getDefaultTestCase());
        return offspring;
    }
}