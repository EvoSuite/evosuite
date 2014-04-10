package org.evosuite.statistics;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.evosuite.Properties;
import org.evosuite.ga.Chromosome;
import org.evosuite.rmi.ClientServices;
import org.evosuite.testcase.ConstructorStatement;
import org.evosuite.testcase.ExecutionResult;
import org.evosuite.testcase.ExecutionTrace;
import org.evosuite.testcase.MethodStatement;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.TestCaseExecutor;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.objectweb.asm.Type;

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

		/*
		 * for each test case, keep track of thrown exceptions and their position (a index) in
		 * the test case sequence.
		 * For each of these exception, keep track of whether it was explicit (ie directly thrown)
		 */
		Map<TestCase, Map<Integer, Boolean>> isExceptionExplicit = new HashMap<TestCase, Map<Integer, Boolean>>();
		Map<TestCase, Map<Integer, Throwable>> exceptionMappings = new HashMap<TestCase, Map<Integer, Throwable>>();		

		for (TestChromosome testChromosome : testSuite.getTestChromosomes()) {
			TestCase tc = testChromosome.getTestCase();
			ExecutionResult res = testChromosome.getLastExecutionResult();
			isExceptionExplicit.put(tc, res.explicitExceptions);
			exceptionMappings.put(tc, res.getCopyOfExceptionMapping());
		}

		/*
		 * for each method name, check the class of thrown exceptions in those methods
		 */
		Map<String, Set<Class<?>>> implicitTypesOfExceptions = new HashMap<>();
		Map<String, Set<Class<?>>> explicitTypesOfExceptions = new HashMap<>();

		for (TestCase test : exceptionMappings.keySet()) {
			Map<Integer, Throwable> exceptions = exceptionMappings.get(test);
			//iterate on the indexes of the statements that resulted in an exception
			for (Integer i : exceptions.keySet()) {
				Throwable t = exceptions.get(i);
				if (t instanceof SecurityException && Properties.SANDBOX){
					continue;
				}
				if (i >= test.size()) {
					// Timeouts are put after the last statement if the process was forcefully killed
					continue;
				}

				String methodName = "";
				boolean sutException = false;

				if (test.getStatement(i) instanceof MethodStatement) {
					MethodStatement ms = (MethodStatement) test.getStatement(i);
					Method method = ms.getMethod().getMethod();
					methodName = method.getName() + Type.getMethodDescriptor(method);

					if (method.getDeclaringClass().equals(Properties.getTargetClass())){
						sutException = true;
					}

				} else if (test.getStatement(i) instanceof ConstructorStatement) {

					ConstructorStatement cs = (ConstructorStatement) test.getStatement(i);
					Constructor<?> constructor = cs.getConstructor().getConstructor();
					methodName = "<init>" + Type.getConstructorDescriptor(constructor);
					if (constructor.getDeclaringClass().equals(Properties.getTargetClass())){
						sutException = true;
					}
				}

				boolean notDeclared = !test.getStatement(i).getDeclaredExceptions().contains(t.getClass());

				if (notDeclared && sutException) {
					/*
					 * we need to distinguish whether it is explicit (ie "throw" in the code, eg for validating
					 * input for pre-condition) or implicit ("likely" a real fault).
					 */					
					boolean isExplicit = isExceptionExplicit.get(test).containsKey(i)
							&& isExceptionExplicit.get(test).get(i);

					if (isExplicit) {
						if (!explicitTypesOfExceptions.containsKey(methodName)){
							explicitTypesOfExceptions.put(methodName, new HashSet<Class<?>>());
						}
						explicitTypesOfExceptions.get(methodName).add(t.getClass());
					} else {
						if (!implicitTypesOfExceptions.containsKey(methodName)){
							implicitTypesOfExceptions.put(methodName, new HashSet<Class<?>>());
						}
						implicitTypesOfExceptions.get(methodName).add(t.getClass());
					}
				}
			}
		}

		ClientServices.getInstance().getClientNode().trackOutputVariable(
				RuntimeVariable.Explicit_MethodExceptions, getNumExceptions(explicitTypesOfExceptions));
		ClientServices.getInstance().getClientNode().trackOutputVariable(
				RuntimeVariable.Explicit_TypeExceptions, getNumClassExceptions(explicitTypesOfExceptions));
		ClientServices.getInstance().getClientNode().trackOutputVariable(
				RuntimeVariable.Implicit_MethodExceptions, getNumExceptions(implicitTypesOfExceptions));
		ClientServices.getInstance().getClientNode().trackOutputVariable(
				RuntimeVariable.Implicit_TypeExceptions, getNumClassExceptions(implicitTypesOfExceptions));

		/*
		 * NOTE: in old report generator, we were using Properties.SAVE_ALL_DATA
		 * to check if writing the full explicitTypesOfExceptions and implicitTypesOfExceptions
		 */
	}

	private static int getNumExceptions(Map<String, Set<Class<?>>> exceptions) {
		int total = 0;
		for (Set<Class<?>> exceptionSet : exceptions.values()) {
			total += exceptionSet.size();
		}
		return total;
	}

	private static int getNumClassExceptions(Map<String, Set<Class<?>>> exceptions) {
		Set<Class<?>> classExceptions = new HashSet<Class<?>>();
		for (Set<Class<?>> exceptionSet : exceptions.values()) {
			classExceptions.addAll(exceptionSet);
		}
		return classExceptions.size();
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
