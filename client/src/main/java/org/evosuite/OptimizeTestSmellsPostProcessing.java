package org.evosuite;

import org.evosuite.rmi.ClientServices;
import org.evosuite.statistics.RuntimeVariable;
import org.evosuite.testsmells.AbstractTestSmell;
import org.evosuite.testsmells.AbstractTestSuiteSmell;
import org.evosuite.testsmells.smells.*;
import org.evosuite.testsuite.TestSuiteChromosome;

import java.util.ArrayList;
import java.util.List;

public class OptimizeTestSmellsPostProcessing {

    private List<AbstractTestSmell> listOfTestSmells;
    private List<AbstractTestSuiteSmell> listOfAbstractTestSmells;

    public OptimizeTestSmellsPostProcessing(){ initializeTestSmells(); }

    private void initializeTestSmells(){
        listOfTestSmells = new ArrayList<>();
        listOfTestSmells.add(new AssertionRoulette("Assertion Roulette"));
        listOfTestSmells.add(new BrittleAssertion("Brittle Assertion"));
        listOfTestSmells.add(new DuplicateAssert("Duplicate Assert"));
        listOfTestSmells.add(new EagerTest("Eager Test"));
        listOfTestSmells.add(new EmptyTest("Empty Test"));
        listOfTestSmells.add(new IndirectTesting("Indirect Testing"));
        listOfTestSmells.add(new LikelyIneffectiveObjectComparison("Likely Ineffective Object Comparison"));
        listOfTestSmells.add(new MysteryGuest("Mystery Guest"));
        listOfTestSmells.add(new ObscureInlineSetup("Obscure Inline Setup"));
        listOfTestSmells.add(new RedundantAssertion("Redundant Assertion"));
        listOfTestSmells.add(new SensitiveEquality("Sensitive Equality"));
        listOfTestSmells.add(new SlowTests("Slow Tests"));
        listOfTestSmells.add(new UnknownTest("Unknown Test"));
        listOfTestSmells.add(new UnusedInputs("Unused Inputs"));
        listOfTestSmells.add(new VerboseTest("Verbose Test"));

        listOfAbstractTestSmells = new ArrayList<>();
        listOfAbstractTestSmells.add(new LackOfCohesionOfMethods("Lack of Cohesion of Methods"));
        listOfAbstractTestSmells.add(new LazyTest("Lazy Test"));
    }

    public int getNumTestSmells(TestSuiteChromosome testSuite){
        int smellCount = 0;
        int specificSmell;

        for (AbstractTestSuiteSmell testSmell : listOfAbstractTestSmells){
            specificSmell = testSmell.obtainSmellCount(testSuite);
            ClientServices.getInstance().getClientNode().trackOutputVariable(getTestSmellVariable(testSmell.getSmellName()), specificSmell);
            smellCount += specificSmell;
        }

        for (AbstractTestSmell testSmell : listOfTestSmells){
            specificSmell = testSmell.obtainSmellCount(testSuite);
            ClientServices.getInstance().getClientNode().trackOutputVariable(getTestSmellVariable(testSmell.getSmellName()), specificSmell);
            smellCount += specificSmell;
        }

        ClientServices.getInstance().getClientNode().trackOutputVariable(RuntimeVariable.AllTestSmells, smellCount);

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