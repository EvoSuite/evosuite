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
        return (int) Math.signum(chromosome1.calculateSmellValuesTestCase(listOfTestSmells)
                - chromosome2.calculateSmellValuesTestCase(listOfTestSmells));
    }

    @Override
    public int compareGenerations(TestChromosome parent1, TestChromosome parent2, TestChromosome child1, TestChromosome child2) {
        return (int) Math.signum(Math.min(parent1.calculateSmellValuesTestCase(listOfTestSmells), parent2.calculateSmellValuesTestCase(listOfTestSmells))
                - Math.min(child1.calculateSmellValuesTestCase(listOfTestSmells), child2.calculateSmellValuesTestCase(listOfTestSmells)));
    }

    private void initializeTestSmells(){
        listOfTestSmells = new ArrayList<>();

        listOfTestSmells.add(new EagerTest());
        listOfTestSmells.add(new EmptyTest());
        listOfTestSmells.add(new IndirectTesting());
        listOfTestSmells.add(new LikelyIneffectiveObjectComparison());
        listOfTestSmells.add(new MysteryGuest());
        listOfTestSmells.add(new ObscureInlineSetup());
        listOfTestSmells.add(new Overreferencing());
        listOfTestSmells.add(new ResourceOptimism());
        listOfTestSmells.add(new RottenGreenTests());
        listOfTestSmells.add(new SlowTests());
        listOfTestSmells.add(new TestCodeDuplication());
        listOfTestSmells.add(new VerboseTest());
    }
}
