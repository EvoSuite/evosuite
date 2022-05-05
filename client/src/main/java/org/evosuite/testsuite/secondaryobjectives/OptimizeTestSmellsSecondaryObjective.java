package org.evosuite.testsuite.secondaryobjectives;

import org.evosuite.ga.SecondaryObjective;
import org.evosuite.testsmells.AbstractTestSmell;
import org.evosuite.testsuite.TestSuiteChromosome;

public class OptimizeTestSmellsSecondaryObjective extends SecondaryObjective<TestSuiteChromosome> {

    private static final long serialVersionUID = -7001972100989940342L;

    private AbstractTestSmell testSmell;

    public OptimizeTestSmellsSecondaryObjective(AbstractTestSmell testSmell) {
        this.testSmell = testSmell;
    }

    @Override
    public int compareChromosomes(TestSuiteChromosome chromosome1, TestSuiteChromosome chromosome2) {
        return (int) Math.signum(chromosome1.calculateSmellValuesTestSuite(testSmell)
                - chromosome2.calculateSmellValuesTestSuite(testSmell));
    }

    @Override
    public int compareGenerations(TestSuiteChromosome parent1, TestSuiteChromosome parent2, TestSuiteChromosome child1, TestSuiteChromosome child2) {
        return (int) Math.signum(Math.min(parent1.calculateSmellValuesTestSuite(testSmell), parent2.calculateSmellValuesTestSuite(testSmell))
                - Math.min(child1.calculateSmellValuesTestSuite(testSmell), child2.calculateSmellValuesTestSuite(testSmell)));
    }
}
