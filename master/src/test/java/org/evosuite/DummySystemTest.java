package org.evosuite;

import com.examples.with.different.packagename.mutation.SimpleMutationExample2;
import com.examples.with.different.packagename.testsmells.*;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.Test;

public class DummySystemTest extends SystemTestBase{

    @Test
    public void test1() {
        EvoSuite evosuite = new EvoSuite();

        Properties.CRITERION = new Properties.Criterion[]{Properties.Criterion.LINE, Properties.Criterion.BRANCH, Properties.Criterion.EXCEPTION, Properties.Criterion.WEAKMUTATION,
                Properties.Criterion.OUTPUT, Properties.Criterion.METHOD, Properties.Criterion.METHODNOEXCEPTION, Properties.Criterion.CBRANCH};

        //String targetClass = SimpleMutationExample2.class.getCanonicalName();
        String targetClass = TestSmellsServer.class.getCanonicalName();
        //String targetClass = TestSmellsTestingClass1.class.getCanonicalName();

        //-----------------------------
        // Set the secondary objective:
        //-----------------------------

        //Properties.SECONDARY_OBJECTIVE = new Properties.SecondaryObjective[]{Properties.SecondaryObjective.TEST_SMELL_EAGER_TEST};

        //Properties.SECONDARY_OBJECTIVE = new Properties.SecondaryObjective[]{Properties.SecondaryObjective.TEST_SMELL_EAGER_TEST, Properties.SecondaryObjective.TEST_SMELL_EMPTY_TEST,
        //        Properties.SecondaryObjective.TEST_SMELL_INDIRECT_TESTING, Properties.SecondaryObjective.TEST_SMELL_VERBOSE_TEST};

        //Properties.SECONDARY_OBJECTIVE = new Properties.SecondaryObjective[]{Properties.SecondaryObjective.TEST_SMELL_INDIRECT_TESTING,
        //        Properties.SecondaryObjective.TEST_SMELL_ROTTEN_GREEN_TESTS};

        // Test smell metrics that can be optimized as secondary criteria:
        Properties.SECONDARY_OBJECTIVE = new Properties.SecondaryObjective[]{Properties.SecondaryObjective.TEST_SMELL_EAGER_TEST,
                Properties.SecondaryObjective.TEST_SMELL_INDIRECT_TESTING, Properties.SecondaryObjective.TEST_SMELL_LIKELY_INEFFECTIVE_OBJECT_COMPARISON,
                Properties.SecondaryObjective.TEST_SMELL_OBSCURE_INLINE_SETUP,Properties.SecondaryObjective.TEST_SMELL_OVERREFERENCING,
                Properties.SecondaryObjective.TEST_SMELL_ROTTEN_GREEN_TESTS, Properties.SecondaryObjective.TEST_SMELL_VERBOSE_TEST};

        // Optimal combination of test smell metrics to optimize as secondary criteria:
        //Properties.SECONDARY_OBJECTIVE = new Properties.SecondaryObjective[]{Properties.SecondaryObjective.TEST_SMELL_INDIRECT_TESTING,
        //        Properties.SecondaryObjective.TEST_SMELL_OBSCURE_INLINE_SETUP,Properties.SecondaryObjective.TEST_SMELL_OVERREFERENCING,
        //        Properties.SecondaryObjective.TEST_SMELL_ROTTEN_GREEN_TESTS, Properties.SecondaryObjective.TEST_SMELL_VERBOSE_TEST};


        //Properties.SECONDARY_OBJECTIVE = new Properties.SecondaryObjective[]{Properties.SecondaryObjective.TEST_SMELL_INDIRECT_TESTING,
        //        Properties.SecondaryObjective.TEST_SMELL_INDIRECT_TESTING};

        //Properties.SECONDARY_OBJECTIVE = new Properties.SecondaryObjective[]{Properties.SecondaryObjective.RANDOM};

        //--------------------------
        // Set the output variables:
        //--------------------------

        //Properties.OUTPUT_VARIABLES = "AllTestSmellsBeforePostProcess,AllTestSmells";

        Properties.OUTPUT_VARIABLES = "AllTestSmellsBeforePostProcess,\n" +
                "TestSmellAssertionRouletteBeforePostProcess,\n" +
                "TestSmellBrittleAssertionBeforePostProcess,\n" +
                "TestSmellDuplicateAssertBeforePostProcess,\n" +
                "TestSmellEagerTestBeforePostProcess,\n" +
                "TestSmellEmptyTestBeforePostProcess,\n" +
                "TestSmellIndirectTestingBeforePostProcess,\n" +
                "TestSmellLackOfCohesionOfMethodsBeforePostProcess,\n" +
                "TestSmellLazyTestBeforePostProcess,\n" +
                "TestSmellLikelyIneffectiveObjectComparisonBeforePostProcess,\n" +
                "TestSmellMysteryGuestBeforePostProcess,\n" +
                "TestSmellObscureInlineSetupBeforePostProcess,\n" +
                "TestSmellOverreferencingBeforePostProcess,\n" +
                "TestSmellRedundantAssertionBeforePostProcess,\n" +
                "TestSmellResourceOptimismBeforePostProcess,\n" +
                "TestSmellRottenGreenTestsBeforePostProcess,\n" +
                "TestSmellSlowTestsBeforePostProcess,\n" +
                "TestSmellTestRedundancyBeforePostProcess,\n" +
                "TestSmellUnknownTestBeforePostProcess,\n" +
                "TestSmellUnrelatedAssertionsBeforePostProcess,\n" +
                "TestSmellUnusedInputsBeforePostProcess,\n" +
                "TestSmellVerboseTestBeforePostProcess,\n" +
                "AllTestSmells,\n" +
                "TestSmellAssertionRoulette,\n" +
                "TestSmellBrittleAssertion,\n" +
                "TestSmellDuplicateAssert,\n" +
                "TestSmellEagerTest,\n" +
                "TestSmellEmptyTest,\n" +
                "TestSmellIndirectTesting,\n" +
                "TestSmellLackOfCohesionOfMethods,\n" +
                "TestSmellLazyTest,\n" +
                "TestSmellLikelyIneffectiveObjectComparison,\n" +
                "TestSmellMysteryGuest,\n" +
                "TestSmellObscureInlineSetup,\n" +
                "TestSmellOverreferencing,\n" +
                "TestSmellRedundantAssertion,\n" +
                "TestSmellResourceOptimism,\n" +
                "TestSmellRottenGreenTests,\n" +
                "TestSmellSlowTests,\n" +
                "TestSmellTestRedundancy,\n" +
                "TestSmellUnknownTest,\n" +
                "TestSmellUnrelatedAssertions,\n" +
                "TestSmellUnusedInputs,\n" +
                "TestSmellVerboseTest";

        //---------------------
        // Set the tiebreakers:
        //---------------------

        //Properties.TIEBREAKER = new Properties.SecondaryObjective[]{Properties.SecondaryObjective.TEST_SMELL_EAGER_TEST,
        //        Properties.SecondaryObjective.TEST_SMELL_ROTTEN_GREEN_TESTS, Properties.SecondaryObjective.TEST_SMELL_VERBOSE_TEST};

        //---------
        // Command:
        //---------

        String[] command = new String[] { "-generateMOSuite", "-class", targetClass };
        //String[] command = new String[] { "-generateMOSuite", "-class", targetClass, "-Dtest_smell_list=false" };

        Object result = evosuite.parseCommandLine(command);
        GeneticAlgorithm<TestSuiteChromosome> ga = getGAFromResult(result);
        TestSuiteChromosome best = ga.getBestIndividual();

        System.out.println("CoveredGoals:\n" + best.getCoveredGoals());
        System.out.println("EvolvedTestSuite:\n" + best);
    }

}
