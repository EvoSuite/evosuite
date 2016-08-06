package org.evosuite.coverage.epa;

import org.evosuite.Properties;
import org.evosuite.TestGenerationContext;
import org.evosuite.testcase.ExecutableChromosome;
import org.evosuite.testcase.execution.EvosuiteError;
import org.evosuite.testcase.execution.ExecutionResult;
import org.evosuite.testsuite.AbstractTestSuiteChromosome;
import org.evosuite.testsuite.TestSuiteFitnessFunction;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by pantonio on 7/17/16.
 */
public class EPASuiteFitness extends TestSuiteFitnessFunction {
	/**
	 * 
	 */
	private static final long serialVersionUID = -9069407017356785315L;
	protected final EPA epa;
	protected final Map<String, EPATransitionCoverageTestFitness> coverage_goal_map;

	public EPASuiteFitness(String epaXMLFilename) {
		if (epaXMLFilename == null) {
			throw new IllegalArgumentException("epa XML Filename cannot be null");
		}
		try {
			EPA target_epa = EPAFactory.buildEPA(epaXMLFilename);
			checkEPAStates(target_epa, Properties.TARGET_CLASS);
			checkEPAActionNames(target_epa, Properties.TARGET_CLASS);

			this.epa = target_epa;
			this.coverage_goal_map = buildCoverageGoalMap(epaXMLFilename);

		} catch (ParserConfigurationException | SAXException | IOException e) {
			throw new EvosuiteError(e);
		}
	}

	protected static Map<String, EPATransitionCoverageTestFitness> buildCoverageGoalMap(String epaXMLFilename) {
		EPATransitionCoverageFactory goalFactory = new EPATransitionCoverageFactory(Properties.TARGET_CLASS,
				epaXMLFilename);
		Map<String, EPATransitionCoverageTestFitness> coverage_goal_map = new HashMap<String, EPATransitionCoverageTestFitness>();
		for (EPATransitionCoverageTestFitness goal : goalFactory.getCoverageGoals()) {
			coverage_goal_map.put(goal.getGoalName(), goal);
		}
		return coverage_goal_map;
	}

	protected static void checkEPAStates(EPA epa, String className) {
		try {
			for (EPAState epaState : epa.getStates()) {
				if (epa.getInitialState().equals(epaState)) {
					continue; // initial states are false by default
				}
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

	protected static void checkEPAActionNames(EPA epa, String className) {
		try {
			Class<?> clazz = TestGenerationContext.getInstance().getClassLoaderForSUT().loadClass(className);
			for (String actionName : epa.getActions()) {
				final boolean found = !EPAUtils.getEpaActionMethods(actionName, clazz).isEmpty()
						|| !EPAUtils.getEpaActionConstructors(actionName, clazz).isEmpty();
				if (!found) {
					throw new EvosuiteError(
							"EPA Action Name " + actionName + "was not found in target class " + className);
				}
			}
		} catch (ClassNotFoundException e) {
			throw new EvosuiteError(e);
		}
	}

	/**
	 * Fitness value is the number of uncovered EPA transitions
	 */
	@Override
	public final double getFitness(AbstractTestSuiteChromosome<? extends ExecutableChromosome> suite) {
		return getFitnessFromAnalysis(suite, getAnalysis(suite));
	}

	private double getFitnessFromAnalysis(AbstractTestSuiteChromosome<? extends ExecutableChromosome> suite, Map<String, Integer> analysis) {
		final Integer uncoveredTransitions = analysis.get("uncoveredTransitions");
		final Integer coveredTransitions = analysis.get("coveredTransitions");
		final double fitness = uncoveredTransitions;
		updateIndividual(this, suite, fitness);
		suite.setCoverage(this, coveredTransitions / analysis.get("epaTransitionSize"));

		suite.setNumOfCoveredGoals(this, coveredTransitions);
		suite.setNumOfNotCoveredGoals(this, uncoveredTransitions);
		return fitness;
	}

	private Map<String, Integer> getAnalysis(AbstractTestSuiteChromosome<? extends ExecutableChromosome> suite) {
		List<ExecutionResult> executionResults = runTestSuite(suite);
		List<EPATrace> epaTraces = new ArrayList<>();
		List<EPATransition> epaErrorTransitions = new ArrayList<>();
		for (ExecutionResult result : executionResults) {

			Collection<? extends EPATrace> newEpaTraces = result.getTrace().getEPATraces();
			epaErrorTransitions.addAll(result.getTrace().getEPAErrorTransitions());
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

		final Set<EPATransition> tracedEpaTransitions = epaTraces.stream()
				.map(EPATrace::getEpaTransitions)
				.flatMap(Collection::stream)
				.collect(Collectors.toSet());

		final int covered_transitions = tracedEpaTransitions.size();
		
		final Map<String, Integer> epaTransitionsAnalysis = new HashMap<>();
		epaTransitionsAnalysis.put("epaTransitionSize", epa.getTransitions().size());
		epaTransitionsAnalysis.put("coveredTransitions", covered_transitions);
		epaTransitionsAnalysis.put("errorTransitions", epaErrorTransitions.size());
		epaTransitionsAnalysis.put("uncoveredTransitions", epa.getTransitions().size() - covered_transitions);
		return epaTransitionsAnalysis;
	}
}
