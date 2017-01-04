package org.evosuite.testcase;

import com.examples.with.different.packagename.staticfield.StaticFinalAssignment;
import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.SystemTestBase;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.statistics.OutputVariable;
import org.evosuite.statistics.RuntimeVariable;
import org.evosuite.statistics.backend.DebugStatisticsBackend;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.Assert;
import org.junit.Test;

import java.util.Map;

/**
 * Created by gordon on 04/01/2017.
 */
public class StaticFinalFieldAssignmentSystemTest extends SystemTestBase {

    @Test
    public void test() {
        EvoSuite evosuite = new EvoSuite();

        String targetClass = StaticFinalAssignment.class.getCanonicalName();
        Properties.TARGET_CLASS = targetClass;
        String[] command = new String[] { "-generateSuite", "-class", targetClass };
        // Properties.OUTPUT_VARIABLES = "" + RuntimeVariable.HadUnstableTests;

        Object result = evosuite.parseCommandLine(command);

        GeneticAlgorithm<?> ga = getGAFromResult(result);
        TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();

        String code = best.toString();

        Assert.assertFalse("Contains illegal assignment to final variable: "+code, code.contains(".FOO = "));
    }
}
