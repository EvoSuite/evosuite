package org.evosuite.testcase.secondaryobjectives;

import org.evosuite.ga.SecondaryObjective;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testsmells.AbstractTestCaseSmell;

public class OptimizeTestSmellsSecondaryObjective extends SecondaryObjective<TestChromosome> {

    private static final long serialVersionUID = 2879934033646173805L;

    private final AbstractTestCaseSmell testSmell;

    public OptimizeTestSmellsSecondaryObjective(AbstractTestCaseSmell testSmell){
        this.testSmell = testSmell;
    }

    @Override
    public int compareChromosomes(TestChromosome chromosome1, TestChromosome chromosome2) {
        return (int) Math.signum(chromosome1.calculateSmellValuesTestCase(testSmell)
                - chromosome2.calculateSmellValuesTestCase(testSmell));
    }

    @Override
    public int compareGenerations(TestChromosome parent1, TestChromosome parent2, TestChromosome child1, TestChromosome child2) {
        return (int) Math.signum(Math.min(parent1.calculateSmellValuesTestCase(testSmell), parent2.calculateSmellValuesTestCase(testSmell))
                - Math.min(child1.calculateSmellValuesTestCase(testSmell), child2.calculateSmellValuesTestCase(testSmell)));
    }
}
