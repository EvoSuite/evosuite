package org.evosuite.testsmells.smells;

import org.evosuite.testcase.TestChromosome;
import org.evosuite.testsmells.AbstractTestCaseSmell;

/**
 * Definition:
 * Unnecessarily long tests.
 *
 * Metric:
 * Count the total number of statements in a test case.
 *
 * Detection:
 * 1 - Return the number of statements in the chromosome (i.e., the size)
 */
public class VerboseTest extends AbstractTestCaseSmell {

    public VerboseTest() {
        super("TestSmellVerboseTest");
    }

    @Override
    public double computeNumberOfTestSmells(TestChromosome chromosome) {
        return chromosome.size();
    }
}
