package org.evosuite;

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
        listOfTestSmells.add(new DuplicateAssert());
        listOfTestSmells.add(new EagerTest());
        listOfTestSmells.add(new IndirectTesting());
        listOfTestSmells.add(new LackOfCohesionOfMethods());
        listOfTestSmells.add(new LazyTest());
        listOfTestSmells.add(new LikelyIneffectiveObjectComparison());
        listOfTestSmells.add(new ObscureInlineSetup());
        listOfTestSmells.add(new Overreferencing());
        listOfTestSmells.add(new RedundantAssertion());
        listOfTestSmells.add(new RottenGreenTests());
        listOfTestSmells.add(new TestRedundancy());
        listOfTestSmells.add(new UnknownTest());
        listOfTestSmells.add(new UnrelatedAssertions());
        listOfTestSmells.add(new UnusedInputs());
        listOfTestSmells.add(new VerboseTest());
    }

    public double computeTotalNumberOfSmells(TestSuiteChromosome testSuite){
        double smellCount = testSuite.calculateSmellValuesTestSuiteUnrestricted(listOfTestSmells);

        //Implement optimization

        return smellCount;
    }
}