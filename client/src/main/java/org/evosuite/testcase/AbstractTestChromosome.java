package org.evosuite.testcase;

import org.evosuite.ga.ConstructionFailedException;
import org.evosuite.ga.SecondaryObjective;
import org.evosuite.testcase.execution.ExecutionResult;
import org.evosuite.testsuite.TestSuiteFitnessFunction;

import java.util.List;

public abstract class AbstractTestChromosome<E extends AbstractTestChromosome<E>> extends ExecutableChromosome<E> {


    /**
     * The test case encoded in this chromosome
     */
    protected TestCase test = new DefaultTestCase();

    @Override
    public abstract void crossOver(E other, int position1, int position2) throws ConstructionFailedException;

    /**
     * Returns the static list of secondary objectives.
     *
     * @return
     */
    public abstract List<SecondaryObjective<E>> getSecondaryObjectives_();

    /**
     * <p>
     * setTestCase
     * </p>
     *
     * @param testCase a {@link org.evosuite.testcase.TestCase} object.
     */
    public void setTestCase(TestCase testCase) {
        test = testCase;
        clearCachedResults();
        clearCachedMutationResults();
        setChanged(true);
    }

    /**
     * <p>
     * getTestCase
     * </p>
     *
     * @return a {@link org.evosuite.testcase.TestCase} object.
     */
    public TestCase getTestCase() {
        return test;
    }

    public abstract ExecutionResult executeForFitnessFunction(
            TestSuiteFitnessFunction<?, ?, E> testSuiteFitnessFunction);
}

