package org.evosuite.performance.indicator;

import com.examples.with.different.packagename.performance.CoveredCallsDummy;
import org.evosuite.Properties;
import org.evosuite.TestGenerationContext;
import org.evosuite.classpath.ClassPathHandler;
import org.evosuite.symbolic.TestCaseBuilder;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.variable.VariableReference;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

abstract class AbstractIndicatorTest {

    protected void setTargetClass(String targetClass) {
        ClassPathHandler.getInstance().changeTargetCPtoTheSameAsEvoSuite();
        Properties.TARGET_CLASS = targetClass;
    }

    /**
     * Creates a chromosome exercising the {@link CoveredCallsDummy} class
     */
    protected TestChromosome buildChromosomeForCoverage()
            throws NoSuchMethodException, SecurityException, ClassNotFoundException {
        TestCaseBuilder builder = new TestCaseBuilder();

        Class<?> callClass = TestGenerationContext.getInstance().getClassLoaderForSUT()
                .loadClass(CoveredCallsDummy.class.getName());

        Constructor<?> constructor = callClass.getConstructor();
        VariableReference callee = builder.appendConstructor(constructor);
        Method barMethod = callClass.getMethod("callOne");
        builder.appendMethod(callee, barMethod);


        TestChromosome offspring = new TestChromosome();
        offspring.setTestCase(builder.getDefaultTestCase());
        return offspring;
    }

    /**
     * Creates a {@link TestChromosome} with 10 method calls and 13 statements
     */
    protected TestChromosome buildChromosome(String className, String methodName)
            throws NoSuchMethodException, SecurityException, ClassNotFoundException {
        TestCaseBuilder builder = new TestCaseBuilder();
        VariableReference int0 = builder.appendIntPrimitive(10);
        VariableReference int1 = builder.appendIntPrimitive(10);
        VariableReference int2 = builder.appendIntPrimitive(10);

        for (int i = 0; i < 10; i++)
            builder.appendStringPrimitive("10");

        Class<?> fooClass = TestGenerationContext.getInstance().getClassLoaderForSUT()
                .loadClass(className);

        for (int i = 0; i < 10; i++) {
            Method barMethod = fooClass.getMethod(methodName, int.class, int.class, int.class);
            builder.appendMethod(null, barMethod, int0, int1, int2);
        }

        TestChromosome offspring = new TestChromosome();
        offspring.setTestCase(builder.getDefaultTestCase());
        return offspring;
    }
}