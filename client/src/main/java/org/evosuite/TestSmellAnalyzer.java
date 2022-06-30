package org.evosuite;

import org.evosuite.rmi.ClientServices;
import org.evosuite.statistics.RuntimeVariable;
import org.evosuite.testsmells.AbstractTestSmell;
import org.evosuite.testsmells.smells.*;
import org.evosuite.testsuite.TestSuiteChromosome;

import java.util.ArrayList;
import java.util.List;

public class TestSmellAnalyzer {

    /**
     * Write the number of test smells
     * @param testSuite The analyzed test suite
     */
    public static void writeNumTestSmells(TestSuiteChromosome testSuite){

        double specificSmell;

        List<String> listOfVariables = new ArrayList<>();

        for(String entry : Properties.OUTPUT_VARIABLES.split(",")){
            listOfVariables.add(entry.trim());
        }

        if(listOfVariables.contains("AllTestSmells")){

            double smellCount = 0;

            List<AbstractTestSmell> listOfTestSmells = initializeTestSmells();

            for(AbstractTestSmell currentSmell : listOfTestSmells){

                specificSmell = currentSmell.computeTestSmellMetric(testSuite);

                if (!Double.isNaN(specificSmell)){
                    smellCount += specificSmell;
                }

                if(listOfVariables.contains(currentSmell.getName())){
                    ClientServices.track(RuntimeVariable.valueOf(currentSmell.getName()), specificSmell);
                }
            }

            ClientServices.track(RuntimeVariable.valueOf("AllTestSmells"), smellCount);

        }else {

            for (String entry : listOfVariables){

                AbstractTestSmell testSmell = getAbstractTestSmell(entry);

                if(testSmell != null){
                    specificSmell = testSmell.computeTestSmellMetric(testSuite);
                    ClientServices.track(RuntimeVariable.valueOf(testSmell.getName()), specificSmell);
                }
            }
        }
    }

    /**
     * Write the number of test smells for each test case
     * @param testSuite The analyzed test suite
     */
    public static void writeIndividualTestSmells(TestSuiteChromosome testSuite){

        double specificSmell;
        String smellCountForEachTestCase;

        List<String> listOfVariables = new ArrayList<>();

        for(String entry : Properties.OUTPUT_VARIABLES.split(",")){
            listOfVariables.add(entry.trim());
        }

        if(listOfVariables.contains("AllTestSmells")){

            double smellCount = 0;

            List<AbstractTestSmell> listOfTestSmells = initializeTestSmells();

            for(AbstractTestSmell currentSmell : listOfTestSmells){

                specificSmell = currentSmell.computeTestSmellMetric(testSuite);

                if (!Double.isNaN(specificSmell)){
                    smellCount += specificSmell;
                }

                if(listOfVariables.contains(currentSmell.getName())){
                    smellCountForEachTestCase = currentSmell.computeTestSmellMetricForEachTestCase(testSuite);
                    ClientServices.track(RuntimeVariable.valueOf(currentSmell.getName()), smellCountForEachTestCase);
                }
            }

            ClientServices.track(RuntimeVariable.valueOf("AllTestSmells"), smellCount);

        }else {

            for (String entry : listOfVariables){

                AbstractTestSmell testSmell = getAbstractTestSmell(entry);

                if(testSmell != null){
                    smellCountForEachTestCase = testSmell.computeTestSmellMetricForEachTestCase(testSuite);
                    ClientServices.track(RuntimeVariable.valueOf(testSmell.getName()), smellCountForEachTestCase);
                }
            }
        }
    }

    /**
     * Write the number of test smells before minimization
     * @param testSuite The analyzed test suite
     */
    public static void writeNumTestSmellsBeforePostProcess(TestSuiteChromosome testSuite){

        double specificSmell;

        List<String> listOfVariables = new ArrayList<>();

        for(String entry : Properties.OUTPUT_VARIABLES.split(",")){
            listOfVariables.add(entry.trim());
        }

        if(listOfVariables.contains("AllTestSmellsBeforePostProcess")){
            double smellCount = 0;

            List<AbstractTestSmell> listOfTestSmells = initializeTestSmells();

            for(AbstractTestSmell currentSmell : listOfTestSmells){

                specificSmell = currentSmell.computeTestSmellMetric(testSuite);

                if (!Double.isNaN(specificSmell)){
                    smellCount += specificSmell;
                }

                if(listOfVariables.contains(currentSmell.getName() + "BeforePostProcess")){
                    ClientServices.track(RuntimeVariable.valueOf(currentSmell.getName() + "BeforePostProcess"), specificSmell);
                }
            }

            ClientServices.track(RuntimeVariable.valueOf("AllTestSmellsBeforePostProcess"), smellCount);

        } else {

            for (String entry : listOfVariables){

                AbstractTestSmell testSmell = getAbstractTestSmellBeforeMinimization(entry);

                if(testSmell != null){
                    specificSmell = testSmell.computeTestSmellMetric(testSuite);
                    ClientServices.track(RuntimeVariable.valueOf(entry), specificSmell);
                }
            }
        }
    }

    /**
     * Write the number of test smells for each test case before minimization
     * @param testSuite The analyzed test suite
     */
    public static void writeNumIndividualTestSmellsBeforePostProcess(TestSuiteChromosome testSuite){

        double specificSmell;
        String smellCountForEachTestCase;

        List<String> listOfVariables = new ArrayList<>();

        for(String entry : Properties.OUTPUT_VARIABLES.split(",")){
            listOfVariables.add(entry.trim());
        }

        if(listOfVariables.contains("AllTestSmellsBeforePostProcess")){
            double smellCount = 0;

            List<AbstractTestSmell> listOfTestSmells = initializeTestSmells();

            for(AbstractTestSmell currentSmell : listOfTestSmells){

                specificSmell = currentSmell.computeTestSmellMetric(testSuite);

                if (!Double.isNaN(specificSmell)){
                    smellCount += specificSmell;
                }

                if(listOfVariables.contains(currentSmell.getName() + "BeforePostProcess")){
                    smellCountForEachTestCase = currentSmell.computeTestSmellMetricForEachTestCase(testSuite);
                    ClientServices.track(RuntimeVariable.valueOf(currentSmell.getName() + "BeforePostProcess"), smellCountForEachTestCase);
                }
            }

            ClientServices.track(RuntimeVariable.valueOf("AllTestSmellsBeforePostProcess"), smellCount);

        } else {

            for (String entry : listOfVariables){

                AbstractTestSmell testSmell = getAbstractTestSmellBeforeMinimization(entry);

                if(testSmell != null){
                    smellCountForEachTestCase = testSmell.computeTestSmellMetricForEachTestCase(testSuite);
                    ClientServices.track(RuntimeVariable.valueOf(entry), smellCountForEachTestCase);
                }
            }
        }
    }

    /**
     * Initialize all the test smell metrics
     * @return List containing all test smells
     */
    private static List<AbstractTestSmell> initializeTestSmells(){

        List<AbstractTestSmell> listOfTestSmells = new ArrayList<>();

        listOfTestSmells.add(new AssertionRoulette());
        listOfTestSmells.add(new BrittleAssertion());
        listOfTestSmells.add(new DuplicateAssert());
        listOfTestSmells.add(new EagerTest());
        listOfTestSmells.add(new EmptyTest());
        listOfTestSmells.add(new IndirectTesting());
        listOfTestSmells.add(new LackOfCohesionOfMethods());
        listOfTestSmells.add(new LazyTest());
        listOfTestSmells.add(new LikelyIneffectiveObjectComparison());
        listOfTestSmells.add(new MysteryGuest());
        listOfTestSmells.add(new ObscureInlineSetup());
        listOfTestSmells.add(new Overreferencing());
        listOfTestSmells.add(new RedundantAssertion());
        listOfTestSmells.add(new ResourceOptimism());
        listOfTestSmells.add(new RottenGreenTests());
        listOfTestSmells.add(new SlowTests());
        //listOfTestSmells.add(new TestCodeDuplication());
        listOfTestSmells.add(new TestRedundancy());
        listOfTestSmells.add(new UnknownTest());
        listOfTestSmells.add(new UnrelatedAssertions());
        listOfTestSmells.add(new UnusedInputs());
        listOfTestSmells.add(new VerboseTest());

        return listOfTestSmells;
    }

    /**
     * Initialize a specific test smell metric
     * @param smellName Name of the test smell
     * @return AbstractTestSmell that corresponds to the respective test smell
     */
    private static AbstractTestSmell getAbstractTestSmell (String smellName) {

        switch (smellName){
            case "TestSmellAssertionRoulette":
                return new AssertionRoulette();
            case "TestSmellBrittleAssertion":
                return new BrittleAssertion();
            case "TestSmellDuplicateAssert":
                return new DuplicateAssert();
            case "TestSmellEagerTest":
                return new EagerTest();
            case "TestSmellEmptyTest":
                return new EmptyTest();
            case "TestSmellIndirectTesting":
                return new IndirectTesting();
            case "TestSmellLackOfCohesionOfMethods":
                return new LackOfCohesionOfMethods();
            case "TestSmellLazyTest":
                return new LazyTest();
            case "TestSmellLikelyIneffectiveObjectComparison":
                return new LikelyIneffectiveObjectComparison();
            case "TestSmellMysteryGuest":
                return new MysteryGuest();
            case "TestSmellObscureInlineSetup":
                return new ObscureInlineSetup();
            case "TestSmellOverreferencing":
                return new Overreferencing();
            case "TestSmellRedundantAssertion":
                return new RedundantAssertion();
            case "TestSmellResourceOptimism":
                return new ResourceOptimism();
            case "TestSmellRottenGreenTests":
                return new RottenGreenTests();
            case "TestSmellSlowTests":
                return new SlowTests();
            //case "TestSmellTestCodeDuplication":
            //    return new TestCodeDuplication();
            case "TestSmellTestRedundancy":
                return new TestRedundancy();
            case "TestSmellUnknownTest":
                return new UnknownTest();
            case "TestSmellUnrelatedAssertions":
                return new UnrelatedAssertions();
            case "TestSmellUnusedInputs":
                return new UnusedInputs();
            case "TestSmellVerboseTest":
                return new VerboseTest();
            default:
                return null;
        }
    }

    /**
     * Initialize a specific test smell metric (before minimization)
     * @param smellName Name of the test smell
     * @return AbstractTestSmell that corresponds to the respective test smell
     */
    private static AbstractTestSmell getAbstractTestSmellBeforeMinimization (String smellName) {

        switch (smellName){
            case "TestSmellAssertionRouletteBeforePostProcess":
                return new AssertionRoulette();
            case "TestSmellBrittleAssertionBeforePostProcess":
                return new BrittleAssertion();
            case "TestSmellDuplicateAssertBeforePostProcess":
                return new DuplicateAssert();
            case "TestSmellEagerTestBeforePostProcess":
                return new EagerTest();
            case "TestSmellEmptyTestBeforePostProcess":
                return new EmptyTest();
            case "TestSmellIndirectTestingBeforePostProcess":
                return new IndirectTesting();
            case "TestSmellLackOfCohesionOfMethodsBeforePostProcess":
                return new LackOfCohesionOfMethods();
            case "TestSmellLazyTestBeforePostProcess":
                return new LazyTest();
            case "TestSmellLikelyIneffectiveObjectComparisonBeforePostProcess":
                return new LikelyIneffectiveObjectComparison();
            case "TestSmellMysteryGuestBeforePostProcess":
                return new MysteryGuest();
            case "TestSmellObscureInlineSetupBeforePostProcess":
                return new ObscureInlineSetup();
            case "TestSmellOverreferencingBeforePostProcess":
                return new Overreferencing();
            case "TestSmellRedundantAssertionBeforePostProcess":
                return new RedundantAssertion();
            case "TestSmellResourceOptimismBeforePostProcess":
                return new ResourceOptimism();
            case "TestSmellRottenGreenTestsBeforePostProcess":
                return new RottenGreenTests();
            case "TestSmellSlowTestsBeforePostProcess":
                return new SlowTests();
            //case "TestSmellTestCodeDuplicationBeforePostProcess":
            //    return new TestCodeDuplication();
            case "TestSmellTestRedundancyBeforePostProcess":
                return new TestRedundancy();
            case "TestSmellUnknownTestBeforePostProcess":
                return new UnknownTest();
            case "TestSmellUnrelatedAssertionsBeforePostProcess":
                return new UnrelatedAssertions();
            case "TestSmellUnusedInputsBeforePostProcess":
                return new UnusedInputs();
            case "TestSmellVerboseTestBeforePostProcess":
                return new VerboseTest();
            default:
                return null;
        }
    }
}
