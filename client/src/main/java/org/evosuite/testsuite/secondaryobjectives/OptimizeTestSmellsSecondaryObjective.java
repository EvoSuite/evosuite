package org.evosuite.testsuite.secondaryobjectives;

import org.evosuite.ga.SecondaryObjective;
import org.evosuite.testsmells.AbstractTestSmell;
import org.evosuite.testsmells.smells.*;
import org.evosuite.testsuite.TestSuiteChromosome;

import java.util.ArrayList;
import java.util.List;

public class OptimizeTestSmellsSecondaryObjective extends SecondaryObjective<TestSuiteChromosome> {

    private List<AbstractTestSmell> listOfTestSmells;

    OptimizeTestSmellsSecondaryObjective() {
        initializeTestSmells();
    }

    @Override
    public int compareChromosomes(TestSuiteChromosome chromosome1, TestSuiteChromosome chromosome2) {
        return getNumTestSmells(chromosome1) - getNumTestSmells(chromosome2);
    }

    @Override
    public int compareGenerations(TestSuiteChromosome parent1, TestSuiteChromosome parent2, TestSuiteChromosome child1, TestSuiteChromosome child2) {
        return Math.min(getNumTestSmells(parent1), getNumTestSmells(parent2))
                - Math.min(getNumTestSmells(child1), getNumTestSmells(child2));
    }

    private void initializeTestSmells() {
        listOfTestSmells = new ArrayList<>();

        //Test smells related to test cases
        listOfTestSmells.add(new EagerTest());
        listOfTestSmells.add(new VerboseTest());
        listOfTestSmells.add(new IndirectTesting());
        listOfTestSmells.add(new ObscureInlineSetup());
        listOfTestSmells.add(new EmptyTest());

        //Test smells related to test suites
        listOfTestSmells.add(new LackOfCohesionOfMethods());

    }

    private int getNumTestSmells(TestSuiteChromosome chromosome){
        int smellCount = 0;

        for (AbstractTestSmell testSmell : listOfTestSmells){
            smellCount += testSmell.computeTestSmellMetric(chromosome);
        }

        return smellCount;
    }
}
