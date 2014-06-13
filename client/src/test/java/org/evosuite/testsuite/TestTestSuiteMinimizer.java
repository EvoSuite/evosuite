package org.evosuite.testsuite;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Method;

import org.evosuite.Properties;
import org.evosuite.TestGenerationContext;
import org.evosuite.ga.ConstructionFailedException;
import org.evosuite.testcase.DefaultTestCase;
import org.evosuite.testcase.IntPrimitiveStatement;
import org.evosuite.testcase.TestFactory;
import org.evosuite.testcase.VariableReference;
import org.evosuite.utils.GenericClass;
import org.evosuite.utils.GenericMethod;
import org.evosuite.utils.Randomness;
import org.junit.Test;

import com.examples.with.different.packagename.FlagExample1;

public class TestTestSuiteMinimizer
{
    @Test
    public void testMinimizer() throws ClassNotFoundException, NoSuchFieldException, SecurityException, ConstructionFailedException, NoSuchMethodException
    {
        Properties.MINIMIZE_OLD = true;
        Randomness.setSeed(42);

        Class<?> sut = TestGenerationContext.getInstance().getClassLoaderForSUT().loadClass(FlagExample1.class.getCanonicalName());
        GenericClass clazz = new GenericClass(sut);

        DefaultTestCase test = new DefaultTestCase();
        IntPrimitiveStatement ips = new IntPrimitiveStatement(test, 0);
        VariableReference vr = test.addStatement(ips, 0);

        Method m = clazz.getRawClass().getMethod("testMe", new Class<?>[] { int.class });
        GenericMethod method = new GenericMethod(m, sut);

        TestFactory testFactory = TestFactory.getInstance();
        for (int i = 0; i < 10; i++) {
            testFactory.addMethodFor(test, vr, method, i+1);
        }

        assertEquals(17, test.size());

        /*TestSuiteMinimizer minimizer = new TestSuiteMinimizer(null);
        TestSuiteChromosome tsc = new TestSuiteChromosome();
        tsc.addTest(test);
        assertEquals(1, tsc.size());

        minimizer.minimize(tsc);*/

        String code = test.toCode();
        System.out.println(code);
        //assertEquals(code, "int int1 = 0;\nboolean boolean0 = int0.testMe(int1);");        
    }
}
