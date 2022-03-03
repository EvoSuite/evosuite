package org.evosuite;

import com.examples.with.different.packagename.mutation.SimpleMutationExample2;
import com.examples.with.different.packagename.testsmells.TestSmellsServer;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.strategy.TestGenerationStrategy;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.Assert;
import org.junit.Test;

public class DummySystemTest extends SystemTestBase{

    @Test
    public void test1() {
        EvoSuite evosuite = new EvoSuite();

        //String targetClass = SimpleMutationExample2.class.getCanonicalName();
        String targetClass = TestSmellsServer.class.getCanonicalName();

        String[] command = new String[] { "-generateMOSuite", "-class", targetClass };
        Object result = evosuite.parseCommandLine(command);
        GeneticAlgorithm<TestSuiteChromosome> ga = getGAFromResult(result);
        TestSuiteChromosome best = ga.getBestIndividual();

        System.out.println("CoveredGoals:\n" + best.getCoveredGoals());
        System.out.println("EvolvedTestSuite:\n" + best);
    }

}
