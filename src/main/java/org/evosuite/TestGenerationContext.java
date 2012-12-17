/**
 * 
 */
package org.evosuite;

import org.evosuite.coverage.branch.BranchPool;
import org.evosuite.coverage.dataflow.DefUsePool;
import org.evosuite.coverage.mutation.MutationPool;
import org.evosuite.ga.stoppingconditions.GlobalTimeStoppingCondition;
import org.evosuite.ga.stoppingconditions.MaxStatementsStoppingCondition;
import org.evosuite.graphs.GraphPool;
import org.evosuite.graphs.cfg.BytecodeInstructionPool;
import org.evosuite.graphs.cfg.CFGMethodAdapter;
import org.evosuite.javaagent.InstrumentingClassLoader;
import org.evosuite.primitives.ConstantPoolManager;
import org.evosuite.setup.DependencyAnalysis;
import org.evosuite.setup.TestCluster;
import org.evosuite.setup.TestClusterGenerator;
import org.evosuite.testcase.ExecutionTracer;
import org.evosuite.testcase.TestCaseExecutor;
import org.evosuite.utils.LoggingUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	
	private static Logger logger = LoggerFactory.getLogger(TestGenerationContext.class);

	public static ClassLoader getClassLoader() {
		return classLoader;
	}

	public void resetContext() {

		logger.info("*** Resetting context");

		// A fresh context needs a fresh class loader to make sure we can re-instrument classes
		classLoader = new InstrumentingClassLoader();


		TestCaseExecutor.pullDown();

		ExecutionTracer.getExecutionTracer().clear();

		// TODO: BranchPool should not be static
		BranchPool.reset();
		MutationPool.clear();

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

		// Forget the old SUT
		Properties.resetTargetClass();

		TestCaseExecutor.initExecutor();
		
		// Constant pool
		ConstantPoolManager.getInstance().reset();

		if(Properties.CRITERION == Properties.Criterion.DEFUSE) {
			DefUsePool.clear();
			try {
				TestClusterGenerator.generateCluster(Properties.TARGET_CLASS, DependencyAnalysis.getInheritanceTree(), DependencyAnalysis.getCallTree());
			} catch (RuntimeException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		//Properties.getTargetClass();
}

}
