package org.evosuite.mock.java.net;

import com.examples.with.different.packagename.InfiniteWhile;
import com.examples.with.different.packagename.agent.GetURL;
import com.examples.with.different.packagename.mock.java.net.ReadFromInputURL;
import com.examples.with.different.packagename.mock.java.net.ReadFromURL;
import com.examples.with.different.packagename.mock.java.net.ReceiveTcp;

import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.SystemTest;
import org.evosuite.TestSuiteGenerator;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.runtime.RuntimeSettings;
import org.evosuite.runtime.instrumentation.InstrumentingClassLoader;
import org.evosuite.runtime.instrumentation.MethodCallReplacementCache;
import org.evosuite.runtime.testdata.EvoSuiteURL;
import org.evosuite.runtime.testdata.NetworkHandling;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.junit.Assert.fail;

/**
 * Created by arcuri on 12/19/14.
 */
public class MockUrlSystemTest extends SystemTest{

    @Test(timeout = 5000)
    public void testLoading_ReadFromURL() throws Exception{
        //for some reason, this class failed when using loop limit in the search

        RuntimeSettings.useVNET = true;
        RuntimeSettings.maxNumberOfIterationsPerLoop = 100_000;
        MethodCallReplacementCache.resetSingleton();
        org.evosuite.runtime.Runtime.getInstance().resetRuntime();

        InstrumentingClassLoader loader = new InstrumentingClassLoader();
        Class<?> clazz = loader.loadClass(ReadFromURL.class.getCanonicalName());


        Method m = clazz.getMethod("checkResource");
        boolean b = (Boolean) m.invoke(null);
        Assert.assertFalse(b);

        EvoSuiteURL evoURL = new EvoSuiteURL("http://www.evosuite.org/index.html");
        NetworkHandling.createRemoteTextFile(evoURL, "foo");
        b = (Boolean) m.invoke(null);
        Assert.assertTrue(b);
    }


    @Test
    public void testCheckResource(){
        EvoSuite evosuite = new EvoSuite();

        String targetClass = ReadFromURL.class.getCanonicalName();

        Properties.TARGET_CLASS = targetClass;
        Properties.SEARCH_BUDGET = 20000;
        Properties.VIRTUAL_NET = true;

        Properties.MAX_LOOP_ITERATIONS = 100000; //FIXME why search fails if this is on???

        String[] command = new String[] { "-generateSuite", "-class", targetClass };

        Object result = evosuite.parseCommandLine(command);
        Assert.assertTrue(result != null);

        GeneticAlgorithm<?> ga = getGAFromResult(result);
        TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
        System.out.println("EvolvedTestSuite:\n" + best);

        int goals = TestSuiteGenerator.getFitnessFactory().get(0).getCoverageGoals().size(); // assuming single fitness function
        Assert.assertEquals("Wrong number of goals: ", 3, goals);
        Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
    }

    @Test
    public void testUrlAsInput(){
        EvoSuite evosuite = new EvoSuite();

        String targetClass = ReadFromInputURL.class.getCanonicalName();

        Properties.TARGET_CLASS = targetClass;
        Properties.SEARCH_BUDGET = 20000;
        Properties.VIRTUAL_NET = true;

        String[] command = new String[] { "-generateSuite", "-class", targetClass };

        Object result = evosuite.parseCommandLine(command);
        Assert.assertTrue(result != null);

        GeneticAlgorithm<?> ga = getGAFromResult(result);
        TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
        System.out.println("EvolvedTestSuite:\n" + best);

        int goals = TestSuiteGenerator.getFitnessFactory().get(0).getCoverageGoals().size(); // assuming single fitness function
        Assert.assertEquals("Wrong number of goals: ", 5, goals);
        Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
    }


}
