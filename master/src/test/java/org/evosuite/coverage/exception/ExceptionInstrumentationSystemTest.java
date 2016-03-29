package org.evosuite.coverage.exception;

import com.examples.with.different.packagename.exception.*;
import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.SystemTestBase;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.strategy.TestGenerationStrategy;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by gordon on 17/03/2016.
 */
public class ExceptionInstrumentationSystemTest extends SystemTestBase {

    private static final Properties.Criterion[] defaultCriterion = Properties.CRITERION;

    private static boolean defaultArchive = Properties.TEST_ARCHIVE;

    @After
    public void resetProperties() {
        Properties.CRITERION = defaultCriterion;
    }

    public void checkCoverageGoals(Class<?> classUnderTest, int expectedGoals, int expectedCoveredGoals) {
        EvoSuite evosuite = new EvoSuite();

        String targetClass = classUnderTest.getCanonicalName();

        Properties.TARGET_CLASS = targetClass;
        Properties.CRITERION = new Properties.Criterion[]{Properties.Criterion.BRANCH};
        Properties.EXCEPTION_BRANCHES = true;

        String[] command = new String[] { "-generateSuite", "-class", targetClass };

        Object result = evosuite.parseCommandLine(command);
        GeneticAlgorithm<?> ga = getGAFromResult(result);
        TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
        System.out.println("EvolvedTestSuite:\n" + best);

        // # branches == 0
        // # branchless methods == 1 (<init>)
        // # additional branches: 4 (FileNotFoundException true/false, RuntimeException true/false)
        int goals = TestGenerationStrategy.getFitnessFactories().get(0).getCoverageGoals().size(); // assuming single fitness function
        Assert.assertEquals("Wrong number of goals: ", expectedGoals, goals);
        Assert.assertEquals("Non-optimal coverage: ", (double)expectedCoveredGoals/(double)expectedGoals, best.getCoverage(), 0.001);
    }


    @Test
    public void testCheckedExceptionBranchesOneThrow() {
        // # branches == 0
        // # branchless methods == 1 (<init>)
        // # additional branches: 4 (FileNotFoundException true/false, RuntimeException true/false)
        checkCoverageGoals(SimpleTryCatch.class, 5, 3);
    }

    @Test
    public void testCheckedExceptionBranchesTwoThrows() {
        checkCoverageGoals(SimpleTry2Catches.class, 7, 5);
    }

    @Test
    public void testReThrownCheckedExceptionBranchesTwoThrows() {
        checkCoverageGoals(Rethrow2Exceptions.class, 7, 5);
    }

    @Test
    public void testReThrownCheckedAndUncheckedExceptionBranchesTwoThrows() {
        checkCoverageGoals(Rethrow2ExceptionsAndUncheckedException.class, 7, 6);
    }

    @Test
    public void testReThrownCheckedAndErrorBranches() {
        Properties.ERROR_BRANCHES = true;
        // The NPE caused by "foo" being null is now caught outside the exception instrumentation
        // and thus represents a different coverage goal than a RuntimeException thrown _in_ foo.
        // Hence we now only cover 8/9 goals.
        checkCoverageGoals(Rethrow2ExceptionsAndUncheckedException.class, 9, 7);
    }

    @Test
    public void testCatchWithUnknownThrow() {
        checkCoverageGoals(CatchWithUnknownThrow.class, 5, 4);
    }

}
