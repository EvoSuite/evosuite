/**
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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.evosuite.Properties;
import org.evosuite.Properties.Criterion;
import org.evosuite.coverage.exception.ExceptionCoverageFactory;
import org.evosuite.coverage.exception.ExceptionCoverageTestFitness;
import org.evosuite.ga.Chromosome;
import org.evosuite.ga.ChromosomeFactory;
import org.evosuite.ga.FitnessFunction;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.ga.metaheuristics.lips.BudgetConsumptionMonitor;
import org.evosuite.ga.metaheuristics.mosa.comparators.OnlyCrowdingComparator;
import org.evosuite.rmi.ClientServices;
import org.evosuite.statistics.RuntimeVariable;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.TestFitnessFunction;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.evosuite.testsuite.TestSuiteFitnessFunction;
import org.evosuite.utils.ArrayUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of the MOSA (Many-Objective Sorting Algorithm) described in the ICST'15 paper ...
 * 
 * @author Annibale Panichella, Fitsum M. Kifetew
 *
 * @param <T>
 */
public class MOSA<T extends Chromosome> extends AbstractMOSA<T> {

	private static final long serialVersionUID = 146182080947267628L;

	private static final Logger logger = LoggerFactory.getLogger(MOSA.class);

	/** Map used to store the covered test goals (keys of the map) and the corresponding covering test cases (values of the map) **/
	protected Map<FitnessFunction<T>, T> archive = new LinkedHashMap<FitnessFunction<T>, T>();

	/** Boolean vector to indicate whether each test goal is covered or not. **/
	protected Set<FitnessFunction<T>> uncoveredGoals = new LinkedHashSet<FitnessFunction<T>>();

	/** Crowding distance measure to use */
	protected CrowdingDistance<T> distance = new CrowdingDistance<T>();
	
	/** Object used to keep track of the execution time needed to reach the maximum coverage */
	protected BudgetConsumptionMonitor budgetMonitor;
	
	/**
	 * Constructor based on the abstract class {@link AbstractMOSA}
	 * @param factory
	 */
	public MOSA(ChromosomeFactory<T> factory) {
		super(factory);
		budgetMonitor = new BudgetConsumptionMonitor();
	}

	/** {@inheritDoc} */
	@Override
	protected void evolve() {
		List<T> offspringPopulation = breedNextGeneration();

		// Create the union of parents and offSpring
		List<T> union = new ArrayList<T>();
		union.addAll(population);
		union.addAll(offspringPopulation);

		// Ranking the union
		logger.debug("Union Size =" + union.size());
		// Ranking the union using the best rank algorithm (modified version of the non dominated sorting algorithm
		ranking.computeRankingAssignment(union, uncoveredGoals);

		// add to the archive the new covered goals (and the corresponding test cases)
		//this.archive.putAll(ranking.getNewCoveredGoals());

		int remain = population.size();
		int index = 0;
		List<T> front = null;
		population.clear();

		// Obtain the next front
		front = ranking.getSubfront(index);

		while ((remain > 0) && (remain >= front.size()) && !front.isEmpty()) {
			// Assign crowding distance to individuals
			distance.fastEpsilonDominanceAssignment(front, uncoveredGoals);
			// Add the individuals of this front
			population.addAll(front);

			// Decrement remain
			remain = remain - front.size();

			// Obtain the next front
			index++;
			if (remain > 0) {
				front = ranking.getSubfront(index);
			} // if
		} // while

		// Remain is less than front(index).size, insert only the best one
		if (remain > 0 && !front.isEmpty()) { // front contains individuals to insert
			distance.fastEpsilonDominanceAssignment(front, uncoveredGoals);
			Collections.sort(front, new OnlyCrowdingComparator());
			for (int k = 0; k < remain; k++) {
				population.add(front.get(k));
			} // for

			remain = 0;
		} // if
		currentIteration++;
		//logger.error("");
		//logger.error("N. fronts = "+ranking.getNumberOfSubfronts());
		//logger.debug("1* front size = "+ranking.getSubfront(0).size());
		//logger.debug("2* front size = "+ranking.getSubfront(1).size());
		//logger.error("Covered goals = "+this.archive.size());
		//logger.error("Uncovered goals = "+uncoveredGoals.size());
		//logger.debug("Generation=" + currentIteration + " Population Size=" + population.size() + " Archive size=" + archive.size());
	}



	/** {@inheritDoc} */
	@Override
	@SuppressWarnings("unchecked")
	protected void calculateFitness(T c) {
		for (FitnessFunction<T> fitnessFunction : this.fitnessFunctions) {
			double value = fitnessFunction.getFitness(c);
			if (value == 0.0) {
				//((TestChromosome)c).addCoveredGoals(fitnessFunction);
				updateArchive(c, fitnessFunction);
			}
		}
		if (ArrayUtil.contains(Properties.CRITERION, Criterion.EXCEPTION)){
			// if one of the coverage criterion is Criterion.EXCEPTION,
			// then we have to analyze the results of the execution do look
			// for generated exceptions
			List<ExceptionCoverageTestFitness> list = deriveCoveredExceptions(c);
			for (ExceptionCoverageTestFitness exp : list){
				// new covered exceptions (goals) have to be added to the archive
				updateArchive(c, (FitnessFunction<T>) exp);
				if (!fitnessFunctions.contains(exp)){
					// let's update the list of fitness functions 
					this.fitnessFunctions.add((FitnessFunction<T>) exp);
					// let's update the newly discovered exceptions to ExceptionCoverageFactory 
					ExceptionCoverageFactory.getGoals().put(exp.toString(), exp);
				}
			}
		}
		notifyEvaluation(c);
		// update the time needed to reach the max coverage
		budgetMonitor.checkMaxCoverage(this.archive.keySet().size());
	}

	/** {@inheritDoc} */
	@Override
	public void generateSolution() {
		logger.info("executing generateSolution function");

		// keep track of covered goals
		for (FitnessFunction<T> goal : fitnessFunctions) {
			uncoveredGoals.add(goal);
		}

		//initialize population
		if (population.isEmpty())
			initializePopulation();

		// Calculate dominance ranks and crowding distance
		ranking.computeRankingAssignment(population, this.uncoveredGoals);
		for (int i = 0; i<ranking.getNumberOfSubfronts(); i++){
			distance.fastEpsilonDominanceAssignment(ranking.getSubfront(i), this.uncoveredGoals);
		}

		// TODO add here dynamic stopping condition

		while (!isFinished() && this.getNumberOfCoveredGoals()<this.fitnessFunctions.size()) {
			evolve();
			notifyIteration();
		}

		// storing the time needed to reach the maximum coverage
		ClientServices.getInstance().getClientNode().trackOutputVariable(RuntimeVariable.Time2MaxCoverage, this.budgetMonitor.getTime2MaxCoverage());		
		notifySearchFinished();
	}

	/** This method is used to print the number of test goals covered by the test cases stored in the current archive **/
	private int getNumberOfCoveredGoals() {
		int n_covered_goals = this.archive.keySet().size();
		logger.debug("# Covered Goals = " + n_covered_goals);
		return n_covered_goals;
	}

	/** This method return the test goals covered by the test cases stored in the current archive **/
	public Set<FitnessFunction<T>> getCoveredGoals() {
		return this.archive.keySet();
	}

	/**
	 * This method update the archive by adding test cases that cover new test goals, or replacing the
	 * old tests if the new ones are smaller (at the same level of coverage).
	 * 
	 * @param solutionSet is the list of Chromosomes (population)
	 */
	private void updateArchive(T solution, FitnessFunction<T> covered) {
		// the next two lines are needed since that coverage information are used
		// during EvoSuite post-processing
		TestChromosome tch = (TestChromosome) solution;
		tch.getTestCase().getCoveredGoals().add((TestFitnessFunction) covered);

		// store the test cases that are optimal for the test goal in the
		// archive
		if (archive.containsKey(covered)) {
			TestChromosome existingSolution = (TestChromosome) this.archive.get(covered);
			// if the new solution is better (based on secondary criterion), then the archive must be updated
			if (solution.compareSecondaryObjective(existingSolution) < 0) {
				this.archive.put(covered, solution);
			}
		} else {
			archive.put(covered, solution);
			this.uncoveredGoals.remove(covered);
		}
	}

	protected List<T> getArchive() {
		return new ArrayList<T>(new LinkedHashSet<T>(this.archive.values()));
	}

	protected List<T> getFinalTestSuite() {
		// trivial case where there are no branches to cover or the archive is empty
		if (this.getNumberOfCoveredGoals()==0) {
			return getArchive();
		}
		if (archive.size() == 0) {
			if (population.size() > 0) {
				return Arrays.asList(this.population.get(this.population.size() - 1));
			} else {
				return getArchive();
			}
		}
		List<T> final_tests = getArchive();
		List<T> tests = this.getNonDominatedSolutions(final_tests);
		return tests;
	}

	/**
	 * This method is used by the Progress Monitor at the and of each generation to show the totol coverage reached by the algorithm.
	 * Since the Progress Monitor need a "Suite", this method artificially creates a "SuiteChromosome" (see {@link MOSA#suiteFitness}) 
	 * as the union of all test cases stored in {@link MOSA#archive}. 
	 * 
	 * The coverage score of the "SuiteChromosome" is given by the percentage of test goals covered (goals in {@link MOSA#archive})
	 * onto the total number of goals <code> this.fitnessFunctions</code> (see {@link GeneticAlgorithm}).
	 * 
	 * @return "SuiteChromosome" directly consumable by the Progress Monitor.
	 */
	@Override @SuppressWarnings("unchecked")
	public T getBestIndividual() {
		List<T> archiveContent = this.getArchive();
		if (archiveContent.isEmpty()) {
			return (T) new TestSuiteChromosome();
		}

		TestSuiteChromosome best = new TestSuiteChromosome();
		for (T test : archiveContent) {
			best.addTest((TestChromosome) test);
		}
		// compute overall fitness and coverage
		double coverage = ((double) this.getNumberOfCoveredGoals()) / ((double) this.fitnessFunctions.size());
		for (TestSuiteFitnessFunction suiteFitness : suiteFitnesses){
			best.setCoverage(suiteFitness, coverage);
			best.setFitness(suiteFitness,  this.fitnessFunctions.size() - this.getNumberOfCoveredGoals());
		}
		//suiteFitness.getFitness(best);
		return (T) best;
	}

	protected double numberOfCoveredTargets(){
		return this.archive.keySet().size();
	}
}
