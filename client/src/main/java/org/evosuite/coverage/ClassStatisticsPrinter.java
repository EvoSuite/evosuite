/**
 * 
 */
package org.evosuite.coverage;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;

import org.evosuite.Properties;
import org.evosuite.Properties.Criterion;
import org.evosuite.TestGenerationContext;
import org.evosuite.classpath.ClassPathHandler;
import org.evosuite.coverage.branch.BranchPool;
import org.evosuite.graphs.GraphPool;
import org.evosuite.graphs.cfg.CFGMethodAdapter;
import org.evosuite.graphs.cfg.RawControlFlowGraph;
import org.evosuite.instrumentation.LinePool;
import org.evosuite.runtime.classhandling.ClassResetter;
import org.evosuite.runtime.sandbox.Sandbox;
import org.evosuite.setup.DependencyAnalysis;
import org.evosuite.setup.TestCluster;
import org.evosuite.strategy.TestGenerationStrategy;
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

	private static boolean reinstrument(Properties.Criterion criterion) {
		Properties.CRITERION = new Properties.Criterion[1];
		Properties.CRITERION[0] = criterion;

		logger.info("Re-instrumenting for criterion: " + criterion);
		TestGenerationContext.getInstance().resetContext();

		try {
			// we have to analyse the dependencies of the TargetClass
			// again, because resetContext() of TestGenerationContext
			// class is just generating a new TestCluster for some
			// specific cases. and if the dependencies are not analysed,
			// could be that for example inner classes are not loaded,
			// and therefore their goals will not be considered
			DependencyAnalysis.analyzeClass(Properties.TARGET_CLASS,
					Arrays.asList(ClassPathHandler.getInstance().getClassPathElementsForTargetProject()));
		} catch (ClassNotFoundException | RuntimeException e) {
			LoggingUtils.getEvoLogger().error("* Error while initializing target class: "
                    + (e.getMessage() != null ? e.getMessage()
                            : e.toString()));
			return false;
		}

		// Need to load class explicitly in case there are no test cases.
		// If there are tests, then this is redundant
		Properties.getTargetClass(false);

		return true;
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
			DependencyAnalysis.analyzeClass(Properties.TARGET_CLASS,
					Arrays.asList(ClassPathHandler.getInstance().getClassPathElementsForTargetProject()));

			// Load SUT without initialising it
			Class<?> targetClass = Properties.getTargetClass(false);
			if(targetClass != null) {
				LoggingUtils.getEvoLogger().info("* Finished analyzing classpath");
			} else {
				LoggingUtils.getEvoLogger().info("* Error while initializing target class, not continuing");
				return;
			}
			int publicMethods = 0;
			int nonpublicMethods = 0;
			int staticMethods = 0;
			int staticFields = 0;
			for(Method method : targetClass.getDeclaredMethods()) {
				if(method.getName().equals(ClassResetter.STATIC_RESET))
					continue;
				if(Modifier.isPublic(method.getModifiers())) {
					publicMethods++;
				} else {
					nonpublicMethods++;
				}
				if(Modifier.isStatic(method.getModifiers())) {
					LoggingUtils.getEvoLogger().info("Static: "+method);
					staticMethods++;
				}

			}
			for(Constructor<?> constructor: targetClass.getDeclaredConstructors()) {
				if(Modifier.isPublic(constructor.getModifiers())) {
					publicMethods++;
				} else {
					nonpublicMethods++;
				}
			}
			for(Field field : targetClass.getDeclaredFields()) {
				if(Modifier.isStatic(field.getModifiers())) {
					staticFields++;
				}
			}
			LoggingUtils.getEvoLogger().info("* Abstract: "+Modifier.isAbstract(targetClass.getModifiers()));
			LoggingUtils.getEvoLogger().info("* Public methods/constructors: "+publicMethods);
			LoggingUtils.getEvoLogger().info("* Non-Public methods/constructors: "+nonpublicMethods);
			LoggingUtils.getEvoLogger().info("* Static methods: "+staticMethods);
			LoggingUtils.getEvoLogger().info("* Inner classes: "+targetClass.getDeclaredClasses().length);
			LoggingUtils.getEvoLogger().info("* Total fields: "+targetClass.getDeclaredFields().length);
			LoggingUtils.getEvoLogger().info("* Static fields: "+staticFields);
			LoggingUtils.getEvoLogger().info("* Type parameters: "+targetClass.getTypeParameters().length);

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

		LoggingUtils.getEvoLogger().info("* Subclasses: "+(TestCluster.getInheritanceTree().getSubclasses(Properties.TARGET_CLASS).size() - 1));
		LoggingUtils.getEvoLogger().info("* Superclasses/interfaces: "+(TestCluster.getInheritanceTree().getSuperclasses(Properties.TARGET_CLASS).size() - 1));
		LoggingUtils.getEvoLogger().info("* Lines of code: "+LinePool.getNumLines());
		LoggingUtils.getEvoLogger().info("* Methods without branches: "+BranchPool.getInstance(TestGenerationContext.getInstance().getClassLoaderForSUT()).getNumBranchlessMethods());
		LoggingUtils.getEvoLogger().info("* Total branch predicates: "+BranchPool.getInstance(TestGenerationContext.getInstance().getClassLoaderForSUT()).getBranchCounter());
		
		
		double complexity = 0.0;
		int maxComplexity = 0;
		for(Entry<String, RawControlFlowGraph> entry : GraphPool.getInstance(TestGenerationContext.getInstance().getClassLoaderForSUT()).getRawCFGs(Properties.TARGET_CLASS).entrySet()) {
			int c = entry.getValue().getCyclomaticComplexity();
			if(c > maxComplexity)
				maxComplexity = c;
			complexity += c;
			// LoggingUtils.getEvoLogger().info("* Complexity of method "+entry.getKey()+": "+entry.getValue().getCyclomaticComplexity());
		}
		LoggingUtils.getEvoLogger().info("* Average cyclomatic complexity: "+(complexity/CFGMethodAdapter.getNumMethods(TestGenerationContext.getInstance().getClassLoaderForSUT())));
		LoggingUtils.getEvoLogger().info("* Maximum cyclomatic complexity: "+maxComplexity);

		Properties.Criterion oldCriterion[] = Arrays.copyOf(Properties.CRITERION, Properties.CRITERION.length);
		for (Criterion criterion : oldCriterion) {
			if (!reinstrument(criterion)) {
				return ;
			}

			List<TestFitnessFactory<?>> factories = TestGenerationStrategy.getFitnessFactories();

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
