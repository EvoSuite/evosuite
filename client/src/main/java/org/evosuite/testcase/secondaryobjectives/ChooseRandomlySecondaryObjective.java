package org.evosuite.testcase.secondaryobjectives;

import org.evosuite.ga.SecondaryObjective;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.utils.Randomness;

public class ChooseRandomlySecondaryObjective extends SecondaryObjective<TestChromosome> {

    private static final long serialVersionUID = -8543152934847182447L;

    /**
     * {@inheritDoc}
     */
    @Override
    public double compareChromosomes(TestChromosome chromosome1, TestChromosome chromosome2) {
        return Randomness.nextBoolean() ? -1 : 1;
    }

    @Override
    public double compareGenerations(TestChromosome parent1, TestChromosome parent2, TestChromosome child1, TestChromosome child2) {
        return Randomness.nextBoolean() ? -1 : 1;
    }
}
