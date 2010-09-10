/**
 * 
 */
package de.unisb.cs.st.evosuite.testsuite;

import java.util.ArrayList;
import java.util.List;


import org.apache.log4j.Logger;

import de.unisb.cs.st.evosuite.testcase.ExecutionResult;
import de.unisb.cs.st.evosuite.testcase.ExecutionTracer;
import de.unisb.cs.st.evosuite.testcase.MaxStatementsStoppingCondition;
import de.unisb.cs.st.evosuite.testcase.TestCase;
import de.unisb.cs.st.evosuite.testcase.TestCaseExecutor;
import de.unisb.cs.st.evosuite.testcase.TestChromosome;
import de.unisb.cs.st.ga.Chromosome;
import de.unisb.cs.st.ga.FitnessFunction;

/**
 * @author Gordon Fraser
 *
 */
public abstract class TestSuiteFitnessFunction extends FitnessFunction {

	protected Logger logger = Logger.getLogger(TestSuiteFitnessFunction.class);
	
	protected TestCaseExecutor executor = new TestCaseExecutor();

//	protected Map<TestCase, ExecutionResult> result_cache = new HashMap<TestCase, ExecutionResult>();

	// protected Map<TestCase, ExecutionResult> result_cache = new LRUMap(Integer.parseInt(MutationProperties.getPropertyOrDefault("GA.test_cache", "20000")) + 1);

	// protected int test_cache_size = Integer.parseInt(MutationProperties.getPropertyOrDefault("GA.test_cache", "20000")) + 1;

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
	public ExecutionResult runTest(TestCase test) {
		
		ExecutionResult result = new ExecutionResult(test, null);
		
		try {
	        //logger.debug("Executing test");
			result.exceptions = executor.runWithTrace(test);
			executor.setLogging(true);
			result.trace = ExecutionTracer.getExecutionTracer().getTrace();
			//logger.info("Got data about "+result.trace.finished_calls.size()+" calls");
			//logger.info("Got data about "+result.trace.coverage.size()+" coverage methods");

			int num = test.size();
			/*
			if(ex != null) {
				result.exception = ex;
				result.exception_statement = test.exception_statement;
				num = test.size() - test.exception_statement;
				logger.debug("Exception raised: "+ex);
				logger.debug("Statement: "+test.getStatement(test.exception_statement).getCode());
				if(ex instanceof TestCaseExecutor.TimeoutExceeded) {
					if(mutant != null)
						logger.info("Mutant timed out!");
					else
						logger.info("Program timed out!");
					
				}
			}
			*/
			MaxStatementsStoppingCondition.statementsExecuted(num);
			//for(TestObserver observer : observers) {
			//	observer.testResult(result);
			//}
		} catch(Exception e) {
			System.out.println("TG: Exception caught: "+e);
			e.printStackTrace();
			System.exit(1);
		}

		//System.out.println("TG: Killed "+result.getNumKilled()+" out of "+mutants.size());
		return result;
	}

	
	@Override
	protected void updateIndividual(Chromosome individual, double fitness) {
		individual.setFitness(fitness);
	}
	
	protected List<ExecutionResult> runTestSuite(TestSuiteChromosome suite) {
		
		List<ExecutionResult> results = new ArrayList<ExecutionResult>();
		
		for(TestChromosome test : suite.tests) {
			// Only execute test if it hasn't been changed
			if(test.isChanged() || test.last_result == null) { // || !result_cache.containsKey(test.test)) {
				//logger.info("Executing test with length "+test.size());

				//logger.debug("Executing test with length "+test.size()+", Cache size: "+result_cache.size());
				//logger.trace(test.test.toCode());
				ExecutionResult result = runTest(test.test);
				results.add(result);
				
				/*
				ExecutionResult r = new ExecutionResult(test.test, null);
				r.trace = result.trace.clone();
				r.test = test.test.clone();
				r.exceptions = new HashMap<Integer, Throwable>(result.exceptions);
				*/
				test.last_result = result; //.clone();
				test.setChanged(false);
			}
			else {
				//logger.info("Skipping test with length "+test.size());
				//results.add(result_cache.get(test.test));
				results.add(test.last_result);
				//ExecutionResult result = runTest(test.test, m);
				//if(!result.trace.equals(result_cache.get(test.test))) {
				//	logger.error("Cached result does not match new result!");
				//}
				
				
			}
		}
		

		
		return results;
	}
}
