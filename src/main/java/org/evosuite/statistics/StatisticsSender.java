package org.evosuite.statistics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.evosuite.coverage.exception.ExceptionCoverageSuiteFitness;
import org.evosuite.ga.Chromosome;
import org.evosuite.rmi.ClientServices;
import org.evosuite.testcase.ExecutionResult;
import org.evosuite.testcase.ExecutionTrace;
import org.evosuite.testcase.TestCaseExecutor;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testsuite.TestSuiteChromosome;

/**
 * Class responsible to send "individuals" from Client to Master process.
 * All sending of individuals should go through this class, and not 
 * calling ClientServices directly
 * 
 * <p>
 * TODO: still to clarify what type of extra information we want to send with each individual,
 * eg the state in which it was computed (Search vs Minimization) 
 * 
 * @author arcuri
 *
 */
public class StatisticsSender {

	/**
	 * Send the given individual to the Client, plus any other needed info
	 * 
	 * @param individual
	 */
	public static void sendIndividualToMaster(Chromosome individual) throws IllegalArgumentException{
		if(individual == null){
			throw new IllegalArgumentException("No defined individual to send");
		}
		ClientServices.getInstance().getClientNode().updateStatistics(individual);

	}


	/**
	 * First execute (if needed) the test cases to be sure to have latest correct data,
	 * and then send it to Master
	 */
	public static void executedAndThenSendIndividualToMaster(TestSuiteChromosome testSuite) throws IllegalArgumentException{
		if(testSuite == null){
			throw new IllegalArgumentException("No defined test suite to send");
		}

		/*
		 * TODO: shouldn't a test that was never executed always be executed before sending?
		 * ie, do we really need a separated public sendIndividualToMaster???
		 */

		for (TestChromosome test : testSuite.getTestChromosomes()) {
			if (test.getLastExecutionResult() == null) {
				ExecutionResult result = TestCaseExecutor.runTest(test.getTestCase());
				test.setLastExecutionResult(result);
			}
		}

		sendCoveredInfo(testSuite);
		sendExceptionInfo(testSuite);
		sendIndividualToMaster(testSuite);		
	}

	// -------- private methods ------------------------

	private static void sendExceptionInfo(TestSuiteChromosome testSuite) {

		List<ExecutionResult> results = new ArrayList<>();
		
		for (TestChromosome testChromosome : testSuite.getTestChromosomes()) {
			results.add(testChromosome.getLastExecutionResult());
		}

		/*
		 * for each method name, check the class of thrown exceptions in those methods
		 */
		Map<String, Set<Class<?>>> implicitTypesOfExceptions = new HashMap<>();
		Map<String, Set<Class<?>>> explicitTypesOfExceptions = new HashMap<>();

		ExceptionCoverageSuiteFitness.calculateExceptionInfo(results,implicitTypesOfExceptions,explicitTypesOfExceptions);

		ClientServices.getInstance().getClientNode().trackOutputVariable(
				RuntimeVariable.Explicit_MethodExceptions, ExceptionCoverageSuiteFitness.getNumExceptions(explicitTypesOfExceptions));
		ClientServices.getInstance().getClientNode().trackOutputVariable(
				RuntimeVariable.Explicit_TypeExceptions, ExceptionCoverageSuiteFitness.getNumClassExceptions(explicitTypesOfExceptions));
		ClientServices.getInstance().getClientNode().trackOutputVariable(
				RuntimeVariable.Implicit_MethodExceptions, ExceptionCoverageSuiteFitness.getNumExceptions(implicitTypesOfExceptions));
		ClientServices.getInstance().getClientNode().trackOutputVariable(
				RuntimeVariable.Implicit_TypeExceptions, ExceptionCoverageSuiteFitness.getNumClassExceptions(implicitTypesOfExceptions));

		/*
		 * NOTE: in old report generator, we were using Properties.SAVE_ALL_DATA
		 * to check if writing the full explicitTypesOfExceptions and implicitTypesOfExceptions
		 */
	}

	

	private static void sendCoveredInfo(TestSuiteChromosome testSuite){

		Set<String> coveredMethods = new HashSet<String>();
		Set<Integer> coveredTrueBranches = new HashSet<Integer>();
		Set<Integer> coveredFalseBranches = new HashSet<Integer>();
		Set<Integer> coveredLines = new HashSet<Integer>();

		for (TestChromosome test : testSuite.getTestChromosomes()) {
			ExecutionTrace trace = test.getLastExecutionResult().getTrace();
			coveredMethods.addAll(trace.getCoveredMethods());
			coveredTrueBranches.addAll(trace.getCoveredTrueBranches());
			coveredFalseBranches.addAll(trace.getCoveredFalseBranches());
			coveredLines.addAll(trace.getCoveredLines());
		}

		ClientServices.getInstance().getClientNode().trackOutputVariable(
				RuntimeVariable.Covered_Goals, testSuite.getCoveredGoals().size());		
		ClientServices.getInstance().getClientNode().trackOutputVariable(
				RuntimeVariable.Covered_Methods, coveredMethods.size());	
		ClientServices.getInstance().getClientNode().trackOutputVariable(
				RuntimeVariable.Covered_Branches, coveredTrueBranches.size() + coveredFalseBranches.size());
		ClientServices.getInstance().getClientNode().trackOutputVariable(
				RuntimeVariable.Covered_Lines, coveredLines);
	}
}
