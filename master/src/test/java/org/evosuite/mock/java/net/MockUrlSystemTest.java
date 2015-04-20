package org.evosuite.mock.java.net;

import com.examples.with.different.packagename.agent.GetURL;
import com.examples.with.different.packagename.mock.java.net.ReadFromInputURL;
import com.examples.with.different.packagename.mock.java.net.ReadFromURL;
import com.examples.with.different.packagename.mock.java.net.ReceiveTcp;
import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.SystemTest;
import org.evosuite.TestSuiteGenerator;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by arcuri on 12/19/14.
 */
public class MockUrlSystemTest extends SystemTest{

    private static final boolean VNET = Properties.VIRTUAL_NET;

    @After
    public void restoreProperties(){
        Properties.VIRTUAL_NET = VNET;
    }

    @Test
    public void testCheckResource(){
        EvoSuite evosuite = new EvoSuite();

        String targetClass = ReadFromURL.class.getCanonicalName();

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
