/**
 * 
 */
package org.evosuite.coverage;

import java.io.File;
import java.util.Arrays;

import org.evosuite.Properties;
import org.evosuite.TestGenerationContext;
import org.evosuite.TestSuiteGenerator;
import org.evosuite.sandbox.Sandbox;
import org.evosuite.setup.DependencyAnalysis;
import org.evosuite.utils.LoggingUtils;

/**
 * @author Gordon Fraser
 * 
 */
public class ClassStatisticsPrinter {

	private static void reinstrument(Properties.Criterion criterion) {
		Properties.Criterion oldCriterion = Properties.CRITERION;
		if (oldCriterion == criterion)
			return;

		Properties.CRITERION = criterion;
		TestGenerationContext.getInstance().resetContext();
		// Need to load class explicitly in case there are no test cases.
		// If there are tests, then this is redundant
		Properties.getTargetClass();
	}

	private final static Properties.Criterion[] criteria = { Properties.Criterion.BRANCH,
	        Properties.Criterion.DEFUSE, Properties.Criterion.WEAKMUTATION,
	        Properties.Criterion.STATEMENT };

	/**
	 * Identify all JUnit tests starting with the given name prefix, instrument
	 * and run tests
	 */
	public static void printClassStatistics() {
		Sandbox.goingToExecuteSUTCode();
		Sandbox.goingToExecuteUnsafeCodeOnSameThread();
		try {
			DependencyAnalysis.analyze(Properties.TARGET_CLASS,
			                           Arrays.asList(Properties.CP.split(File.pathSeparator)));
			LoggingUtils.getEvoLogger().info("* Finished analyzing classpath");
		} catch (Throwable e) {
			LoggingUtils.getEvoLogger().error("* Error while initializing target class: "
			                                          + (e.getMessage() != null ? e.getMessage()
			                                                  : e.toString()));
			return;
		} finally {
			Sandbox.doneWithExecutingUnsafeCodeOnSameThread();
			Sandbox.doneWithExecutingSUTCode();
		}
		for (Properties.Criterion criterion : criteria) {
			reinstrument(criterion);
			TestFitnessFactory<?> factory = TestSuiteGenerator.getFitnessFactory();
			int numGoals = factory.getCoverageGoals().size();
			LoggingUtils.getEvoLogger().info("* Criterion " + criterion + ": " + numGoals);
		}

	}

}
