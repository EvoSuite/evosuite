/**
 * 
 */
package org.evosuite.coverage;

import java.util.Arrays;
import java.util.List;

import org.evosuite.Properties;
import org.evosuite.Properties.Criterion;
import org.evosuite.TestGenerationContext;
import org.evosuite.TestSuiteGenerator;
import org.evosuite.runtime.sandbox.Sandbox;
import org.evosuite.testcase.TestFitnessFunction;
import org.evosuite.utils.LoggingUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Gordon Fraser
 * 
 */
public class ClassStatisticsPrinter {

	private static final Logger logger = LoggerFactory.getLogger(ClassStatisticsPrinter.class);

	private static void reinstrument(Properties.Criterion criterion) {
		Properties.CRITERION = new Properties.Criterion[1];
		Properties.CRITERION[0] = criterion;

		logger.info("Re-instrumenting for criterion: " + criterion);
		TestGenerationContext.getInstance().resetContext();
		// Need to load class explicitly in case there are no test cases.
		// If there are tests, then this is redundant
		Properties.getTargetClass(false);
	}

	/**
	 * Identify all JUnit tests starting with the given name prefix, instrument
	 * and run tests
	 */
	public static void printClassStatistics() {
		Sandbox.goingToExecuteSUTCode();
		TestGenerationContext.getInstance().goingToExecuteSUTCode();
		Sandbox.goingToExecuteUnsafeCodeOnSameThread();
		try {
			// Load SUT without initialising it
			Class<?> targetClass = Properties.getTargetClass(false);
			if(targetClass != null) {
				//DependencyAnalysis.analyze(Properties.TARGET_CLASS,
				//		Arrays.asList(ClassPathHandler.getInstance().getClassPathElementsForTargetProject()));
				LoggingUtils.getEvoLogger().info("* Finished analyzing classpath");
			} else {
				LoggingUtils.getEvoLogger().info("* Error while initializing target class, not continuing");
			}
		} catch (Throwable e) {
			LoggingUtils.getEvoLogger().error("* Error while initializing target class: "
			                                          + (e.getMessage() != null ? e.getMessage()
			                                                  : e.toString()));
			return;
		} finally {
			Sandbox.doneWithExecutingUnsafeCodeOnSameThread();
			Sandbox.doneWithExecutingSUTCode();
			TestGenerationContext.getInstance().doneWithExecuteingSUTCode();
		}

		Properties.Criterion oldCriterion[] = Arrays.copyOf(Properties.CRITERION, Properties.CRITERION.length);
		for (Criterion criterion : oldCriterion) {
			reinstrument(criterion);

			List<TestFitnessFactory<?>> factories = TestSuiteGenerator.getFitnessFactory();

			int numGoals = 0;
			for (TestFitnessFactory<?> factory : factories) {
				if (Properties.PRINT_GOALS) {
					for (TestFitnessFunction goal : factory.getCoverageGoals())
						LoggingUtils.getEvoLogger().info("" + goal.toString());
				}
				numGoals += factory.getCoverageGoals().size();
			}

			LoggingUtils.getEvoLogger().info("* Criterion " + criterion + ": " + numGoals);
		}
	}
}
