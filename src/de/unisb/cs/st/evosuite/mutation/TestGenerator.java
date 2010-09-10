/*
 * Copyright (C) 2009 Saarland University
 * 
 * This file is part of Javalanche.
 * 
 * Javalanche is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Javalanche is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser Public License
 * along with Javalanche.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.unisb.cs.st.evosuite.mutation;


import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import de.unisb.cs.st.evosuite.testcase.ExecutionTracer;
import de.unisb.cs.st.javalanche.mutation.runtime.testDriver.MutationTestDriver;

/**
 * This class is the central test case generator. 
 * It activates the test case generation strategy defined by the user, 
 * asks this strategy to create some tests, and then writes the resulting
 * tests to a test suite file.
 * 
 * @author Gordon Fraser
 *
 */
public class TestGenerator {
	
	private static Logger logger = Logger.getLogger(TestGenerator.class);
	
	private MutationTestDriver test_driver;
	
	Set<String> tests;	

	/**
	 * Constructor
	 * 
	 * @param driver
	 *   The test driver that knows about the existing junit tests
	 */
	public TestGenerator(MutationTestDriver driver) {
		test_driver = driver;
		List<String> existing_tests = test_driver.getAllTests();
		tests = new HashSet<String>();
		for(String test : existing_tests) {
			tests.add(test);
		}
		logger.info("TG: Number of existing tests  : "+tests.size());
	}
	
	/**
	 * Choose test generation strategy defined by user and generate tests.
	 * Resulting tests are written in JUnit test suites.
	 */
	public void generateTests() {
		TestGenerationStrategy strategy;
		String strategy_name = System.getProperty("test.strategy");
		if("TestSuite".equals(strategy_name))
			strategy = new TestSuiteStrategy();
		else
			strategy = new TestGAStrategy();

		/*
		if("OneByOne".equals(strategy_name))
			strategy = new OneByOneStrategy();
		else if("OneByOneMonitored".equals(strategy_name))
			strategy = new OneByOneMonitoredStrategy();
		else if("OneByOneMaximalFitness".equals(strategy_name))
			strategy = new OneByOneMaximalFitness();
		else if("TestGA".equals(strategy_name)) {
			strategy = new TestGAStrategy();
		}
		else
			strategy = new KillOneStrategy();
			*/
		ExecutionTracer.getExecutionTracer().checkMutations();
//		TestGenerationStrategy strategy = new OneByOneStrategy();
//		TestGenerationStrategy strategy = new OneByOneMonitoredStrategy();
		strategy.generateTests();
		
		String test_class_name = System.getProperty("test.classes");
		String output_dir      = System.getProperty("test.directory");
		String bug_dir         = System.getProperty("bug.directory");
		if(test_class_name == null) {
			logger.error("Parameter test.classes not set, not writing test suite");
			return;
		}
		if(output_dir == null) {
			logger.error("Parameter test.directory not set, not writing test suite");
			return;
		}

		if(bug_dir == null) {
			logger.error("Parameter bug.directory not set, not writing failed tests");
			return;
		}
		File theDir = new File(output_dir);
		if(!theDir.exists()) {
			logger.error("Output directory "+output_dir+" does not exist, not writing test suite");
			return;			
		}
		theDir = new File(bug_dir);
		if(!theDir.exists()) {
			logger.error("Bug output directory "+bug_dir+" does not exist, not writing test suite");
			return;			
		}
		
		String[] parts = test_class_name.split("/");
		String suite_name = parts[parts.length - 1].replaceAll("_\\d+.task$", "").replace(".","_");
		strategy.writeTestSuite("TestSuite_"+suite_name, output_dir);
		strategy.writeFailedTests("Failed_"+suite_name, bug_dir);
		
		MutationStatistics statistics = MutationStatistics.getInstance();
		//statistics.logStatistics();
	}
	
}
