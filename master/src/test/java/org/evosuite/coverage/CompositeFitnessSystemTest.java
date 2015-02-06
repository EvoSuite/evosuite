package org.evosuite.coverage;

import com.examples.with.different.packagename.coverage.BooleanOneLine;
import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.SystemTest;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by arcuri on 12/24/14.
 */
public class CompositeFitnessSystemTest extends SystemTest{

    private final boolean COMPOSITIONAL_FITNESS = Properties.COMPOSITIONAL_FITNESS;

    @After
    public void afterTest() {
        Properties.COMPOSITIONAL_FITNESS = COMPOSITIONAL_FITNESS;
    }


    @Test
    public void testBooleanOneLine_3(){
        EvoSuite evosuite = new EvoSuite();

        String targetClass = BooleanOneLine.class.getCanonicalName();
        Properties.TARGET_CLASS = targetClass;
        Properties.COMPOSITIONAL_FITNESS = true;
        Properties.CRITERION = new Properties.Criterion[]{
                Properties.Criterion.LINE,
                Properties.Criterion.OUTPUT,
                Properties.Criterion.EXCEPTION
        };

        String[] command = new String[] { "-generateSuite", "-class", targetClass };
        Object result = evosuite.parseCommandLine(command);
        GeneticAlgorithm<?> ga = getGAFromResult(result);
        TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();

        /*
            Should be 3 tests:
            1) return true
            2) return false
            3) NPE
         */
        Assert.assertEquals(3, best.getTests().size());
    }
}
