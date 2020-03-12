package org.evosuite.coverage;

import org.evosuite.Properties;
import org.evosuite.testcase.TestFitnessFunction;
import org.evosuite.testsuite.TestSuiteFitnessFunction;
import org.evosuite.utils.LoggingUtils;

import java.util.ArrayList;
import java.util.List;

public class FitnessFunctionsUtils {

    /**
     * Convert criterion names to test suite fitness functions
     *
     * @param criterion
	 * @return
	 */
    public static List<TestSuiteFitnessFunction> getFitnessFunctions(Properties.Criterion[] criterion) {
        List<TestSuiteFitnessFunction> ffs = new ArrayList<TestSuiteFitnessFunction>();
        for (int i = 0; i < criterion.length; i++) {
            TestSuiteFitnessFunction newFunction = FitnessFunctions.getFitnessFunction(criterion[i]);

            // If this is compositional fitness, we need to make sure
            // that all functions are consistently minimization or
            // maximization functions
            if (Properties.ALGORITHM != Properties.Algorithm.NSGAII && Properties.ALGORITHM != Properties.Algorithm.SPEA2) {
                for (TestSuiteFitnessFunction oldFunction : ffs) {
                    if (oldFunction.isMaximizationFunction() != newFunction.isMaximizationFunction()) {
                        StringBuffer sb = new StringBuffer();
                        sb.append("* Invalid combination of fitness functions: ");
                        sb.append(oldFunction.toString());
                        if (oldFunction.isMaximizationFunction())
                            sb.append(" is a maximization function ");
                        else
                            sb.append(" is a minimization function ");
                        sb.append(" but ");
                        sb.append(newFunction.toString());
                        if (newFunction.isMaximizationFunction())
                            sb.append(" is a maximization function ");
                        else
                            sb.append(" is a minimization function ");
                        LoggingUtils.getEvoLogger().info(sb.toString());
                        throw new RuntimeException("Invalid combination of fitness functions");
                    }
                }
            }
            ffs.add(newFunction);

        }

        return ffs;
    }

    /**
	 * Convert criterion names to factories for test case fitness functions
	 * @return
	 */
	public static List<TestFitnessFactory<? extends TestFitnessFunction>> getFitnessFactories(Properties.Criterion[] criterion) {
	    List<TestFitnessFactory<? extends TestFitnessFunction>> goalsFactory = new ArrayList<TestFitnessFactory<? extends TestFitnessFunction>>();
	    for (int i = 0; i < criterion.length; i++) {
	        goalsFactory.add(FitnessFunctions.getFitnessFactory(criterion[i]));
	    }

		return goalsFactory;
	}
}