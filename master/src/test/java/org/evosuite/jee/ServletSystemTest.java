package org.evosuite.jee;

import com.examples.with.different.packagename.jee.injection.InjectionWithInheritance;
import com.examples.with.different.packagename.jee.servlet.PostPutGetServlet;
import com.examples.with.different.packagename.jee.servlet.SimpleHttpServlet;
import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.SystemTest;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by Andrea Arcuri on 01/07/15.
 */
public class ServletSystemTest extends SystemTest{



    @Test
    public void testSimpleCase_noJEE(){
        EvoSuite evosuite = new EvoSuite();

        String targetClass = SimpleHttpServlet.class.getCanonicalName();

        Properties.TARGET_CLASS = targetClass;
        Properties.CRITERION = new Properties.Criterion[]{Properties.Criterion.LINE};
        Properties.JEE = false;

        String[] command = new String[] { "-generateSuite", "-class", targetClass };

        Object result = evosuite.parseCommandLine(command);
        GeneticAlgorithm<?> ga = getGAFromResult(result);
        TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
        System.out.println("EvolvedTestSuite:\n" + best);

        Assert.assertTrue(best.getCoverage() < 1);
    }

    @Test
    public void testCombination() {
        testSimpleCase_noJEE();
        super.resetStaticVariables(); //After
        super.setDefaultPropertiesForTestCases(); //Before
        testSimpleCase_withJEE();
    }

    @Test
    public void testSimpleCase_withJEE(){
        Properties.JEE = true;
        do100percentLineTest(SimpleHttpServlet.class);
    }

    @Test
    public void testPostPutGetServlet(){
        Properties.JEE = true;
        do100percentLineTest(PostPutGetServlet.class);
    }

}
