package org.evosuite.testcase.secondaryobjectives;

import org.evosuite.ga.SecondaryObjective;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testsmells.AbstractTestCaseSmell;
import org.evosuite.testsmells.smells.*;

import java.util.ArrayList;
import java.util.List;

public class OptimizeTestSmellsSecondaryObjective extends SecondaryObjective<TestChromosome> {

    private static final long serialVersionUID = 2879934033646173805L;
    private List<AbstractTestCaseSmell> listOfTestSmells;

    public OptimizeTestSmellsSecondaryObjective(){
        initializeTestSmells();
    }

    public OptimizeTestSmellsSecondaryObjective(String smellName){
        listOfTestSmells = new ArrayList<>();

        switch (smellName) {
            case "TEST_SMELL_EAGER_TEST":
                listOfTestSmells.add(new EagerTest());
                return;
            case "TEST_SMELL_EMPTY_TEST":
                listOfTestSmells.add(new EmptyTest());
                return;
            case "TEST_SMELL_INDIRECT_TESTING":
                listOfTestSmells.add(new IndirectTesting());
                return;
            case "TEST_SMELL_LIKELY_INEFFECTIVE_OBJECT_COMPARISON":
                listOfTestSmells.add(new LikelyIneffectiveObjectComparison());
                return;
            case "TEST_SMELL_MYSTERY_GUEST":
                listOfTestSmells.add(new MysteryGuest());
                return;
            case "TEST_SMELL_OBSCURE_INLINE_SETUP":
                listOfTestSmells.add(new ObscureInlineSetup());
                return;
            case "TEST_SMELL_OVERREFERENCING":
                listOfTestSmells.add(new Overreferencing());
                return;
            case "TEST_SMELL_RESOURCE_OPTIMISM":
                listOfTestSmells.add(new ResourceOptimism());
                return;
            case "TEST_SMELL_ROTTEN_GREEN_TESTS":
                listOfTestSmells.add(new RottenGreenTests());
                return;
            case "TEST_SMELL_SLOW_TESTS":
                listOfTestSmells.add(new SlowTests());
                return;
            case "TEST_SMELL_VERBOSE_TEST":
                listOfTestSmells.add(new VerboseTest());
                return;
            default:
                throw new RuntimeException("ERROR: asked for unknown secondary objective \""
                        + smellName + "\"");
        }
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
        listOfTestSmells.add(new VerboseTest());
    }
}
