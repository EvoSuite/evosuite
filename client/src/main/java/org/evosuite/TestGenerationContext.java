/**
 * 
 */
package org.evosuite;

import org.evosuite.contracts.ContractChecker;
import org.evosuite.contracts.FailingTestSet;
import org.evosuite.coverage.archive.TestsArchive;
import org.evosuite.coverage.branch.BranchPool;
import org.evosuite.coverage.dataflow.DefUsePool;
import org.evosuite.coverage.mutation.MutationPool;
import org.evosuite.coverage.mutation.MutationTimeoutStoppingCondition;
import org.evosuite.ga.stoppingconditions.GlobalTimeStoppingCondition;
import org.evosuite.ga.stoppingconditions.MaxStatementsStoppingCondition;
import org.evosuite.graphs.GraphPool;
import org.evosuite.graphs.cfg.BytecodeInstructionPool;
import org.evosuite.graphs.cfg.CFGMethodAdapter;
import org.evosuite.instrumentation.InstrumentingClassLoader;
import org.evosuite.instrumentation.LinePool;
import org.evosuite.runtime.Runtime;
import org.evosuite.runtime.instrumentation.MethodCallReplacementCache;
import org.evosuite.runtime.util.SystemInUtil;
import org.evosuite.seeding.CastClassManager;
import org.evosuite.seeding.ConstantPoolManager;
import org.evosuite.seeding.ObjectPoolManager;
import org.evosuite.setup.DependencyAnalysis;
import org.evosuite.setup.TestCluster;
import org.evosuite.setup.TestClusterGenerator;
import org.evosuite.testcarver.extraction.CarvingManager;
import org.evosuite.testcase.execution.ExecutionTracer;
import org.evosuite.testcase.execution.TestCaseExecutor;
import org.evosuite.utils.ArrayUtil;
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
	private InstrumentingClassLoader classLoader;
	
	/**
	 * The regerssion class loader
	 */
	private InstrumentingClassLoader regressionClassLoader;

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
		regressionClassLoader = new InstrumentingClassLoader(true);
	}

	public static TestGenerationContext getInstance() {
		return singleton;
	}

    /**
		 * This is pretty important if the SUT use classloader of the running thread.
		 * If we do not set this up, we will end up with cast exceptions.
		 *
     * <p>
		 * Note, an example in which this happens is in
		 *
     * <p>
		 * org.dom4j.bean.BeanAttribute
		 *
     * <p>
		 * in SF100 project 62_dom4j
		 */
    public void goingToExecuteSUTCode(){

		Thread.currentThread().setContextClassLoader(classLoader);
	}
	
	public void doneWithExecuteingSUTCode(){
		Thread.currentThread().setContextClassLoader(originalClassLoader);
	}
	
	public InstrumentingClassLoader getClassLoaderForSUT() {
		return classLoader;
	}	
	
	public InstrumentingClassLoader getRegressionClassLoaderForSUT() {
		return regressionClassLoader;
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
		BranchPool.getInstance(classLoader).reset();
		LinePool.reset();
		MutationPool.clear();

		// TODO: Clear only pool of current classloader?
		GraphPool.clearAll();
		DefUsePool.clear();

		// TODO: This is not nice
		for(ClassLoader cl:CFGMethodAdapter.methods.keySet())
			CFGMethodAdapter.methods.get(cl).clear();

		// TODO: Clear only pool of current classloader?
		BytecodeInstructionPool.clearAll();

		// TODO: After this, the test cluster is empty until DependencyAnalysis.analyse is called
		TestCluster.reset();
		CastClassManager.getInstance().clear();

		// This counts the current level of recursion during test generation
		org.evosuite.testcase.TestFactory.getInstance().reset();

		MaxStatementsStoppingCondition.setNumExecutedStatements(0);
		GlobalTimeStoppingCondition.forceReset();
		MutationTimeoutStoppingCondition.resetStatic();

		// Forget the old SUT
		Properties.resetTargetClass();

		TestCaseExecutor.initExecutor();

		TestsArchive.instance.reset();
		
		// Constant pool
		ConstantPoolManager.getInstance().reset();
		ObjectPoolManager.getInstance().reset();
		CarvingManager.getInstance().clear();

		// TODO: Why are we doing this?
		if (Properties.INSTRUMENT_CONTEXT
				|| ArrayUtil.contains(Properties.CRITERION, Properties.Criterion.DEFUSE)
				|| ArrayUtil.contains(Properties.CRITERION, Properties.Criterion.IBRANCH)) {
//				|| ArrayUtil.contains(Properties.CRITERION, Properties.Criterion.CBRANCH)) {
			try {
				TestClusterGenerator clusterGenerator = new TestClusterGenerator();
				clusterGenerator.generateCluster(Properties.TARGET_CLASS,
				                                 DependencyAnalysis.getInheritanceTree(),  DependencyAnalysis.getCallGraph());
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
        MethodCallReplacementCache.resetSingleton();
	}
}
