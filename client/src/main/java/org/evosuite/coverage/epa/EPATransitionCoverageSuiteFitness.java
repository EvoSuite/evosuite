package org.evosuite.coverage.epa;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.xml.parsers.ParserConfigurationException;

import org.evosuite.Properties;
import org.evosuite.TestGenerationContext;
import org.evosuite.testcase.ExecutableChromosome;
import org.evosuite.testcase.execution.EvosuiteError;
import org.evosuite.testcase.execution.ExecutionResult;
import org.evosuite.testcase.execution.TestCaseExecutor;
import org.evosuite.testsuite.AbstractTestSuiteChromosome;
import org.evosuite.testsuite.TestSuiteFitnessFunction;
import org.objectweb.asm.Type;
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

	private final Map<String, EPATransitionCoverageTestFitness> coverage_goal_map;

	public EPATransitionCoverageSuiteFitness(String epaXMLFilename) {
		if (epaXMLFilename == null) {
			throw new IllegalArgumentException("epa XML Filename cannot be null");
		}
		try {
			EPA target_epa = EPAFactory.buildEPA(epaXMLFilename);
			checkEPAStates(target_epa, Properties.TARGET_CLASS);
			checkEPAActionNames(target_epa, Properties.TARGET_CLASS);

			this.epa = target_epa;
			this.coverage_goal_map = buildCoverageGoalMap(epaXMLFilename);

			TestCaseExecutor.getInstance().addObserver(new EPATraceObserver());

		} catch (ParserConfigurationException | SAXException | IOException e) {
			throw new EvosuiteError(e);
		}
	}

	private static Map<String, EPATransitionCoverageTestFitness> buildCoverageGoalMap(String epaXMLFilename) {
		EPATransitionCoverageFactory goalFactory = new EPATransitionCoverageFactory(Properties.TARGET_CLASS,
				epaXMLFilename);
		Map<String, EPATransitionCoverageTestFitness> coverage_goal_map = new HashMap<String, EPATransitionCoverageTestFitness>();
		for (EPATransitionCoverageTestFitness goal : goalFactory.getCoverageGoals()) {
			coverage_goal_map.put(goal.getGoalName(), goal);
		}
		return coverage_goal_map;
	}

	private static void checkEPAStates(EPA epa, String className) {
		try {
			for (EPAState epaState : epa.getStates()) {
				Class<?> clazz = TestGenerationContext.getInstance().getClassLoaderForSUT().loadClass(className);
				if (!EPAUtils.epaStateMethodExists(epaState, clazz)) {
					throw new EvosuiteError("Boolean query method for EPA State " + epaState.getName()
							+ " was not found in target class " + className);
				}
			}
		} catch (ClassNotFoundException e) {
			throw new EvosuiteError(e);
		}
	}

	private static void checkEPAActionNames(EPA epa, String className) {
		try {
			for (EPATransition transition : epa.getTransitions()) {
				final String actionName = transition.getActionName();
				String[] parts = actionName.split("\\(");
				final String methodName = parts[0];
				final String methodSignature = "(" + parts[1];

				boolean found = hasMethodOrConstructor(className, methodName, methodSignature);
				if (!found) {
					throw new EvosuiteError(
							"EPA Action Name " + actionName + "was not found in target class " + className);
				}
			}
		} catch (ClassNotFoundException e) {
			throw new EvosuiteError(e);
		}
	}

	private static boolean hasMethodOrConstructor(String className, final String methodName,
			final String methodSignature) throws ClassNotFoundException {
		Class<?> clazz = TestGenerationContext.getInstance().getClassLoaderForSUT().loadClass(className);
		for (Method method : clazz.getDeclaredMethods()) {
			String methodDescriptor = Type.getMethodDescriptor(method);
			if ((methodDescriptor.equals(methodSignature)) && method.getName().equals(methodName)) {
				return true;
			}
		}
		for (Constructor<?> constructor : clazz.getDeclaredConstructors()) {
			String constructorDescriptor = Type.getConstructorDescriptor(constructor);
			if ((constructorDescriptor.equals(methodSignature)) && "<init>".equals(methodName)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Fitness value is the number of uncovered EPA transitions
	 */
	@Override
	public double getFitness(AbstractTestSuiteChromosome<? extends ExecutableChromosome> suite) {

		List<ExecutionResult> executionResults = runTestSuite(suite);
		List<EPATrace> epaTraces = new ArrayList<EPATrace>();
		for (ExecutionResult result : executionResults) {

			Collection<? extends EPATrace> newEpaTraces = result.getEPATraces();
			epaTraces.addAll(newEpaTraces);

			for (EPATrace trace : newEpaTraces) {
				for (EPATransition transition : trace.getEpaTransitions()) {
					String transitionName = transition.getTransitionName();
					if (!this.coverage_goal_map.containsKey(transitionName)) {
						logger.debug("goal for transition " + transition.toString() + " was not found!");
						break; // discard rest of trace
					}
					EPATransitionCoverageTestFitness goal = this.coverage_goal_map.get(transitionName);
					result.test.addCoveredGoal(goal);
				}
			}

		}

		final Set<EPATransition> tracedEpaTransitions = epaTraces.stream().map(EPATrace::getEpaTransitions)
				.flatMap(Collection::stream).collect(Collectors.toSet());

		final int epaTransitionSize = epa.getTransitions().size();
		final int covered_transitions = tracedEpaTransitions.size();
		final int uncovered_transitions = epaTransitionSize - covered_transitions;
		final double fitness = uncovered_transitions;

		updateIndividual(this, suite, fitness);
		double coverage = (double) covered_transitions / (double) epaTransitionSize;
		suite.setCoverage(this, coverage);

		suite.setNumOfCoveredGoals(this, covered_transitions);
		suite.setNumOfNotCoveredGoals(this, uncovered_transitions);
		return fitness;
	}

}
