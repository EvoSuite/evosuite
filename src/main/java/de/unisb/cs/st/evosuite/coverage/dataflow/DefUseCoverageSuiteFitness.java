/**
 * Copyright (C) 2011,2012 Gordon Fraser, Andrea Arcuri and EvoSuite contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Public License along with
 * EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package de.unisb.cs.st.evosuite.coverage.dataflow;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.unisb.cs.st.evosuite.coverage.dataflow.DefUseCoverageTestFitness.DefUsePairType;
import de.unisb.cs.st.evosuite.ga.Chromosome;
import de.unisb.cs.st.evosuite.testcase.ExecutionResult;
import de.unisb.cs.st.evosuite.testcase.TestChromosome;
import de.unisb.cs.st.evosuite.testsuite.TestSuiteChromosome;
import de.unisb.cs.st.evosuite.testsuite.TestSuiteFitnessFunction;

/**
 * Evaluate fitness of a test suite with respect to all of its def-use pairs
 * 
 * First simple and naive idea: Just take each DUGoal, calculate the minimal
 * fitness over all results in the suite once a goal is covered don't check for
 * it again in the end sum up all those fitness and that is s the resulting
 * suite-fitness
 * 
 * @author Andre Mis
 */
public class DefUseCoverageSuiteFitness extends TestSuiteFitnessFunction {
	private static final long serialVersionUID = 1L;

	static List<DefUseCoverageTestFitness> goals = DefUseCoverageFactory
			.getDUGoals();

	public static Map<DefUsePairType, Integer> totalGoals = initTotalGoals();
	public static Map<DefUsePairType, Integer> mostCoveredGoals = new HashMap<DefUsePairType, Integer>();

	public Map<DefUsePairType, Integer> coveredGoals = new HashMap<DefUsePairType, Integer>();

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.unisb.cs.st.evosuite.ga.FitnessFunction#getFitness(de.unisb.cs.st.
	 * evosuite.ga.Chromosome)
	 */
	@Override
	public double getFitness(Chromosome individual) {
		logger.trace("Calculating defuse fitness");

		TestSuiteChromosome suite = (TestSuiteChromosome) individual;
		List<ExecutionResult> results = runTestSuite(suite);
		double fitness = 0.0;

		Set<DefUseCoverageTestFitness> coveredGoalsSet = DefUseExecutionTraceAnalyzer
				.getCoveredGoals(results);

		initCoverageMaps();

		for (DefUseCoverageTestFitness goal : goals) {
			if (coveredGoalsSet.contains(goal))
				continue;

			double goalFitness = 2.0;
			for (ExecutionResult result : results) {
				TestChromosome tc = new TestChromosome();
				tc.setTestCase(result.test);
				double resultFitness = goal.getFitness(tc, result);
				if (resultFitness < goalFitness)
					goalFitness = resultFitness;
				if (goalFitness == 0.0) {
					result.test.addCoveredGoal(goal);
					coveredGoalsSet.add(goal);
					break;
				}
			}
			fitness += goalFitness;
		}

		countCoveredGoals(coveredGoalsSet);
		trackCoverageStatistics(suite);
		updateIndividual(individual, fitness);

		int coveredGoalCount = countCoveredGoals();
		int totalGoalCount = countTotalGoals();
		if (fitness == 0.0 && coveredGoalCount < totalGoalCount)
			throw new IllegalStateException("Fitness 0 implies 100% coverage "
					+ coveredGoalCount + " / " + totalGoals
					+ " (covered / total)");

		return fitness;
	}

	private static Map<DefUsePairType, Integer> initTotalGoals() {
		Map<DefUsePairType, Integer> r = new HashMap<DefUsePairType, Integer>();

		// init map
		for (DefUsePairType type : DefUseCoverageTestFitness.DefUsePairType
				.values())
			r.put(type, 0);

		// count total goals according to type
		for (DefUseCoverageTestFitness goal : goals)
			r.put(goal.getType(), r.get(goal.getType()) + 1);

		return r;
	}

	private void initCoverageMaps() {
		for (DefUsePairType type : DefUseCoverageTestFitness.DefUsePairType
				.values()) {
			coveredGoals.put(type, 0);
			if (mostCoveredGoals.get(type) == null)
				mostCoveredGoals.put(type, 0);
		}
	}

	private int countCoveredGoals() {
		return countGoalsIn(coveredGoals);
	}

	public static int countMostCoveredGoals() {
		return countGoalsIn(mostCoveredGoals);
	}

	private static int countTotalGoals() {
		return countGoalsIn(totalGoals);
	}

	private static int countGoalsIn(Map<DefUsePairType, Integer> goalMap) {
		int r = 0;
		for (DefUsePairType type : DefUseCoverageTestFitness.DefUsePairType
				.values()) {
			if(goalMap.get(type) != null)
				r += goalMap.get(type);
		}
		return r;
	}

	private void trackCoverageStatistics(TestSuiteChromosome suite) {

		setMostCovered();
		setSuiteCoverage(suite);
	}

	private void countCoveredGoals(
			Set<DefUseCoverageTestFitness> coveredGoalsSet) {
		for (DefUseCoverageTestFitness goal : coveredGoalsSet) {
			coveredGoals.put(goal.getType(),
					coveredGoals.get(goal.getType()) + 1);

		}
	}

	private void setSuiteCoverage(TestSuiteChromosome suite) {

		if (goals.size() > 0)
			suite.setCoverage(countCoveredGoals() / (double) goals.size());
		else
			suite.setCoverage(1.0);
	}

	private void setMostCovered() {

		for (DefUsePairType type : DefUseCoverageTestFitness.DefUsePairType
				.values()) {
			if (mostCoveredGoals.get(type) < coveredGoals.get(type)) {
				mostCoveredGoals.put(type, coveredGoals.get(type));
				if (mostCoveredGoals.get(type) > totalGoals.get(type))
					throw new IllegalStateException(
							"Can't cover more goals than there exist of type "
									+ type + " " + mostCoveredGoals.get(type)
									+ " / " + totalGoals.get(type)
									+ " (mostCovered / total)");
			}
		}
	}

	public static void printCoverage() {

		System.out.println("* Time spent optimizing covered goals analysis: "
				+ DefUseExecutionTraceAnalyzer.timeGetCoveredGoals + "ms");

		for (DefUsePairType type : DefUseCoverageTestFitness.DefUsePairType
				.values()) {
			System.out
					.println("* Covered goals of type " + type + ": "
							+ mostCoveredGoals.get(type) + " / "
							+ totalGoals.get(type));
		}

		System.out.println("* Covered " + countMostCoveredGoals() + "/"
				+ countTotalGoals() + " goals");
	}
}
