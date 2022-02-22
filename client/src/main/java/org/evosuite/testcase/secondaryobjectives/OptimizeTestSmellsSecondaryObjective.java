package org.evosuite.testcase.secondaryobjectives;

import org.evosuite.ga.SecondaryObjective;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testsmells.AbstractTestCaseSmell;
import org.evosuite.testsmells.smells.*;

import java.util.ArrayList;
import java.util.List;

public class OptimizeTestSmellsSecondaryObjective extends SecondaryObjective<TestChromosome> {

    private List<AbstractTestCaseSmell> listOfTestSmells;

    public OptimizeTestSmellsSecondaryObjective(){
        initializeTestSmells();
    }

    @Override
    public int compareChromosomes(TestChromosome chromosome1, TestChromosome chromosome2) {
        return getNumTestSmells(chromosome1) - getNumTestSmells(chromosome2);
    }

    @Override
    public int compareGenerations(TestChromosome parent1, TestChromosome parent2, TestChromosome child1, TestChromosome child2) {
        return Math.min(getNumTestSmells(parent1), getNumTestSmells(parent2))
                - Math.min(getNumTestSmells(child1), getNumTestSmells(child2));
    }

    private void initializeTestSmells(){
        listOfTestSmells = new ArrayList<>();

        listOfTestSmells.add(new EagerTest());
        listOfTestSmells.add(new VerboseTest());
        listOfTestSmells.add(new IndirectTesting());
        listOfTestSmells.add(new ObscureInlineSetup());
        listOfTestSmells.add(new EmptyTest());
        listOfTestSmells.add(new SlowTests());
        listOfTestSmells.add(new MysteryGuest());
        listOfTestSmells.add(new LikelyIneffectiveObjectComparison());
        listOfTestSmells.add(new Overreferencing());
        listOfTestSmells.add(new ResourceOptimism());
    }

    private int getNumTestSmells(TestChromosome chromosome){
        int smellCount = 0;

        for (AbstractTestCaseSmell testSmell : listOfTestSmells){
            smellCount += testSmell.computeNumberOfSmells(chromosome);
        }

        return smellCount;
    }
}
