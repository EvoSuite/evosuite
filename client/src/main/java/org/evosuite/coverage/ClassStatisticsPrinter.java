/**
 * 
 */
package org.evosuite.coverage;

import java.util.List;

import org.evosuite.Properties;
import org.evosuite.TestGenerationContext;
import org.evosuite.TestSuiteGenerator;
import org.evosuite.runtime.sandbox.Sandbox;
import org.evosuite.utils.ArrayUtil;
import org.evosuite.utils.LoggingUtils;

/**
 * @author Gordon Fraser
 * 
 */
public class ClassStatisticsPrinter {

	private static void reinstrument(Properties.Criterion criterion) {
	    Properties.Criterion[] oldCriterion = Properties.CRITERION;
	    if (ArrayUtil.contains(oldCriterion, criterion))
	        return ;

	    Properties.CRITERION = new Properties.Criterion[1];
        Properties.CRITERION[0] = criterion;

		TestGenerationContext.getInstance().resetContext();
		// Need to load class explicitly in case there are no test cases.
		// If there are tests, then this is redundant
		Properties.getTargetClass(false);
	}

	private final static Properties.Criterion[] criteria = { Properties.Criterion.BRANCH,
	         Properties.Criterion.WEAKMUTATION,
	        Properties.Criterion.STATEMENT };
	// Properties.Criterion.DEFUSE is currently experimental

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
		Properties.Criterion[] backup = Properties.CRITERION;
		for (Properties.Criterion criterion : criteria) {
			reinstrument(criterion);
			List<TestFitnessFactory<?>> factories = TestSuiteGenerator.getFitnessFactory();
			int numGoals = 0;
			for (TestFitnessFactory<?> factory : factories)
			    numGoals += factory.getCoverageGoals().size();
			LoggingUtils.getEvoLogger().info("* Criterion " + criterion + ": " + numGoals);

			Properties.CRITERION = backup;
		}
	}
}
