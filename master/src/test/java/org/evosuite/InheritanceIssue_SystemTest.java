package org.evosuite;

import com.examples.with.different.packagename.inheritance.A;
import com.examples.with.different.packagename.inheritance.B;
import com.examples.with.different.packagename.inheritance.TheSuperClass;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by Andrea Arcuri on 16/07/15.
 */
public class InheritanceIssue_SystemTest extends SystemTest{

    private void doTest(Class<?> target){
        EvoSuite evosuite = new EvoSuite();

        String targetClass = target.getCanonicalName();

        Properties.TARGET_CLASS = targetClass;
        Properties.CRITERION = new Properties.Criterion[]{Properties.Criterion.LINE};

        String[] command = new String[] { "-generateSuite", "-class", targetClass };

        Object result = evosuite.parseCommandLine(command);
        GeneticAlgorithm<?> ga = getGAFromResult(result);
        TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
        System.out.println("EvolvedTestSuite:\n" + best);

        Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
    }

    @Test
    public void testSuper(){
        doTest(TheSuperClass.class);
    }

    @Test
    public void testA(){
        doTest(A.class);
    }

    @Test
    public void testB(){
        doTest(B.class);
    }

    @Test
    public void testCombination(){
        doTest(A.class);
        super.resetStaticVariables();
        super.setDefaultPropertiesForTestCases();
        doTest(B.class);
    }
}
