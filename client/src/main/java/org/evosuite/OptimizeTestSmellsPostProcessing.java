package org.evosuite;

import org.evosuite.statistics.RuntimeVariable;
import org.evosuite.testsmells.AbstractTestSmell;
import org.evosuite.testsmells.smells.*;
import org.evosuite.testsuite.TestSuiteChromosome;

import java.util.ArrayList;
import java.util.List;

public class OptimizeTestSmellsPostProcessing {

    private List<AbstractTestSmell> listOfTestSmells;

    public OptimizeTestSmellsPostProcessing(){
        initializeTestSmells();
    }

    private void initializeTestSmells(){
        listOfTestSmells = new ArrayList<>();

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
    }

    public int getNumTestSmells(TestSuiteChromosome testSuite){
        int smellCount = 0;
        int specificSmell;

        for (AbstractTestSmell testSmell : listOfTestSmells){
            specificSmell = testSmell.computeNumberOfSmells(testSuite);
            smellCount += specificSmell;
        }

        return smellCount;
    }

    private RuntimeVariable getTestSmellVariable (String smellName){
        switch (smellName){
            case "Assertion Roulette":
                return RuntimeVariable.TestSmellAssertionRoulette;
            case "Brittle Assertion":
                return RuntimeVariable.TestSmellBrittleAssertion;
            case "Duplicate Assert":
                return RuntimeVariable.TestSmellDuplicateAssert;
            case "Eager Test":
                return RuntimeVariable.TestSmellEagerTest;
            case "Empty Test":
                return RuntimeVariable.TestSmellEmptyTest;
            case "Indirect Testing":
                return RuntimeVariable.TestSmellIndirectTesting;
            case "Lack of Cohesion of Methods":
                return RuntimeVariable.TestSmellLackOfCohesionOfMethods;
            case "Lazy Test":
                return RuntimeVariable.TestSmellLazyTest;
            case "Likely Ineffective Object Comparison":
                return RuntimeVariable.TestSmellLikelyIneffectiveObjectComparison;
            case "Mystery Guest":
                return RuntimeVariable.TestSmellMysteryGuest;
            case "Obscure Inline Setup":
                return RuntimeVariable.TestSmellObscureInlineSetup;
            case "Redundant Assertion":
                return RuntimeVariable.TestSmellRedundantAssertion;
            case "Sensitive Equality":
                return RuntimeVariable.TestSmellSensitiveEquality;
            case "Slow Tests":
                return RuntimeVariable.TestSmellSlowTests;
            case "Unknown Test":
                return RuntimeVariable.TestSmellUnknownTest;
            case "Unused Inputs":
                return RuntimeVariable.TestSmellUnusedInputs;
            case "Verbose Test":
                return RuntimeVariable.TestSmellVerboseTest;
            default:
                throw new RuntimeException("Criterion not supported: " + smellName);
        }
    }
}