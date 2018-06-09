/**
 * Copyright (C) 2010-2018 Gordon Fraser, Andrea Arcuri and EvoSuite
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
package org.evosuite.ga.metaheuristics.mosa;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import org.evosuite.Properties;
import org.evosuite.ga.Chromosome;
import org.evosuite.ga.ChromosomeFactory;
import org.evosuite.ga.FitnessFunction;
import org.evosuite.ga.comparators.OnlyCrowdingComparator;
import org.evosuite.ga.metaheuristics.mosa.structural.MultiCriteriatManager;
import org.evosuite.ga.metaheuristics.mosa.structural.StructuralGoalManager;
import org.evosuite.ga.operators.ranking.CrowdingDistance;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.evosuite.testsuite.TestSuiteFitnessFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of the DynaMOSA (Many Objective Sorting Algorithm) described in the paper
 * "Automated Test Case Generation as a Many-Objective Optimisation Problem with Dynamic Selection
 * of the Targets".
 * 
 * @author Annibale Panichella, Fitsum M. Kifetew, Paolo Tonella
 */
public class DynaMOSA<T extends Chromosome> extends AbstractMOSA<T> {

	private static final long serialVersionUID = 146182080947267628L;

	private static final Logger logger = LoggerFactory.getLogger(DynaMOSA.class);

	/** Manager to determine the test goals to consider at each generation */
	protected StructuralGoalManager<T> goalsManager = null;

	protected CrowdingDistance<T> distance = new CrowdingDistance<T>();

	/**
	 * Constructor based on the abstract class {@link AbstractMOSA}.
	 * 
	 * @param factory
	 */
	public DynaMOSA(ChromosomeFactory<T> factory) {
		super(factory);
	}

	/** {@inheritDoc} */
	@Override
	protected void evolve() {
		List<T> offspringPopulation = this.breedNextGeneration();

		// Create the union of parents and offSpring
		List<T> union = new ArrayList<T>(this.population.size() + offspringPopulation.size());
		union.addAll(this.population);
		union.addAll(offspringPopulation);

		// Ranking the union
		logger.debug("Union Size = {}", union.size());

		// Ranking the union using the best rank algorithm (modified version of the non dominated sorting algorithm
		this.rankingFunction.computeRankingAssignment(union, this.goalsManager.getCurrentGoals());

		// let's form the next population using "preference sorting and non-dominated sorting" on the
		// updated set of goals
		int remain = Math.max(Properties.POPULATION, this.rankingFunction.getSubfront(0).size());
		int index = 0;
		List<T> front = null;
		this.population.clear();

		// Obtain the next front
		front = this.rankingFunction.getSubfront(index);

		while ((remain > 0) && (remain >= front.size()) && !front.isEmpty()) {
			// Assign crowding distance to individuals
			this.distance.fastEpsilonDominanceAssignment(front, this.goalsManager.getCurrentGoals());

			// Add the individuals of this front
			this.population.addAll(front);

			// Decrement remain
			remain = remain - front.size();

			// Obtain the next front
			index++;
			if (remain > 0) {
				front = this.rankingFunction.getSubfront(index);
			}
		}

		// Remain is less than front(index).size, insert only the best one
		if (remain > 0 && !front.isEmpty()) { // front contains individuals to insert
			this.distance.fastEpsilonDominanceAssignment(front, this.goalsManager.getCurrentGoals());
			Collections.sort(front, new OnlyCrowdingComparator());
			for (int k = 0; k < remain; k++) {
				this.population.add(front.get(k));
			}

			remain = 0;
		}

		this.currentIteration++;
		//logger.debug("N. fronts = {}", ranking.getNumberOfSubfronts());
		//logger.debug("1* front size = {}", ranking.getSubfront(0).size());
		logger.debug("Covered goals = {}", goalsManager.getCoveredGoals().size());
		logger.debug("Current goals = {}", goalsManager.getCurrentGoals().size());
		logger.debug("Uncovered goals = {}", goalsManager.getUncoveredGoals().size());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void generateSolution() {
		logger.debug("executing generateSolution function");

		this.goalsManager = new MultiCriteriatManager<T>(this.fitnessFunctions);

		logger.debug("Initial Number of Goals = " + this.goalsManager.getCurrentGoals().size());

		//initialize population
		if (this.population.isEmpty()) {
			this.initializePopulation();
		}

		// update current goals
		this.calculateFitness();

		// Calculate dominance ranks and crowding distance
		this.rankingFunction.computeRankingAssignment(this.population, this.goalsManager.getCurrentGoals());

		for (int i = 0; i < this.rankingFunction.getNumberOfSubfronts(); i++){
			this.distance.fastEpsilonDominanceAssignment(this.rankingFunction.getSubfront(i), this.goalsManager.getCurrentGoals());
		}
		// next generations
		while (!isFinished() && this.goalsManager.getUncoveredGoals().size() > 0) {
			this.evolve();
			this.notifyIteration();
		}

		this.notifySearchFinished();
	}

	/** 
	 * {@inheritDoc}
	 */
	@Override
	protected Set<FitnessFunction<T>> getCoveredGoals() {
		return this.goalsManager.getCoveredGoals().keySet();
	}

	/** 
	 * {@inheritDoc}
	 */
	@Override
	protected int getNumberOfCoveredGoals() {
		return this.getCoveredGoals().size();
	}

	/** 
	 * {@inheritDoc}
	 */
	protected Set<FitnessFunction<T>> getUncoveredGoals() {
		return this.goalsManager.getUncoveredGoals();
	}

	/** 
	 * {@inheritDoc}
	 */
	@Override
	protected int getNumberOfUncoveredGoals() {
		return this.getUncoveredGoals().size();
	}

	/** 
	 * {@inheritDoc}
	 */
	@Override
	protected int getTotalNumberOfGoals() {
		return this.getNumberOfCoveredGoals() + this.getNumberOfUncoveredGoals();
	}

	/** 
	 * {@inheritDoc}
	 */
	@Override
	protected List<T> getSolutions() {
		List<T> suite = new ArrayList<T>(this.goalsManager.getArchive());
		return suite;
	}

	/** 
	 * {@inheritDoc}
	 */
	@Override
	protected TestSuiteChromosome generateSuite() {
		TestSuiteChromosome suite = new TestSuiteChromosome();
		for (T t : this.getSolutions()) {
			TestChromosome test = (TestChromosome) t;
			suite.addTest(test);
		}
		return suite;
	}

	/** 
	 * {@inheritDoc}
	 */
	@Override
	protected void calculateFitness(T c) {
		this.goalsManager.calculateFitness(c);
		this.notifyEvaluation(c);
	}

	/** 
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<T> getBestIndividuals() {
		TestSuiteChromosome bestTestCases = this.generateSuite();

		if (bestTestCases.getTestChromosomes().isEmpty()) {
			// trivial case where there are no branches to cover or the archive is empty
			for (T test : this.population) {
				bestTestCases.addTest((TestChromosome) test);
			}
		}

		// compute overall fitness and coverage
		this.computeCoverageAndFitness(bestTestCases);

		List<T> bests = new ArrayList<T>(1);
		bests.add((T) bestTestCases);

		return bests;
	}

	/** 
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	@Override
	public T getBestIndividual() {
		TestSuiteChromosome best = this.generateSuite();
		if (best.getTestChromosomes().isEmpty()) {
			for (T test : this.population) {
				best.addTest((TestChromosome) test);
			}
			for (TestSuiteFitnessFunction suiteFitness : this.suiteFitnessFunctions.keySet()) {
				best.setCoverage(suiteFitness, 0.0);
				best.setFitness(suiteFitness,  1.0);
			}
			return (T) best;
		}

		// compute overall fitness and coverage
		this.computeCoverageAndFitness(best);

		return (T) best;
	}

	/** 
	 * {@inheritDoc}
	 */
	@Override
	protected void computeCoverageAndFitness(TestSuiteChromosome suite) {
		for (Entry<TestSuiteFitnessFunction, Class<?>> entry : this.suiteFitnessFunctions.entrySet()) {
			TestSuiteFitnessFunction suiteFitnessFunction = entry.getKey();
			Class<?> testFitnessFunction = entry.getValue();

			int numberCoveredTargets = this.goalsManager.getNumberOfCoveredTargets(testFitnessFunction);
			int numberUncoveredTargets = this.goalsManager.getNumberOfUncoveredTargets(testFitnessFunction);
			int totalNumberTargets = numberCoveredTargets + numberUncoveredTargets;

			double coverage = totalNumberTargets == 0 ? 0.0
			    : ((double) numberCoveredTargets)
			    / ((double) (numberCoveredTargets + numberUncoveredTargets));

			suite.setFitness(suiteFitnessFunction, ((double) numberUncoveredTargets));
			suite.setCoverage(suiteFitnessFunction, coverage);
			suite.setNumOfCoveredGoals(suiteFitnessFunction, numberCoveredTargets);
			suite.setNumOfNotCoveredGoals(suiteFitnessFunction, numberUncoveredTargets);
		}
	}

}
