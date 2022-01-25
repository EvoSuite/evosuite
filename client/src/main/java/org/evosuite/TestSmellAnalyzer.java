package org.evosuite;

import org.evosuite.rmi.ClientServices;
import org.evosuite.statistics.RuntimeVariable;
import org.evosuite.testsmells.AbstractTestSmell;
import org.evosuite.testsmells.smells.*;
import org.evosuite.testsuite.TestSuiteChromosome;

import java.util.ArrayList;
import java.util.List;

public class TestSmellAnalyzer {

    public static void writeNumTestSmells(TestSuiteChromosome testSuite){

        int specificSmell;

        List<String> listOfVariables = new ArrayList<>();

        for(String entry : Properties.OUTPUT_VARIABLES.split(",")){
            listOfVariables.add(entry.trim());
        }

        if(listOfVariables.contains("AllTestSmells")){

            int smellCount = 0;

            List<AbstractTestSmell> listOfTestSmells = initializeTestSmells();

            for(AbstractTestSmell currentSmell : listOfTestSmells){

                specificSmell = currentSmell.computeNumberOfSmells(testSuite);
                smellCount += specificSmell;

                if(listOfVariables.contains(currentSmell.getName())){
                    ClientServices.track(RuntimeVariable.valueOf(currentSmell.getName()), specificSmell);
                }
            }

            ClientServices.track(RuntimeVariable.valueOf("AllTestSmells"), smellCount);

        }else {

            for (String entry : listOfVariables){

                AbstractTestSmell testSmell = getAbstractTestSmell(entry);

                if(testSmell != null){
                    specificSmell = testSmell.computeNumberOfSmells(testSuite);
                    ClientServices.track(RuntimeVariable.valueOf(testSmell.getName()), specificSmell);
                }
            }
        }
    }

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
        listOfTestSmells.add(new RedundantAssertion());
        listOfTestSmells.add(new SensitiveEquality());
        listOfTestSmells.add(new SlowTests());
        listOfTestSmells.add(new UnknownTest());
        listOfTestSmells.add(new UnusedInputs());
        listOfTestSmells.add(new VerboseTest());

        return listOfTestSmells;
    }

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
            case "TestSmellRedundantAssertion":
                return new RedundantAssertion();
            case "TestSmellSensitiveEquality":
                return new SensitiveEquality();
            case "TestSmellSlowTests":
                return new SlowTests();
            case "TestSmellUnknownTest":
                return new UnknownTest();
            case "TestSmellUnusedInputs":
                return new UnusedInputs();
            case "TestSmellVerboseTest":
                return new VerboseTest();
            default:
                return null;
        }
    }
}
