package org.evosuite.testsmells.smells;

import org.evosuite.ga.FitnessFunction;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testsmells.AbstractTestCaseSmell;

/**
 * Detection:
 * 1 - Return the number of statements in the chromosome (i.e., the size)
 */
public class VerboseTest extends AbstractTestCaseSmell {

    public VerboseTest() {
        super("TestSmellVerboseTest");
    }

    @Override
    public double computeNumberOfSmells(TestChromosome chromosome) {
        return FitnessFunction.normalize(chromosome.size());
    }
}
