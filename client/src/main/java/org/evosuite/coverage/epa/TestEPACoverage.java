package org.evosuite.coverage.epa;

import org.evosuite.Properties;
import org.evosuite.Properties.Criterion;
import org.evosuite.TestGenerationContext;
import org.evosuite.classpath.ClassPathHandler;
import org.evosuite.runtime.classhandling.ResetManager;
import org.evosuite.testcase.execution.ExecutionTracer;
import org.evosuite.utils.Randomness;
import org.junit.After;
import org.junit.Before;

public abstract class TestEPACoverage {

	private final static boolean DEFAULT_IS_TRACE_ENABLED = ExecutionTracer.isTraceCallsEnabled();
	private java.util.Properties currentProperties;

	@Before
	public void setUp() {
		ClassPathHandler.getInstance().changeTargetCPtoTheSameAsEvoSuite();

		Properties.getInstance().resetToDefaults();

		Randomness.setSeed(42);
		Properties.TARGET_CLASS = "";

		TestGenerationContext.getInstance().resetContext();
		ResetManager.getInstance().clearManager();
		Randomness.setSeed(42);

		currentProperties = (java.util.Properties) System.getProperties().clone();

		Properties.CRITERION = getCriteria();
		ExecutionTracer.enableTraceCalls();
	}

	abstract protected Criterion[] getCriteria();

	@After
	public void tearDown() {
		if (DEFAULT_IS_TRACE_ENABLED) {
			ExecutionTracer.enableTraceCalls();
		} else {
			ExecutionTracer.disableTraceCalls();
		}
		TestGenerationContext.getInstance().resetContext();
		ResetManager.getInstance().clearManager();
		System.setProperties(currentProperties);
		Properties.getInstance().resetToDefaults();
	}

}
