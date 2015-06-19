package org.evosuite.coverage;

import com.examples.with.different.packagename.SingleMethod;
import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.SystemTest;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.result.TestGenerationResult;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.Assert;
import org.junit.Test;
import java.util.List;
import static org.hamcrest.CoreMatchers.*;

/**
 * Created by jrojas
 */
public class TestCoveredGoalsCount extends SystemTest {

    @Test
    public void testCoveredGoalsCount() {
        EvoSuite evosuite = new EvoSuite();

        String targetClass = SingleMethod.class.getCanonicalName();
        Properties.TARGET_CLASS = targetClass;

        Properties.TEST_ARCHIVE = false;// || true;
        Properties.MINIMIZE = false;// || true;
        Properties.COVERAGE = false;// || true;
        Properties.POPULATION = 1;
        Properties.MAX_LENGTH = 3;
        Properties.CHROMOSOME_LENGTH = 3;

        Properties.CRITERION = new Properties.Criterion[] {Properties.Criterion.ONLYLINE};

        String[] command = new String[] { "-generateSuite", "-class", targetClass };
        List<List<TestGenerationResult>> result = (List<List<TestGenerationResult>>)evosuite.parseCommandLine(command);
        GeneticAlgorithm<?> ga = getGAFromResult(result);
        TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();

        System.out.println("EvolvedTestSuite:\n" + best);
        System.out.println("CoveredGoals: " + best.getCoveredGoals());
        Assert.assertEquals("getCoveredGoals().size()", 1, best.getCoveredGoals().size());
        Assert.assertEquals("getNumOfCoveredGoals()", 1, best.getNumOfCoveredGoals());
    }

}
