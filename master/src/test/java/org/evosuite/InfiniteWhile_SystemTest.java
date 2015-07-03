package org.evosuite;

import com.examples.with.different.packagename.InfiniteWhile;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.runtime.TooManyResourcesException;
import org.evosuite.runtime.instrumentation.EvoClassLoader;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.junit.Assert.fail;

/**
 * Created by Andrea Arcuri on 29/03/15.
 */
public class InfiniteWhile_SystemTest  extends SystemTest{

    @Test(timeout = 5000)
    public void testLoading() throws Exception{
        EvoClassLoader loader = new EvoClassLoader();
        Class<?> clazz = loader.loadClass(InfiniteWhile.class.getCanonicalName());

        Method m = clazz.getMethod("infiniteLoop");
        try {
            m.invoke(null);
            fail();
        }catch(InvocationTargetException e){
            //expected
            Assert.assertTrue(e.getCause() instanceof TooManyResourcesException);
        }
    }

    @Test(timeout = 30_000)
    public void systemTest(){

        EvoSuite evosuite = new EvoSuite();

        String targetClass = InfiniteWhile.class.getCanonicalName();
        Properties.TARGET_CLASS = targetClass;
        Properties.SEARCH_BUDGET = 10;
        Properties.TIMEOUT = 5000;
        Properties.STOPPING_CONDITION = Properties.StoppingCondition.MAXTIME;
        String[] command = new String[] { "-generateSuite", "-class", targetClass };

        Object result = evosuite.parseCommandLine(command);

        GeneticAlgorithm<?> ga = getGAFromResult(result);
        TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();

        System.out.println("EvolvedTestSuite:\n" + best);
        Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
    }
}
