package org.evosuite.jee;

import com.examples.with.different.packagename.jee.*;
import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.SystemTest;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by Andrea Arcuri on 29/06/15.
 */
public class InjectionSystemTest extends SystemTest{

    private void doTest(Class<?> target){
        EvoSuite evosuite = new EvoSuite();

        String targetClass = target.getCanonicalName();

        Properties.TARGET_CLASS = targetClass;
        Properties.CRITERION = new Properties.Criterion[]{Properties.Criterion.LINE};
        Properties.JEE = true;
        Properties.P_REFLECTION_ON_PRIVATE = 0.0;

        String[] command = new String[] { "-generateSuite", "-class", targetClass };

        Object result = evosuite.parseCommandLine(command);
        GeneticAlgorithm<?> ga = getGAFromResult(result);
        TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
        System.out.println("EvolvedTestSuite:\n" + best);

        Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
    }

    @Test
    public void testInjectionSimpleInheritance(){
        doTest(InjectionSimpleInheritance.class);
    }

    @Test
    public void testInjectionTrivialInheritance(){
        doTest(InjectionTrivialInheritance.class);
    }


    @Test
    public void testInjectionWithInheritance(){
        doTest(InjectionWithInheritance.class);
    }

    @Test
    public void testCombination(){
        doTest(InjectionWithInheritance.class);
        super.resetStaticVariables(); //After
        super.setDefaultPropertiesForTestCases(); //Before
        doTest(InjectionSimpleInheritance.class);
    }

    @Test
    public void testCombination3(){
        doTest(InjectionWithInheritance.class);
        super.resetStaticVariables(); //After
        super.setDefaultPropertiesForTestCases(); //Before
        doTest(GeneralInjectionExample.class);
        super.resetStaticVariables(); //After
        super.setDefaultPropertiesForTestCases(); //Before
        doTest(InjectionSimpleInheritance.class);
    }

    @Test
    public void testInjectionAndPostConstruct(){
        doTest(InjectionAndPostConstruct.class);
    }

    @Test
    public void testGeneralInjection(){
        doTest(GeneralInjectionExample.class);
    }

    @Test
    public void testPostConstructor(){
        doTest(PostConstructorInjection.class);
    }

    @Test
    public void testEntityManager(){
        doTest(EntityManagerInjection.class);
    }


    @Test
    public void testMultipleDefaultInjections(){
        doTest(MultipleDefaultInjections.class);
    }
}
