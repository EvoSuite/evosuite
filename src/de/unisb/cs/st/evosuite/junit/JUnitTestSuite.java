/**
 * 
 */
package de.unisb.cs.st.evosuite.junit;

import java.util.HashSet;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.junit.runner.JUnitCore;

import de.unisb.cs.st.evosuite.testcase.ExecutionResult;
import de.unisb.cs.st.evosuite.testcase.ExecutionTrace;
import de.unisb.cs.st.evosuite.testcase.ExecutionTracer;
import de.unisb.cs.st.evosuite.testcase.TestCase;
import de.unisb.cs.st.evosuite.testcase.TestCaseExecutor;
import de.unisb.cs.st.evosuite.testsuite.TestSuiteChromosome;


/**
 * @author Gordon Fraser
 *
 */
public class JUnitTestSuite {

	private static Logger logger = Logger.getLogger(JUnitTestSuite.class);
	
	private Set<String> covered_methods;
	
	private Set<String> covered_branches_true;

	private Set<String> covered_branches_false;

	private TestCaseExecutor executor = new TestCaseExecutor();

	public void runSuite(String name) {
		try {
			Class<?> forName = null;
			forName = Class.forName(name);
			logger.info("Running against JUnit test suite "+name);
			JUnitCore.runClasses(forName);
			ExecutionTrace trace = ExecutionTracer.getExecutionTracer().getTrace();
			
			
			covered_methods  = new HashSet<String>();
			covered_branches_true = new HashSet<String>();
			covered_branches_false = new HashSet<String>();
			
			for(Entry<String, Integer> entry : trace.covered_methods.entrySet()) {
				if(!entry.getKey().contains("$"))
					covered_methods.add(entry.getKey());
			}

			for(Entry<String, Double> entry : trace.true_distances.entrySet()) {
				if(entry.getValue() == 0.0)
					if(!entry.getKey().contains("$"))
						covered_branches_true.add(entry.getKey());
			}
			
			for(Entry<String, Double> entry : trace.false_distances.entrySet()) {
				if(entry.getValue() == 0.0)
					if(!entry.getKey().contains("$"))
						covered_branches_false.add(entry.getKey());
			}


		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public void runSuite(TestSuiteChromosome chromosome) {
		covered_methods  = new HashSet<String>();
		covered_branches_true = new HashSet<String>();
		covered_branches_false = new HashSet<String>();

		for(TestCase test : chromosome.getTests()) {
			ExecutionResult result = runTest(test);
			for(Entry<String, Integer> entry : result.trace.covered_methods.entrySet()) {
				//if(!entry.getKey().contains("$"))
					covered_methods.add(entry.getKey());
			}

			for(Entry<String, Double> entry : result.trace.true_distances.entrySet()) {
				if(entry.getValue() == 0.0)
					//if(!entry.getKey().contains("$"))
						covered_branches_true.add(entry.getKey());
			}
			
			for(Entry<String, Double> entry : result.trace.false_distances.entrySet()) {
				if(entry.getValue() == 0.0)
					//if(!entry.getKey().contains("$"))
						covered_branches_false.add(entry.getKey());
			}
		}
	}

	public Set<String> getCoveredMethods() {
		return covered_methods;
	}
	
	public Set<String> getTrueCoveredBranches() {
		return covered_branches_true;
	}

	public Set<String> getFalseCoveredBranches() {
		return covered_branches_false;
	}
	
public ExecutionResult runTest(TestCase test) {
		
		ExecutionResult result = new ExecutionResult(test, null);
		
		try {
			result.exceptions = executor.runWithTrace(test);
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
