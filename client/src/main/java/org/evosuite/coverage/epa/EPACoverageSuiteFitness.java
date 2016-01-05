package org.evosuite.coverage.epa;

import org.evosuite.testcase.ExecutableChromosome;
import org.evosuite.testcase.execution.EvosuiteError;
import org.evosuite.testcase.execution.ExecutionResult;
import org.evosuite.testcase.execution.MethodCall;
import org.evosuite.testsuite.AbstractTestSuiteChromosome;
import org.evosuite.testsuite.TestSuiteFitnessFunction;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.xml.parsers.ParserConfigurationException;

public class EPACoverageSuiteFitness extends TestSuiteFitnessFunction {

	/**
	 * 
	 */
	private static final long serialVersionUID = -9069407017356785315L;

	private final EPA epa;

	public EPACoverageSuiteFitness(String epaXMLFilename) {
		if (epaXMLFilename == null) {
			throw new IllegalArgumentException("epa XML Filename cannot be null");
		}
		try {
			this.epa = EPAFactory.buildEPA(epaXMLFilename);
		} catch (ParserConfigurationException | SAXException | IOException e) {
			throw new EvosuiteError(e);
		}
	}

	@Override
	public double getFitness(AbstractTestSuiteChromosome<? extends ExecutableChromosome> suite) {
		List<ExecutionResult> executionResults = runTestSuite(suite);
		final List<EPATrace> epaTraces = executionResults.stream().map(ExecutionResult::getTrace)
				.map(executionTrace -> new EPATrace(executionTrace, epa)).collect(Collectors.toList());

		return epa.getCoverage(epaTraces);
		//
		// int i = 0;
		// for (ExecutionResult executionResult : executionResults) {
		//
		// if (executionResult.getTrace().getMethodCalls().size() < 3)
		// continue;
		//
		// System.out.println("=========================================");
		// System.out.println("ExecutionResult #" + Integer.toString(i));
		// System.out.println("Code:");
		// System.out.println(executionResult.test);
		// System.out.println("-----------------------------------------");
		// for (MethodCall methodCallexecution :
		// executionResult.getTrace().getMethodCalls()) {
		// System.out.println("- " + methodCallexecution.className + "||" +
		// cleanMethodName(methodCallexecution.methodName));
		// }
		// System.out.println();
		// i++;
		// }
		// return 0;
	}

}
