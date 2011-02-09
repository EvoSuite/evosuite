/*
 * Copyright (C) 2010 Saarland University
 * 
 * This file is part of EvoSuite.
 * 
 * EvoSuite is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * EvoSuite is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser Public License
 * along with EvoSuite.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.unisb.cs.st.evosuite.coverage;

import java.util.List;

import org.apache.log4j.Logger;

import de.unisb.cs.st.evosuite.testcase.ExecutionResult;
import de.unisb.cs.st.evosuite.testcase.ExecutionTracer;
import de.unisb.cs.st.evosuite.testcase.TestCase;
import de.unisb.cs.st.evosuite.testcase.TestCaseExecutor;

/**
 * @author Gordon Fraser
 *
 */
public abstract class TestCoverageGoal {

	protected static Logger logger = Logger.getLogger(TestCoverageGoal.class);
	
	protected static TestCaseExecutor executor = new TestCaseExecutor();
	
	/**
	 * Return true if this coverage goal is covered by the given test
	 * 
	 * @param test
	 * @return
	 */
	public abstract boolean isCovered(TestCase test);
	
	
	/**
	 * Determine if there is an existing test case covering this goal
	 * @return
	 */
	public boolean isCovered(List<TestCase> tests) {
		for(TestCase test : tests) {
			if(isCovered(test))
				return true;
		}
		return false;
	}
	
	public static boolean hasTimeout(ExecutionResult result) {
		
		if(result == null) {
			logger.warn("Result is null!");
			return false;
		}
		else if(result.test == null) {
			logger.warn("Test is null!");
			return false;
		}
		int size = result.test.size();
		if(result.exceptions.containsKey(size)) {
			if(result.exceptions.get(size) instanceof TestCaseExecutor.TimeoutExceeded) {
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Execute a test case
	 * @param test
	 *   The test case to execute
	 * @param mutant
	 *   The mutation to active (null = no mutation)
	 *   
	 * @return
	 *   Result of the execution
	 */
	protected ExecutionResult runTest(TestCase test) {
		
		ExecutionResult result = new ExecutionResult(test, null);
		
		try {
			
			result.exceptions = executor.run(test);
			executor.setLogging(true);
			result.trace = ExecutionTracer.getExecutionTracer().getTrace();
		} catch(Exception e) {
			System.out.println("TG: Exception caught: "+e);
			e.printStackTrace();
			System.exit(1);
		}

		return result;
	}
}
