package org.evosuite.testcase.secondaryobjectives;

import org.evosuite.ga.SecondaryObjective;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testsmells.AbstractTestCaseSmell;
import org.evosuite.testsmells.smells.*;

public class OptimizeIndividualTestSmellSecondaryObjective extends SecondaryObjective<TestChromosome> {

    private static final long serialVersionUID = -6840830085846174507L;

    private final AbstractTestCaseSmell testSmell;
    private final String secondaryObjectiveName;

    public OptimizeIndividualTestSmellSecondaryObjective(String smellName){
        secondaryObjectiveName = smellName;

        switch (smellName) {
            case "TEST_SMELL_EAGER_TEST":
                testSmell = new EagerTest();
                return;
            case "TEST_SMELL_EMPTY_TEST":
                testSmell = new EmptyTest();
                return;
            case "TEST_SMELL_INDIRECT_TESTING":
                testSmell = new IndirectTesting();
                return;
            case "TEST_SMELL_LIKELY_INEFFECTIVE_OBJECT_COMPARISON":
                testSmell = new LikelyIneffectiveObjectComparison();
                return;
            case "TEST_SMELL_MYSTERY_GUEST":
                testSmell = new MysteryGuest();
                return;
            case "TEST_SMELL_OBSCURE_INLINE_SETUP":
                testSmell = new ObscureInlineSetup();
                return;
            case "TEST_SMELL_OVERREFERENCING":
                testSmell = new Overreferencing();
                return;
            case "TEST_SMELL_RESOURCE_OPTIMISM":
                testSmell = new ResourceOptimism();
                return;
            case "TEST_SMELL_ROTTEN_GREEN_TESTS":
                testSmell = new RottenGreenTests();
                return;
            case "TEST_SMELL_SLOW_TESTS":
                testSmell = new SlowTests();
                return;
            case "TEST_SMELL_VERBOSE_TEST":
                testSmell = new VerboseTest();
                return;
            default:
                throw new RuntimeException("ERROR: asked for unknown secondary objective \""
                        + smellName + "\"");
        }
    }

    @Override
    public int compareChromosomes(TestChromosome chromosome1, TestChromosome chromosome2) {
        return (int) Math.signum(chromosome1.calculateSmellValuesTestCase(secondaryObjectiveName, testSmell)
                - chromosome2.calculateSmellValuesTestCase(secondaryObjectiveName, testSmell));
    }

    @Override
    public int compareGenerations(TestChromosome parent1, TestChromosome parent2, TestChromosome child1, TestChromosome child2) {
        return (int) Math.signum(Math.min(parent1.calculateSmellValuesTestCase(secondaryObjectiveName, testSmell), parent2.calculateSmellValuesTestCase(secondaryObjectiveName, testSmell))
                - Math.min(child1.calculateSmellValuesTestCase(secondaryObjectiveName, testSmell), child2.calculateSmellValuesTestCase(secondaryObjectiveName, testSmell)));
    }

    public String getObjectiveName(){
        return this.secondaryObjectiveName;
    }
}
