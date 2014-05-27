/**
 * Copyright (C) 2011,2012 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 * 
 * This file is part of EvoSuite.
 * 
 * EvoSuite is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 * 
 * EvoSuite is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Public License for more details.
 * 
 * You should have received a copy of the GNU Public License along with
 * EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.evosuite.Properties.Criterion;
import org.evosuite.Properties.StatisticsBackend;
import org.evosuite.Properties.StoppingCondition;
import org.evosuite.ga.GeneticAlgorithm;
import org.evosuite.reset.ResetManager;
import org.evosuite.result.TestGenerationResult;
import org.evosuite.utils.Randomness;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;

/**
 * @author Andrea Arcuri
 * 
 */
public class SystemTest {

	public static final String ALREADY_SETUP = "systemtest.alreadysetup";

	private static java.util.Properties currentProperties;

	static {
		String s = System.getProperty(ALREADY_SETUP);
		if (s == null) {
			System.setProperty(ALREADY_SETUP, ALREADY_SETUP);
			runSetup();
		}
	}

	@After
	public void resetStaticVariables() {
		TestGenerationContext.getInstance().resetContext();
		ResetManager.getInstance().clearManager();
		System.setProperties(currentProperties);
		Properties.getInstance().resetToDefaults();
	}

	@Before
	public void setDefaultPropertiesForTestCases() {
		
		ClientProcess.geneticAlgorithmStatus = null;
		
		Properties.getInstance().resetToDefaults();
		
		Properties.HTML = false;
		Properties.SHOW_PROGRESS = false;
		Properties.SERIALIZE_RESULT = false;
		Properties.JUNIT_TESTS = false;
		Properties.PLOT = false;

		Properties.STOPPING_CONDITION = StoppingCondition.MAXSTATEMENTS;
		Properties.SEARCH_BUDGET = 10000;

		Properties.GLOBAL_TIMEOUT = 120;
		Properties.MINIMIZATION_TIMEOUT = 8;
		Properties.EXTRA_TIMEOUT = 2;

		Properties.ENABLE_ASSERTS_FOR_EVOSUITE = true;
		Properties.CLIENT_ON_THREAD = true;
		Properties.SANDBOX = false;
		Properties.ERROR_BRANCHES = false;
		Properties.CRITERION = Criterion.BRANCH;

		Properties.NEW_STATISTICS = true;
		Properties.OLD_STATISTICS = false;
		Properties.STATISTICS_BACKEND = StatisticsBackend.DEBUG;
		
		TestGenerationContext.getInstance().resetContext();
		ResetManager.getInstance().clearManager();
		Randomness.setSeed(42);

		currentProperties = (java.util.Properties) System.getProperties().clone();
	}

	/*
	 * this static variable is a safety net to be sure it is called only once. 
	 * static variables are shared and not re-initialized
	 * during a sequence of test cases.
	 */
	private static boolean hasBeenAlreadyRun = false;

	private static void runSetup() {
		if (hasBeenAlreadyRun) {
			return;
		}

		deleteEvoDirs();

		System.out.println("*** SystemTest: runSetup() ***");

		String target = System.getProperty("user.dir") + File.separator + "target"
		        + File.separator + "test-classes";

		File targetDir = new File(target);
		try {
			Assert.assertTrue("Target directory does not exist: "
			                          + targetDir.getCanonicalPath(), targetDir.exists());
		} catch (IOException e) {
			Assert.fail(e.getMessage());
		}
		Assert.assertTrue(targetDir.isDirectory());

		EvoSuite evosuite = new EvoSuite();
		String[] command = new String[] { "-setup", target };

		Object result = evosuite.parseCommandLine(command);
		Assert.assertNull(result);
		File evoProp = new File(Properties.OUTPUT_DIR + File.separator
		        + "evosuite.properties");
		Assert.assertTrue("It was not created: " + evoProp.getAbsolutePath(),
		                  evoProp.exists());

		hasBeenAlreadyRun = true;
	}

	private static void deleteEvoDirs() {

		System.out.println("*** SystemTest: deleteEvoDirs() ***");

		try {
			org.apache.commons.io.FileUtils.deleteDirectory(new File("evosuite-files"));
			org.apache.commons.io.FileUtils.deleteDirectory(new File("evosuite-report"));
			org.apache.commons.io.FileUtils.deleteDirectory(new File("evosuite-tests"));
		} catch (IOException e) {
			Assert.fail(e.getMessage());
		}
		hasBeenAlreadyRun = false;
	}
	
	@SuppressWarnings("unchecked")
	protected GeneticAlgorithm<?> getGAFromResult(Object result) {
		assert(result instanceof List);
		List<TestGenerationResult> results = (List<TestGenerationResult>)result;
		assert(results.size() == 1);
		return results.iterator().next().getGeneticAlgorithm();
	}
}
