/**
 * 
 */
package org.evosuite;

import org.apache.commons.cli.Option;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Gordon Fraser
 * 
 */
public class TestInstrumentDependency extends SystemTest {

	private final boolean oldAssertions = Properties.ASSERTIONS;
	private final boolean oldMinimize = Properties.MINIMIZE;
	private final boolean oldErrorBranches = Properties.ERROR_BRANCHES;

	@Override
	@After
	public void resetStaticVariables() {
		Properties.ASSERTIONS = oldAssertions;
		Properties.MINIMIZE = oldMinimize;
		Properties.ERROR_BRANCHES = oldErrorBranches;
	}

	@Test
	public void testErrorBranchesOnDependency() {
		EvoSuite evosuite = new EvoSuite();

		String targetClass = Option.class.getCanonicalName();

		Properties.TARGET_CLASS = targetClass;
		Properties.SEARCH_BUDGET = 1000;
		Properties.MINIMIZE = false;
		Properties.ASSERTIONS = false;

		Properties.ERROR_BRANCHES = true;
		String[] commandErrorBranches = new String[] { "-generateSuite", "-class",
		        targetClass };
		evosuite.parseCommandLine(commandErrorBranches);
		int errorBranchGoals = TestSuiteGenerator.getFitnessFactory().getCoverageGoals().size();

		resetStaticVariables();

		Properties.ERROR_BRANCHES = false;
		String[] command = new String[] { "-generateSuite", "-class", targetClass };
		evosuite.parseCommandLine(command);
		int originalGoals = TestSuiteGenerator.getFitnessFactory().getCoverageGoals().size();

		Assert.assertTrue(errorBranchGoals > originalGoals);
	}
}
