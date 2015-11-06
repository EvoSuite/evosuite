/**
 * Copyright (C) 2010-2015 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser Public License as published by the
 * Free Software Foundation, either version 3.0 of the License, or (at your
 * option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License along
 * with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite;

import java.io.File;
import java.io.IOException;
import java.util.*;

import org.evosuite.Properties.Criterion;
import org.evosuite.Properties.StatisticsBackend;
import org.evosuite.Properties.StoppingCondition;
import org.evosuite.coverage.archive.TestsArchive;
import org.evosuite.coverage.exception.ExceptionCoverageFactory;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.result.TestGenerationResult;
import org.evosuite.runtime.RuntimeSettings;
import org.evosuite.runtime.instrumentation.RuntimeInstrumentation;
import org.evosuite.runtime.mock.MockFramework;
import org.evosuite.runtime.classhandling.ResetManager;
import org.evosuite.statistics.OutputVariable;
import org.evosuite.statistics.RuntimeVariable;
import org.evosuite.statistics.backend.DebugStatisticsBackend;
import org.evosuite.testsuite.TestSuiteChromosome;
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
		RuntimeInstrumentation.setAvoidInstrumentingShadedClasses(false);
		RuntimeSettings.applyUIDTransformation = false;

		TestGenerationContext.getInstance().resetContext();
		ResetManager.getInstance().clearManager();
		System.setProperties(currentProperties);
		Properties.getInstance().resetToDefaults();
		ExceptionCoverageFactory.getGoals().clear();
		TestsArchive.instance.reset();
	}

	@Before
	public void setDefaultPropertiesForTestCases() {
		
		Properties.getInstance().resetToDefaults();

		Properties.IS_RUNNING_A_SYSTEM_TEST = true;
		RuntimeInstrumentation.setAvoidInstrumentingShadedClasses(true);

		Properties.HTML = false;
		Properties.SHOW_PROGRESS = false;
		Properties.SERIALIZE_RESULT = false;
		Properties.JUNIT_TESTS = false;
		Properties.PLOT = false;

		Properties.STOPPING_CONDITION = StoppingCondition.MAXSTATEMENTS;
		Properties.SEARCH_BUDGET = 30000;

		Properties.GLOBAL_TIMEOUT = 120;
		Properties.MINIMIZATION_TIMEOUT = 8;
		Properties.EXTRA_TIMEOUT = 2;

		Properties.ENABLE_ASSERTS_FOR_EVOSUITE = true;
		Properties.CLIENT_ON_THREAD = true;
		Properties.SANDBOX = false;
		Properties.ERROR_BRANCHES = false;
		Properties.CRITERION = new Criterion[] { Criterion.BRANCH };

		Properties.NEW_STATISTICS = true;
		Properties.STATISTICS_BACKEND = StatisticsBackend.DEBUG;
		
		TestGenerationContext.getInstance().resetContext();
		ResetManager.getInstance().clearManager();

		//change seed every month
		long seed = new GregorianCalendar().get(Calendar.MONTH);
		Randomness.setSeed(seed);

		currentProperties = (java.util.Properties) System.getProperties().clone();
		
		MockFramework.enable();
	}

	protected GeneticAlgorithm<?>  do100percentLineTest(Class<?> target){
		EvoSuite evosuite = new EvoSuite();

		String targetClass = target.getCanonicalName();

		Properties.TARGET_CLASS = targetClass;
		Properties.CRITERION = new Properties.Criterion[]{Properties.Criterion.LINE};

		String[] command = new String[] { "-generateSuite", "-class", targetClass };

		Object result = evosuite.parseCommandLine(command);
		GeneticAlgorithm<?> ga = getGAFromResult(result);
		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
		System.out.println("EvolvedTestSuite:\n" + best);

		Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);

		return ga;
	}


	protected void checkUnstable() throws IllegalStateException{
		
		if(!Properties.OUTPUT_VARIABLES.contains(RuntimeVariable.HadUnstableTests.toString())){
			throw new IllegalStateException("Properties.OUTPUT_VARIABLES needs to contain RuntimeVariable.HadUnstableTests");
		}
		
		Map<String, OutputVariable<?>> map = DebugStatisticsBackend.getLatestWritten();
		Assert.assertNotNull(map);
		OutputVariable unstable = map.get(RuntimeVariable.HadUnstableTests.toString());
		Assert.assertNotNull(unstable);
		Assert.assertEquals(Boolean.FALSE, unstable.getValue());
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

		String master = getMasterTestsTarget();
		String runtime = getRuntimeTestsTarget();
		String client = getClientTestsTarget();
		String external = getExternalTarget();

		EvoSuite evosuite = new EvoSuite();
		String[] command = new String[] { "-setup", master,runtime, client, external };

		Object result = evosuite.parseCommandLine(command);
		Assert.assertNull(result);
		File evoProp = new File(Properties.OUTPUT_DIR + File.separator
		        + "evosuite.properties");
		Assert.assertTrue("It was not created: " + evoProp.getAbsolutePath(),
		                  evoProp.exists());

		hasBeenAlreadyRun = true;
	}

    //FIXME: these will change once com.examples goes to its own module

	private static String getExternalTarget(){
		String target = System.getProperty("user.dir") + File.separator + "external";

		checkFile(target);
		return target;
	}

	private static String getMasterTestsTarget() {
		String target = System.getProperty("user.dir") + File.separator + "target"
		        + File.separator + "test-classes";

		checkFile(target);
		return target;
	}

	private static String getRuntimeTestsTarget() {
		String target = 
				System.getProperty("user.dir") + 
				File.separator +".." + 
				File.separator +"runtime" +		
				File.separator + "target"
		        + File.separator + "test-classes";

		checkFile(target);
		return target;
	}

    private static String getClientTestsTarget() {
        String target =
                System.getProperty("user.dir") +
                        File.separator +".." +
                        File.separator +"client" +
                        File.separator + "target"
                        + File.separator + "test-classes";

        checkFile(target);
        return target;
    }

	private static void checkFile(String target) {
		File targetDir = new File(target);
		try {
			Assert.assertTrue("Target directory does not exist: "
			                          + targetDir.getCanonicalPath(), targetDir.exists());
		} catch (IOException e) {
			Assert.fail(e.getMessage());
		}
		Assert.assertTrue(targetDir.isDirectory());
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
		List<List<TestGenerationResult>> results = (List<List<TestGenerationResult>>)result;
		assert(results.size() == 1);
		//return results.iterator().next().getGeneticAlgorithm();
		return results.get(0).get(0).getGeneticAlgorithm();
	}
}
