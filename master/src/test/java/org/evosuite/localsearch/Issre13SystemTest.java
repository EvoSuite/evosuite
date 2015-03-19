package org.evosuite.localsearch;

import com.examples.with.different.packagename.localsearch.DseBar;
import com.examples.with.different.packagename.localsearch.IntegerLocalSearchExample;
import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.SystemTest;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by Andrea Arcuri on 19/03/15.
 */
public class Issre13SystemTest extends SystemTest{

    @Before
    public void init(){
        Properties.LOCAL_SEARCH_PROBABILITY = 1.0;
        Properties.LOCAL_SEARCH_RATE = 1;
        Properties.LOCAL_SEARCH_BUDGET_TYPE = Properties.LocalSearchBudgetType.TESTS;
        Properties.LOCAL_SEARCH_BUDGET = 100;
        Properties.SEARCH_BUDGET = 50000;
    }

    @Test
    public void testLocalSearch(){

        //it should be trivial for LS

        EvoSuite evosuite = new EvoSuite();
        String targetClass = DseBar.class.getCanonicalName();
        Properties.TARGET_CLASS = targetClass;

        Properties.DSE_PROBABILITY = 0.0; //force using only LS, no DSE

        String[] command = new String[] { "-generateSuite", "-class", targetClass };

        Object result = evosuite.parseCommandLine(command);
        GeneticAlgorithm<?> ga = getGAFromResult(result);
        TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
        System.out.println("EvolvedTestSuite:\n" + best);

        Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
    }

    @Test
    public void testDSE(){

        //should it be trivial for DSE ?

        EvoSuite evosuite = new EvoSuite();
        String targetClass = DseBar.class.getCanonicalName();
        Properties.TARGET_CLASS = targetClass;

        Properties.DSE_PROBABILITY = 1.0; //force using only DSE, no LS

        String[] command = new String[] { "-generateSuite", "-class", targetClass };

        Object result = evosuite.parseCommandLine(command);
        GeneticAlgorithm<?> ga = getGAFromResult(result);
        TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
        System.out.println("EvolvedTestSuite:\n" + best);

        Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
    }

}
