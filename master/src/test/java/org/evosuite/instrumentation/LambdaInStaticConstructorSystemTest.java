package org.evosuite.instrumentation;

import com.examples.with.different.packagename.LambdaInStaticConstructor;
import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.SystemTestBase;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.Assert;
import org.junit.Test;

public class LambdaInStaticConstructorSystemTest extends SystemTestBase {

    @Test
    public void testNoCrashInCLINIT() throws Throwable{
        String targetClass = LambdaInStaticConstructor.class.getCanonicalName();
        Properties.TARGET_CLASS = targetClass;
        Properties.TIMEOUT = 50000000;
        Properties.RESET_STATIC_FINAL_FIELDS = true;

        EvoSuite evosuite = new EvoSuite();
        String[] command = new String[] { "-generateSuite", "-class", targetClass };

        Object result = evosuite.parseCommandLine(command);

        GeneticAlgorithm<?> ga = getGAFromResult(result);

        TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
        System.out.println(best.toString());

    }
}
