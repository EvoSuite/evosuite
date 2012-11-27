/**
 * 
 */
package org.evosuite;

import org.evosuite.coverage.branch.BranchPool;
import org.evosuite.ga.stoppingconditions.GlobalTimeStoppingCondition;
import org.evosuite.ga.stoppingconditions.MaxStatementsStoppingCondition;
import org.evosuite.graphs.GraphPool;
import org.evosuite.graphs.cfg.BytecodeInstructionPool;
import org.evosuite.graphs.cfg.CFGMethodAdapter;
import org.evosuite.javaagent.InstrumentingClassLoader;
import org.evosuite.primitives.ConstantPoolManager;
import org.evosuite.setup.TestCluster;
import org.evosuite.testcase.ExecutionTracer;
import org.evosuite.testcase.TestCaseExecutor;
import org.evosuite.utils.LoggingUtils;

/**
 * @author Gordon Fraser
 * 
 */
public class TestGenerationContext {

	private static TestGenerationContext instance = new TestGenerationContext();

	private TestGenerationContext() {

	}

	public static TestGenerationContext getInstance() {
		return instance;
	}

	/**
	 * This is the classloader that does the instrumentation - it needs to be
	 * used by all test code
	 */
	private static ClassLoader classLoader = new InstrumentingClassLoader();

	public static ClassLoader getClassLoader() {
		return classLoader;
	}

	public void resetContext() {

		LoggingUtils.getEvoLogger().info("*** Resetting context");

		TestCaseExecutor.pullDown();

		ExecutionTracer.getExecutionTracer().clear();

		// TODO: BranchPool should not be static
		BranchPool.reset();

		// TODO: Clear only pool of current classloader?
		GraphPool.clearAll();

		// TODO: This is not nice
		CFGMethodAdapter.methods.clear();

		// TODO: Clear only pool of current classloader?
		BytecodeInstructionPool.clearAll();

		// TODO: After this, the test cluster is empty until DependencyAnalysis.analyse is called
		TestCluster.reset();

		// This counts the current level of recursion during test generation
		org.evosuite.testcase.TestFactory.getInstance().reset();

		MaxStatementsStoppingCondition.setNumExecutedStatements(0);
		GlobalTimeStoppingCondition.forceReset();

		// A fresh context needs a fresh class loader to make sure we can re-instrument classes
		classLoader = new InstrumentingClassLoader();

		// Forget the old SUT
		Properties.resetTargetClass();

		TestCaseExecutor.initExecutor();
		
		// Constant pool
		ConstantPoolManager.getInstance().reset();
	}

}
