package org.evosuite.testsmells.smells;

import org.evosuite.testcase.TestChromosome;
import org.evosuite.testsmells.AbstractNormalizedTestCaseSmell;

/**
 * Definition:
 * Unnecessarily long tests.
 *
 * Metric:
 * Total number of statements in a test case.
 *
 * Computation:
 * 1 - Return the number of statements in the chromosome (i.e., the size)
 */
public class VerboseTest extends AbstractNormalizedTestCaseSmell {

    private static final long serialVersionUID = -1030200312174060155L;

    public VerboseTest() {
        super("TestSmellVerboseTest");
    }

    @Override
    public long computeNumberOfTestSmells(TestChromosome chromosome) {
        return chromosome.size();
    }
}
