package org.evosuite.coverage.epa;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.xml.parsers.ParserConfigurationException;

import org.evosuite.testcase.ExecutableChromosome;
import org.evosuite.testcase.execution.EvosuiteError;
import org.evosuite.testcase.execution.ExecutionResult;
import org.evosuite.testsuite.AbstractTestSuiteChromosome;
import org.evosuite.testsuite.TestSuiteFitnessFunction;
import org.xml.sax.SAXException;

/**
 * This fitness function counts the degree of covered transitions. It is a
 * minimization function (less is better). The value 0.0 means all transitions
 * were covered.
 * 
 * @author galeotti
 *
 */
public class EPATransitionCoverageSuiteFitness extends TestSuiteFitnessFunction {

	/**
	 * 
	 */
	private static final long serialVersionUID = -9069407017356785315L;

	private final EPA epa;

	public EPATransitionCoverageSuiteFitness(String epaXMLFilename) {
		if (epaXMLFilename == null) {
			throw new IllegalArgumentException("epa XML Filename cannot be null");
		}
		try {
			this.epa = EPAFactory.buildEPA(epaXMLFilename);
		} catch (ParserConfigurationException | SAXException | IOException e) {
			throw new EvosuiteError(e);
		}
	}

	/**
	 * Fitness value is the number of uncovered EPA transitions
	 */
	@Override
	public double getFitness(AbstractTestSuiteChromosome<? extends ExecutableChromosome> suite) {
		List<ExecutionResult> executionResults = runTestSuite(suite);

		final List<EPATrace> epaTraces = executionResults.stream().map(ExecutionResult::getTrace)
				.map(executionTrace -> new EPATrace(executionTrace, epa)).collect(Collectors.toList());

		final Set<EPATransition> tracedEpaTransitions = epaTraces.stream().map(EPATrace::getEpaTransitions)
				.flatMap(Collection::stream).collect(Collectors.toSet());

		final int epaTransitionSize = epaTraces.size();
		final int tracedEpaTransitionSize = tracedEpaTransitions.size();
		final int uncoveredEpaTransitions = epaTransitionSize - tracedEpaTransitionSize;
		final double fitness = uncoveredEpaTransitions;
		return fitness;
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
