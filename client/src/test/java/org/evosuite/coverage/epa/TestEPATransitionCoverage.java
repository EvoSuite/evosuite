package org.evosuite.coverage.epa;

import org.evosuite.Properties;
import org.evosuite.Properties.Criterion;
import org.evosuite.classpath.ClassPathHandler;
import org.evosuite.testcase.execution.ExecutionTracer;
import org.evosuite.testcase.execution.TestCaseExecutor;
import org.junit.After;
import org.junit.Before;

public abstract class TestEPATransitionCoverage {

	private final static Criterion[] DEFAULT_CRITERION = Properties.CRITERION;
	private final static boolean DEFAULT_IS_TRACE_ENABLED = ExecutionTracer.isTraceCallsEnabled();

	@Before
	public void prepareExecutionEnvironment() {
		TestCaseExecutor.initExecutor();
		ExecutionTracer.enableTraceCalls();
		ClassPathHandler.getInstance().changeTargetCPtoTheSameAsEvoSuite();
		
		Properties.CRITERION = new Criterion[] { Properties.Criterion.EPATRANSITION };
	}
	
	@After
	public void restoreDefaults() {
		Properties.CRITERION = DEFAULT_CRITERION;
		if (DEFAULT_IS_TRACE_ENABLED) {
			ExecutionTracer.enableTraceCalls();
		} else {
			ExecutionTracer.disableTraceCalls();
		}
		TestCaseExecutor.pullDown();
	}

}
