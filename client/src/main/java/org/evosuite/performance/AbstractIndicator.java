package org.evosuite.performance;

import org.evosuite.ga.Chromosome;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.execution.ExecutionResult;

/**
 * @author annibale.panichella
 *
 * This interface models the performance indicators.
 **/

public abstract class AbstractIndicator<T extends Chromosome> {

    public abstract double getIndicatorValue(T test);

    public abstract String getIndicatorId();

    /**
     * Standard function to normalize values
     * @param x     the value to normalize
     * @return      the normalized value
     */
    public static double normalize(double x){
        return x / (x + 1.0);
    }

    /**
     * For the initial population we do not have the execution results available and therefore we are not
     * able to compute the performance indicators.
     * For such a reason we have to set a dummy value
     *
     * @param results       the execution result to check
     * @param indicator     the indicator to compute
     * @param test          the individual
     * @return              true is the <code>ExecutionResult</code> is null; otherwise, false
     */
    public boolean isInitialIndividual(ExecutionResult results, AbstractIndicator indicator,
                                       TestChromosome test) {
        if (results == null) {
            test.setIndicatorValues(indicator.getIndicatorId(), 1.0);
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName();
    }
}
