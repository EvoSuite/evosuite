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
                return RuntimeVariable.AssertionRoulette;
            case "Brittle Assertion":
                return RuntimeVariable.BrittleAssertion;
            case "Duplicate Assert":
                return RuntimeVariable.DuplicateAssert;
            case "Eager Test":
                return RuntimeVariable.EagerTest;
            case "Empty Test":
                return RuntimeVariable.EmptyTest;
            case "Indirect Testing":
                return RuntimeVariable.IndirectTesting;
            case "Lack of Cohesion of Methods":
                return RuntimeVariable.LackOfCohesionOfMethods;
            case "Lazy Test":
                return RuntimeVariable.LazyTest;
            case "Likely Ineffective Object Comparison":
                return RuntimeVariable.LikelyIneffectiveObjectComparison;
            case "Mystery Guest":
                return RuntimeVariable.MysteryGuest;
            case "Obscure Inline Setup":
                return RuntimeVariable.ObscureInlineSetup;
            case "Redundant Assertion":
                return RuntimeVariable.RedundantAssertion;
            case "Sensitive Equality":
                return RuntimeVariable.SensitiveEquality;
            case "Slow Tests":
                return RuntimeVariable.SlowTests;
            case "Unknown Test":
                return RuntimeVariable.UnknownTest;
            case "Unused Inputs":
                return RuntimeVariable.UnusedInputs;
            case "Verbose Test":
                return RuntimeVariable.VerboseTest;
            default:
                throw new RuntimeException("Criterion not supported: " + smellName);
        }
    }
}