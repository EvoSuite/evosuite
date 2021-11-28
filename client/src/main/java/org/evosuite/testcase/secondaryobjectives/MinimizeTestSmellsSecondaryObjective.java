package org.evosuite.testcase.secondaryobjectives;

import org.evosuite.ga.SecondaryObjective;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testsmells.AbstractTestSmell;
import org.evosuite.testsmells.smells.EagerTest;
import org.evosuite.testsmells.smells.IndirectTesting;
import org.evosuite.testsmells.smells.VerboseTest;

import java.util.ArrayList;
import java.util.List;

public class MinimizeTestSmellsSecondaryObjective extends SecondaryObjective<TestChromosome> {

    private List<AbstractTestSmell> listOfTestSmells;

    public MinimizeTestSmellsSecondaryObjective (){
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
        listOfTestSmells.add(new EagerTest("Eager Test"));
        listOfTestSmells.add(new VerboseTest("Verbose Test"));
        listOfTestSmells.add(new IndirectTesting("Indirect Testing"));
    }

    private int getNumTestSmells(TestChromosome chromosome){
        int smellCount = 0;
        for (AbstractTestSmell testSmell : listOfTestSmells){
            smellCount += testSmell.obtainSmellCount(chromosome);
        }
        return smellCount;
    }

    // Obscure In-line Setup

    // Slow tests

    // Test Code Duplication
}
