/*
 * Copyright (C) 2010 Saarland University
 * 
 * This file is part of EvoSuite.
 * 
 * EvoSuite is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * EvoSuite is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser Public License
 * along with EvoSuite.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.unisb.cs.st.evosuite.coverage.dataflow;

import java.util.List;
import java.util.Set;

import de.unisb.cs.st.evosuite.ga.Chromosome;
import de.unisb.cs.st.evosuite.testcase.ExecutionResult;
import de.unisb.cs.st.evosuite.testcase.TestChromosome;
import de.unisb.cs.st.evosuite.testsuite.TestSuiteChromosome;
import de.unisb.cs.st.evosuite.testsuite.TestSuiteFitnessFunction;

/**
 * Evaluate fitness of a test suite with respect to all of its def-use pairs
 * 
 * @author
 * 
 */
public class AllDefsCoverageSuiteFitness extends TestSuiteFitnessFunction {
	private static final long serialVersionUID = 1L;

	static List<DefUseCoverageTestFitness> goals = DefUseCoverageFactory
			.getDUGoals();

	public static int totalGoals = goals.size();
	public static int totalParamGoals = DefUseCoverageFactory
			.getParamGoalsCount();
	public static int totalIntraGoals = DefUseCoverageFactory
			.getIntraGoalsCount();
	public static int totalInterGoals = DefUseCoverageFactory
			.getInterGoalsCount();

	public static int mostCoveredGoals = 0;
	public static int mostParamGoals = 0;
	public static int mostIntraGoals = 0;
	public static int mostInterGoals = 0;

	public int coveredGoalsTotal;
	public int coveredParamGoals;
	public int coveredIntraGoals;
	public int coveredInterGoals;

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

		// first simple and naive idea:
		// just take each DUGoal, calculate the minimal fitness over all results
		// in the suite
		// once a goal is covered don't check for it again
		// in the end sum up all those fitness and it's the resulting
		// suite-fitness

		// did an experiment here: subject ncs.Bessj
		// so as it turns out the getCoveredGoals() might have to run through up
		// to 50k entry big duTraces and thus take up another 20-25% memory, but
		// if you disable this you have to take more time in single fitness
		// calculations and gain worse coverage / less statements per time
		// so for ncs.Bessj with this disabled:

		/*
		 * ida@ubuntu:~/Bachelor/again/evosuite/examples/ncs$ ^C
		 * ida@ubuntu:~/Bachelor/again/evosuite/examples/ncs$ ../../EvoSuite
		 * -generateSuite -criterion defuse -class ncs.Bessj Generating tests
		 * for class ncs.Bessj Test criterion: All DU Pairs Setting up search
		 * algorithm for whole suite generation Goal computation took: 41ms
		 * Starting evolution Alternative fitness calculation disabled!
		 * [Progress:=====> 19%] [Cov:========================> 71%] Search
		 * finished after 600s and 6 generations, 19420 statements, best
		 * individual has fitness 46.49999999592326 Minimizing result Generated
		 * 4 tests with total length 17 Resulting TestSuite's coverage: 71%
		 * GA-Budget: - ShutdownTestWriter : 0 / 0 - GlobalTime : 602 / 600
		 * Finished! - ZeroFitness : 46 / 0 - MaxStatements : 20.881 / 100.000
		 * Covered 92/129 goals Time spent optimizing covered goals analysis:
		 * 0ms Time spent executing tests: 59980ms Writing JUnit test cases to
		 * evosuite-tests/DEFUSE Time spent calculating single fitnesses:
		 * 540751ms Done!
		 */

		// and enabled:
		/*
		 * ida@ubuntu:~/Bachelor/again/evosuite/examples/ncs$ ../../EvoSuite
		 * -generateSuite -criterion defuse -class ncs.Bessj Generating tests
		 * for class ncs.Bessj Test criterion: All DU Pairs Setting up search
		 * algorithm for whole suite generation Goal computation took: 42ms
		 * Starting evolution Alternative fitness calculation disabled!
		 * [Progress:=======> 25%] [Cov:========================> 71%] Search
		 * finished after 600s and 11 generations, 25732 statements, best
		 * individual has fitness 46.49999999553073 Minimizing result Generated
		 * 4 tests with total length 19 Resulting TestSuite's coverage: 71%
		 * GA-Budget: - ShutdownTestWriter : 0 / 0 - MaxStatements : 28.030 /
		 * 100.000 - ZeroFitness : 46 / 0 - GlobalTime : 604 / 600 Finished!
		 * Covered 92/129 goals Time spent optimizing covered goals analysis:
		 * 414365ms Time spent executing tests: 87669ms Writing JUnit test cases
		 * to evosuite-tests/DEFUSE Time spent calculating single fitnesses:
		 * 100324ms Done!
		 */

		// so we have 25% more executed statements, which means this will stay
		// enabled

		Set<DefUseCoverageTestFitness> coveredGoals = DefUseExecutionTraceAnalyzer
				.getCoveredGoals(results);
		// Set<DefUseCoverageTestFitness> coveredGoals = new
		// HashSet<DefUseCoverageTestFitness>();

		if (coveredGoals.size() > totalGoals)
			throw new IllegalStateException(
					"cant cover more goals than there are: "
							+ coveredGoals.size());

		for (DefUseCoverageTestFitness goal : goals) {
			if (coveredGoals.contains(goal))
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
					// System.out.println(goal.toString());
					// System.out.println(result.test.toCode());
					// System.out.println(resultFitness);
					coveredGoals.add(goal);
					break;
				}
			}
			fitness += goalFitness;
		}

		trackCoverageStatistics(coveredGoals, suite);
		updateIndividual(individual, fitness);

		if (fitness == 0.0 && coveredGoalsTotal < totalGoals)
			throw new IllegalStateException("Fitness 0 implies 100% coverage "
					+ coveredGoalsTotal + " / " + totalGoals);

		return fitness;
	}

	private void trackCoverageStatistics(
			Set<DefUseCoverageTestFitness> coveredGoals,
			TestSuiteChromosome suite) {

		countDUGoalKinds(coveredGoals);
		setSuiteCoverage(suite, coveredGoals);
	}

	private void countDUGoalKinds(Set<DefUseCoverageTestFitness> coveredGoals) {

		coveredParamGoals = 0;
		coveredIntraGoals = 0;
		coveredInterGoals = 0;

		for (DefUseCoverageTestFitness goal : coveredGoals) {
			if (goal.isInterMethodPair())
				coveredInterGoals++;
			else if (goal.isParameterGoal())
				coveredParamGoals++;
			else
				coveredIntraGoals++;
		}

		coveredGoalsTotal = coveredGoals.size();

		setMostCovered();
	}

	private void setSuiteCoverage(TestSuiteChromosome suite,
			Set<DefUseCoverageTestFitness> coveredGoals) {

		if (goals.size() > 0)
			suite.setCoverage(coveredGoals.size() / (double) goals.size());
		else
			suite.setCoverage(1.0);
	}

	private void setMostCovered() {

		if (mostCoveredGoals < coveredGoalsTotal) {
			mostCoveredGoals = coveredGoalsTotal;
			if (mostCoveredGoals > totalGoals)
				throw new IllegalStateException(
						"can't cover more goals than there exist");
		}
		if (mostParamGoals < coveredParamGoals) {
			mostParamGoals = coveredParamGoals;
			if (mostParamGoals > totalParamGoals)
				throw new IllegalStateException(
						"can't cover more param goals than there exist");
		}
		if (mostIntraGoals < coveredIntraGoals) {
			mostIntraGoals = coveredIntraGoals;
			if (mostIntraGoals > totalIntraGoals)
				throw new IllegalStateException(
						"can't cover more Intra goals than there exist");
		}
		if (mostInterGoals < coveredInterGoals) {
			mostInterGoals = coveredInterGoals;
			if (mostInterGoals > totalInterGoals)
				throw new IllegalStateException(
						"can't cover more Inter goals than there exist");
		}
	}

	public static void printCoverage() {
		
		System.out.println("* Covered parameter goals: "+mostParamGoals+" / "+totalParamGoals);
		System.out.println("* Covered intra goals: "+mostIntraGoals+" / "+totalIntraGoals);
		System.out.println("* Covered inter goals: "+mostInterGoals+" / "+totalInterGoals);
		
		System.out.println("* Covered " + mostCoveredGoals + "/" + totalGoals + " goals");
		System.out.println("* Time spent optimizing covered goals analysis: "
		        + DefUseExecutionTraceAnalyzer.timeGetCoveredGoals + "ms");
	}
}
