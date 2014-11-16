package org.evosuite.coverage.readability;

import org.evosuite.testcase.ExecutableChromosome;
import org.evosuite.testsuite.AbstractTestSuiteChromosome;
import org.evosuite.testsuite.TestSuiteFitnessFunction;

public class ReadabilitySuiteFitness extends TestSuiteFitnessFunction {

    /**
     * 
     */
    private static final long serialVersionUID = 6243235746473531638L;

    /**
     * 
     */
    @Override
    public double getFitness(AbstractTestSuiteChromosome<? extends ExecutableChromosome> suite)
    {
        double average = 0.0;

        for (ExecutableChromosome ec : suite.getTestChromosomes()) {
            average += getScore(ec.toString());
        }

        average /= suite.getTestChromosomes().size();

        updateIndividual(this, suite, average);
        return average;
    }

    /**
     * 
     */
    public double getScore(String test)
    {
        // TODO
        return 0.0;
    }

    /**
     * 
     */
    @Override
    public boolean isMaximizationFunction() {
        return false;
    }
}
