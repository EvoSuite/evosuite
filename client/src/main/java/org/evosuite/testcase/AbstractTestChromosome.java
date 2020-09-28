package org.evosuite.testcase;

import org.evosuite.ga.ConstructionFailedException;
import org.evosuite.testcase.execution.ExecutionResult;
import org.evosuite.testsuite.TestSuiteFitnessFunction;

public abstract class AbstractTestChromosome<E extends AbstractTestChromosome<E>> extends ExecutableChromosome<E> {


    private static final long serialVersionUID = 8274081309132365034L;
    /**
     * The test case encoded in this chromosome
     */
    protected TestCase test = new DefaultTestCase();

    @Override
    public abstract void crossOver(E other, int position1, int position2) throws ConstructionFailedException;

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
            TestSuiteFitnessFunction testSuiteFitnessFunction);
}

