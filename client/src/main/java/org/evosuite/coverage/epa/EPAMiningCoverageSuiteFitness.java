/**
 * Copyright (C) 2010-2016 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3.0 of the License, or
 * (at your option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.coverage.epa;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.evosuite.Properties;
import org.evosuite.TestGenerationContext;
import org.evosuite.coverage.exception.ExceptionCoverageFactory;
import org.evosuite.ga.archive.Archive;
import org.evosuite.testcase.ExecutableChromosome;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.execution.EvosuiteError;
import org.evosuite.testcase.execution.ExecutionResult;
import org.evosuite.testsuite.AbstractTestSuiteChromosome;
import org.evosuite.testsuite.TestSuiteFitnessFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EPAMiningCoverageSuiteFitness extends TestSuiteFitnessFunction {

	private static final long serialVersionUID = 1565793073526627496L;

	private static Logger logger = LoggerFactory.getLogger(EPAMiningCoverageSuiteFitness.class);

	private static int maxEPAMiningGoalsCovered = 0;

	public EPAMiningCoverageSuiteFitness() {

	}

	@Override
	public double getFitness(AbstractTestSuiteChromosome<? extends ExecutableChromosome> suite) {
		logger.trace("Calculating EPA Mining fitness");

		Class<?> targetClass;
		try {
			targetClass = TestGenerationContext.getInstance().getClassLoaderForSUT().loadClass(Properties.TARGET_CLASS);
		} catch (ClassNotFoundException e) {
			throw new EvosuiteError(e);
		}

		Map<String, Set<Constructor<?>>> actionConstructorMap = EPAUtils.getEpaActionConstructors(targetClass);
		Map<String, Set<Method>> actionMethodsMap = EPAUtils.getEpaActionMethods(targetClass);
		Map<String, Method> preconditionMethodsMap = EPAUtils.getEpaActionPreconditionMethods(targetClass);

		checkActionAndPreconditionsAnnotations(actionMethodsMap, preconditionMethodsMap);

		Set<String> actionIds = new HashSet<String>(actionMethodsMap.keySet());
		actionIds.addAll(actionConstructorMap.keySet());

		int numberOfAutomataActions = actionIds.size();
		int maxNumberOfAutomataStates = (int) Math.pow(2, numberOfAutomataActions);
		int maxNumberOfAutomataTransitions = maxNumberOfAutomataStates * numberOfAutomataActions
				* maxNumberOfAutomataStates;

		List<ExecutionResult> results = runTestSuite(suite);
		EPAMiningCoverageSuiteFitness contextFitness = this;
		Set<EPAMiningCoverageTestFitness> goalsCoveredByResult = calculateEPAMiningInfo(results, contextFitness);

		if (Properties.TEST_ARCHIVE) {
			// If we are using the archive, then fitness is by definition 0
			// as all assertions already covered are in the archive
			suite.setFitness(this, 0.0);
			suite.setCoverage(this, 1.0);
			maxEPAMiningGoalsCovered = EPAMiningCoverageFactory.getGoals().size();
			return 0.0;
		}

		int numCoveredGoals = goalsCoveredByResult.size();
		int numUncoveredGoals = 0;
		for (EPAMiningCoverageTestFitness knownCoverageGoal : EPAMiningCoverageFactory.getGoals().values()) {
			if (!goalsCoveredByResult.contains(knownCoverageGoal)) {
				numUncoveredGoals++;
			}
		}

		if (numCoveredGoals > maxEPAMiningGoalsCovered) {
			logger.info("(Exceptions) Best individual covers " + numCoveredGoals + " transitions");
			maxEPAMiningGoalsCovered = numCoveredGoals;
		}

		// We cannot set a coverage here, as it does not make any sense
		// suite.setCoverage(this, 1.0);
		double epaMiningCoverageFitness = maxNumberOfAutomataTransitions - numCoveredGoals;

		suite.setFitness(this, epaMiningCoverageFitness);
		if (maxEPAMiningGoalsCovered > 0)
			suite.setCoverage(this, numCoveredGoals / maxEPAMiningGoalsCovered);
		else
			suite.setCoverage(this, 1.0);

		suite.setNumOfCoveredGoals(this, numCoveredGoals);
		suite.setNumOfNotCoveredGoals(this, numUncoveredGoals);
		return epaMiningCoverageFitness;
	}

	private static Set<EPAMiningCoverageTestFitness> calculateEPAMiningInfo(List<ExecutionResult> results,
			EPAMiningCoverageSuiteFitness contextFitness) {

		Set<EPAMiningCoverageTestFitness> goalsCoveredByResults = new HashSet<EPAMiningCoverageTestFitness>();

		for (ExecutionResult result : results) {

			TestChromosome test = new TestChromosome();
			test.setTestCase(result.test);
			test.setLastExecutionResult(result);
			test.setChanged(false);

			for (EPATrace epa_trace : result.getTrace().getEPATraces()) {


				for (EPATransition epa_transition : epa_trace.getEpaTransitions()) {
					if (epa_transition.getDestinationState().equals(EPAState.INVALID_OBJECT_STATE)) {
						break;
					}
					if (epa_transition instanceof EPAExceptionalTransition) {
						break;
					}

					EPAMiningCoverageTestFitness goal = new EPAMiningCoverageTestFitness(Properties.TARGET_CLASS,
							epa_transition.getOriginState(), epa_transition.getActionName(),
							epa_transition.getDestinationState());

					if (!goalsCoveredByResults.contains(goal)) {
						goalsCoveredByResults.add(goal);
					}

					String key = goal.getKey();

					if (!EPAMiningCoverageFactory.getGoals().containsKey(key)) {
						EPAMiningCoverageFactory.getGoals().put(key, goal);
						test.getTestCase().addCoveredGoal(goal);
						if (Properties.TEST_ARCHIVE && contextFitness != null) {
							Archive.getArchiveInstance().addTarget(goal);
							Archive.getArchiveInstance().updateArchive(goal, test, 0.0);
						}
					}
				}
			}
		}

		return goalsCoveredByResults;
	}

	private static void checkActionAndPreconditionsAnnotations(Map<String, Set<Method>> actionMethodsMap,
			Map<String, Method> preconditionMethodsMap) throws EvosuiteError {
		for (String actionId : actionMethodsMap.keySet()) {
			if (!preconditionMethodsMap.containsKey(actionId)) {
				throw new EvosuiteError("@EpaActionPrecondition annotation missing for action " + actionId);
			}
		}

		for (String actionId : preconditionMethodsMap.keySet()) {
			if (!actionMethodsMap.containsKey(actionId)) {
				throw new EvosuiteError("Missing @EpaAction annotation for action " + actionId);
			}
		}
	}
}
