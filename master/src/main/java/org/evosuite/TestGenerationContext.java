/**
 * 
 */
package org.evosuite;

import org.evosuite.contracts.ContractChecker;
import org.evosuite.contracts.FailingTestSet;
import org.evosuite.coverage.branch.BranchPool;
import org.evosuite.coverage.dataflow.DefUsePool;
import org.evosuite.coverage.mutation.MutationPool;
import org.evosuite.ga.stoppingconditions.GlobalTimeStoppingCondition;
import org.evosuite.ga.stoppingconditions.MaxStatementsStoppingCondition;
import org.evosuite.graphs.GraphPool;
import org.evosuite.graphs.cfg.BytecodeInstructionPool;
import org.evosuite.graphs.cfg.CFGMethodAdapter;
import org.evosuite.instrumentation.InstrumentingClassLoader;
import org.evosuite.runtime.Runtime;
import org.evosuite.seeding.CastClassManager;
import org.evosuite.seeding.ConstantPoolManager;
import org.evosuite.seeding.ObjectPoolManager;
import org.evosuite.setup.DependencyAnalysis;
import org.evosuite.setup.TestCluster;
import org.evosuite.setup.TestClusterGenerator;
import org.evosuite.testcase.ExecutionTracer;
import org.evosuite.testcase.TestCaseExecutor;
import org.evosuite.utils.SystemInUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Gordon Fraser
 * 
 */
public class TestGenerationContext {

	private static final Logger logger = LoggerFactory.getLogger(TestGenerationContext.class);

	private static final TestGenerationContext singleton = new TestGenerationContext();

	/**
	 * This is the classloader that does the instrumentation - it needs to be
	 * used by all test code
	 */
	private ClassLoader classLoader;

	/**
	 * The classloader used to load this class
	 */
	private ClassLoader originalClassLoader;

	/**
	 * Private singleton constructor
	 */
	private TestGenerationContext() {
		originalClassLoader = this.getClass().getClassLoader();
		classLoader = new InstrumentingClassLoader();
	}

	public static TestGenerationContext getInstance() {
		return singleton;
	}

	public void goingToExecuteSUTCode(){
		/*
		 * This is pretty important if the SUT use classloader of the running thread.
		 * If we do not set this up, we will end up with cast exceptions.
		 * 
		 * Note, an example in which this happens is in
		 * 
		 * org.dom4j.bean.BeanAttribute
		 * 
		 * in SF100 project 62_dom4j
		 */		
		Thread.currentThread().setContextClassLoader(classLoader);
	}
	
	public void doneWithExecuteingSUTCode(){
		Thread.currentThread().setContextClassLoader(originalClassLoader);
	}
	
	public ClassLoader getClassLoaderForSUT() {
		return classLoader;
	}	
	
	/**
	 * @deprecated use {@code getInstance().getClassLoaderForSUT()}
	 * 
	 * @return
	 */
	public static ClassLoader getClassLoader() {
		return getInstance().classLoader;
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
		DefUsePool.clear();

		// TODO: This is not nice
		CFGMethodAdapter.methods.clear();

		// TODO: Clear only pool of current classloader?
		BytecodeInstructionPool.clearAll();

		// TODO: After this, the test cluster is empty until DependencyAnalysis.analyse is called
		TestCluster.reset();
		CastClassManager.getInstance().clear();

		// This counts the current level of recursion during test generation
		org.evosuite.testcase.TestFactory.getInstance().reset();

		MaxStatementsStoppingCondition.setNumExecutedStatements(0);
		GlobalTimeStoppingCondition.forceReset();

		// Forget the old SUT
		Properties.resetTargetClass();

		TestCaseExecutor.initExecutor();

		// Constant pool
		ConstantPoolManager.getInstance().reset();
		ObjectPoolManager.getInstance().reset();

		if (Properties.CRITERION == Properties.Criterion.DEFUSE) {
			try {
				TestClusterGenerator clusterGenerator = new TestClusterGenerator();
				clusterGenerator.generateCluster(Properties.TARGET_CLASS,
				                                 DependencyAnalysis.getInheritanceTree(),
				                                 DependencyAnalysis.getCallTree());
			} catch (RuntimeException e) {
				logger.error(e.getMessage(), e);
			} catch (ClassNotFoundException e) {
				logger.error(e.getMessage(), e);
			}
		}

		if (Properties.CHECK_CONTRACTS) {
			FailingTestSet.changeClassLoader(classLoader);
		}
		ContractChecker.setActive(true);

		SystemInUtil.resetSingleton();
		Runtime.resetSingleton();
	}
}
