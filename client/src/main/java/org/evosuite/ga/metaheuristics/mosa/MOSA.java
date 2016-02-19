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
package org.evosuite.ga.metaheuristics.mosa;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.evosuite.ProgressMonitor;
import org.evosuite.Properties;
import org.evosuite.Properties.Criterion;
import org.evosuite.coverage.branch.BranchCoverageSuiteFitness;
import org.evosuite.coverage.mutation.StrongMutationSuiteFitness;
import org.evosuite.coverage.mutation.WeakMutationSuiteFitness;
import org.evosuite.coverage.statement.StatementCoverageSuiteFitness;
import org.evosuite.ga.Chromosome;
import org.evosuite.ga.ChromosomeFactory;
import org.evosuite.ga.ConstructionFailedException;
import org.evosuite.ga.FitnessFunction;
import org.evosuite.ga.comparators.CrowdingComparator;
import org.evosuite.ga.comparators.SortByFitness;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.ga.metaheuristics.SearchListener;
import org.evosuite.ga.operators.selection.SelectionFunction;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.evosuite.testsuite.TestSuiteFitnessFunction;
import org.evosuite.utils.ArrayUtil;
import org.evosuite.utils.Randomness;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of the MOSA (Many-Objective Sorting Algorithm) described in the ICST'15 paper ...
 * 
 * @author Annibale, Fitsum
 *
 * @param <T>
 */
public class MOSA<T extends Chromosome> extends GeneticAlgorithm<T> {
	private static final long serialVersionUID = 146182080947267628L;

	private static final Logger logger = LoggerFactory.getLogger(MOSA.class);

	/** Map used to store the covered test goals (keys of the map) and the corresponding covering test cases (values of the map) **/
	protected Map<FitnessFunction<T>, T> archive = new  HashMap<FitnessFunction<T>, T>();
	
	/** Boolean vector to indicate whether each test goal is covered or not. **/
	protected Set<FitnessFunction<T>> uncoveredGoals = new HashSet<FitnessFunction<T>>();

	/**
	 * keep track of overall suite fitness and coverage
	 */
	protected TestSuiteFitnessFunction suiteFitness = new BranchCoverageSuiteFitness();
	
	/** Selection function to select parents */
	protected SelectionFunction<T> selectionFunction = new MOSATournamentSelection<T>();

	/**
	 * Constructor
	 * 
	 * @param factory
	 *            a {@link org.evosuite.ga.ChromosomeFactory} object
	 */
	public MOSA(ChromosomeFactory<T> factory) {
		super(factory);
		if (ArrayUtil.contains(Properties.CRITERION, Criterion.BRANCH))
			suiteFitness = new BranchCoverageSuiteFitness();
		else if (ArrayUtil.contains(Properties.CRITERION, Criterion.STRONGMUTATION) 
				|| ArrayUtil.contains(Properties.CRITERION, Criterion.MUTATION))
			suiteFitness = new StrongMutationSuiteFitness();
		else if (ArrayUtil.contains(Properties.CRITERION, Criterion.WEAKMUTATION))
			suiteFitness = new WeakMutationSuiteFitness();
		else if (ArrayUtil.contains(Properties.CRITERION, Criterion.STATEMENT))
			suiteFitness = new StatementCoverageSuiteFitness();
		else{
			logger.warn("SMOSA currently supports BRANCH, STATEMENT, WEAKMUTATION and STRONGMUTATION criteria : " + Properties.CRITERION + ", defaulting to BRANCH.");
		}

	}

	/** {@inheritDoc} */
	@Override
	protected void evolve() {
		List<T> offspringPopulation = breedNextGeneration();

		// update archive
		updateArchive(offspringPopulation);

		// Create the union of parents and offSpring
		List<T> union = new ArrayList<T>();
		union.addAll(population);
		union.addAll(offspringPopulation);

		// Ranking the union
		logger.debug("Union Size =" + union.size());
		// Ranking the union using the best rank algorithm (modified version of the non dominated sorting algorithm
		Ranking<T> ranking = new RankBasedPreferenceSorting<T>(union, uncoveredGoals);

		// add to the archive the new covered goals (and the corresponding test cases)
		this.archive.putAll(ranking.getNewCoveredGoals());
		
		int remain = population.size();
		int index = 0;
		List<T> front = null;
		population.clear();

		// Obtain the next front
		front = ranking.getSubfront(index);

		while ((remain > 0) && (remain >= front.size())) {
			// Assign crowding distance to individuals
			crowdingDistanceAssignment(front);
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
		if (remain > 0) { // front contains individuals to insert
			crowdingDistanceAssignment(front);
			Collections.sort(front, new CrowdingComparator(true));
			for (int k = 0; k < remain; k++) {
				population.add(front.get(k));
			} // for

			remain = 0;
		} // if
		currentIteration++;
//		LoggingUtils.getEvoLogger().info("");
//		LoggingUtils.getEvoLogger().info("N. fronts = "+ranking.getNumberOfSubfronts());
//		LoggingUtils.getEvoLogger().info("1* front size = "+ranking.getSubfront(0).size());
//		LoggingUtils.getEvoLogger().info("Covered goals = "+getNumberOfCoveredGoals());
//		LoggingUtils.getEvoLogger().info("Uncovered goals = "+this.uncoveredGoals.size());
//	    LoggingUtils.getEvoLogger().info("Archive size = "+this.archive.size());
	    logger.debug("Generation=" + currentIteration + " Population Size=" + population.size() + " Archive size=" + archive.size());
	}

	/**
	 * This method is used to generate new individuals (offsprings) from
	 * the current population
	 * @return offspring population
	 */
	private List<T> breedNextGeneration() {
		List<T> offspringPopulation = new ArrayList<T>();
		while (!isNextPopulationFull(offspringPopulation)) {
			// select best individuals
			T parent1 = selectionFunction.select(population);
			T parent2 = selectionFunction.select(population);

			T offspring1 = (T) parent1.clone();
			T offspring2 = (T) parent2.clone();
			// apply crossover and mutation
			try {
				if (Randomness.nextDouble() <= Properties.CROSSOVER_RATE) {
					crossoverFunction.crossOver(offspring1, offspring2);
				}
				
				this.mutate(offspring1);
				this.mutate(offspring2);

				if (offspring1.isChanged()) {
					offspring1.updateAge(currentIteration);
					calculateFitness(offspring1);
				}
				if (offspring2.isChanged()) {
					offspring2.updateAge(currentIteration);
					calculateFitness(offspring2);
				}

			} catch (ConstructionFailedException e) {
				logger.info("CrossOver/Mutation failed.");
				continue;
			}

			if (!isTooLong(offspring1))
				offspringPopulation.add(offspring1);
			else
				offspringPopulation.add(parent1);

			if (!isTooLong(offspring2))
				offspringPopulation.add(offspring2);
			else
				offspringPopulation.add(parent2);
		}
		return offspringPopulation;
	}
	
	/**
	 * Method used to mutate an offspring
	 */
	private void mutate(T chromosome){		
		if (Math.random()<Properties.P_TEST_INSERTION || chromosome.size() < 2) {
			logger.debug("Test case empty, adding a random statement.");
			T newChromosome = this.chromosomeFactory.getChromosome();
			chromosome=newChromosome;
		}
		else{
			chromosome.mutate();
		}

		chromosome.setChanged(true);
		notifyMutation(chromosome);
	}
	
	/** 
	 * This method computes the fitness scores only for the uncovered goals
	 * @param c chromosome
	 */
	private void calculateFitness(T c) {
		for (FitnessFunction<T> fitnessFunction : this.uncoveredGoals) {
			// compute the fitness function only for uncovered goals
			// while for branch coverage this leads to slight improvements
			// for mutation coverage this save much time
			fitnessFunction.getFitness(c);
		}
		notifyEvaluation(c);
	}

	/** 
	 * This method computes the fitness scores for all (covered and uncovered) goals
	 * @param c chromosome
	 */
	private void completeCalculateFitness(T c) {
		for (FitnessFunction<T> fitnessFunction : fitnessFunctions) {
			fitnessFunction.getFitness(c);
			//notifyEvaluation(c);
		}
	}
	
	/**
	 * Notify all search listeners of fitness evaluation
	 * 
	 * @param chromosome
	 *            a {@link org.evosuite.ga.Chromosome} object.
	 */
	@Override
	protected void notifyEvaluation(Chromosome chromosome) {
		for (SearchListener listener : listeners) {
			if (listener instanceof ProgressMonitor)
				continue;
			listener.fitnessEvaluation(chromosome);
		}
	}
	
	/** {@inheritDoc} */
	@Override
	public void initializePopulation() {
		logger.info("executing initializePopulation function");

		notifySearchStarted();
		currentIteration = 0;

		// Create a random parent population P0
		generateInitialPopulation(Properties.POPULATION);
		// Determine fitness
		calculateFitness();
		this.notifyIteration();
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

		// update archive with goals covered so far (during initialization)
		updateArchive(population);

		// TODO add here dynamic stopping condition

		while (!isFinished() && this.getNumberOfCoveredGoals()<this.fitnessFunctions.size()) {
			evolve();
			notifyIteration();
		}

		completeCalculateFitness();
		notifySearchFinished();
	}

	/**
	 * Calculate fitness for all individuals
	 */
	protected void calculateFitness() {
		logger.debug("Calculating fitness for " + population.size() + " individuals");

		Iterator<T> iterator = population.iterator();
		while (iterator.hasNext()) {
			T c = iterator.next();
			if (isFinished()) {
				if (c.isChanged())
					iterator.remove();
			} else {
				calculateFitness(c);
			}
		}
	}
	
	protected void completeCalculateFitness() {
		logger.debug("Calculating fitness for " + population.size() + " individuals");
		Set<T> arch = new HashSet<T>(); 
		arch.addAll(archive.values());
		Iterator<T> iterator = arch.iterator();
		while (iterator.hasNext()) {
			T c = iterator.next();
				completeCalculateFitness(c);
		}
	}

	/** This method is used to print the number of test goals covered by the test cases stored in the current archive **/
	private int getNumberOfCoveredGoals() {
		int n_covered_goals = this.archive.keySet().size();
		logger.debug("# Covered Goals = " + n_covered_goals);
		return n_covered_goals;
	}
	
	/** This method return the test goals covered by the test cases stored in the current archive **/
	private Set<FitnessFunction<T>> getCoveredGoals() {
		return this.archive.keySet();
	}

	/**
	 * This method update the archive by adding test cases that cover new test goals, or replacing the
	 * old tests if the new ones are smaller (at the same level of coverage).
	 * 
	 * @param solutionSet is the list of Chromosomes (poulation)
	 */
	private void updateArchive(List<T> solutionSet) {
		// store the test cases that are optimal for the test goal in the
		// archive
		for (FitnessFunction<T> entry : this.getCoveredGoals()){
			double bestSize = this.archive.get(entry).size();
			for (T solution : solutionSet) {
				double value = entry.getFitness(solution);
				double size = solution.size();
				if (value == 0.0 && size < bestSize) {
					this.archive.put(entry, solution);
					bestSize = size;
				}
			}
		}
		this.uncoveredGoals.removeAll(this.getCoveredGoals());
	}

	protected List<T> getArchive() {
		Set<T> set = new HashSet<T>(); 
		set.addAll(archive.values());
		List<T> arch = new ArrayList<T>();
		arch.addAll(set);
		return arch;
	}
	
	protected List<T> getFinalTestSuite() {
		// trivial case where there are no branches to cover or the archive is empty
		if (this.getNumberOfCoveredGoals()==0) {
			return getArchive();
		}
		if (archive.size() == 0)
			if (population.size() > 0) {
				ArrayList<T> list = new ArrayList<T>();
				list.add(population.get(population.size() - 1));
				return list;
			} else
				return getArchive();
		List<T>[] rank=this.nonDominatedSorting(getArchive());
		return rank[0];
	}
	
	/**
	 * Method used to assign the Crowding Distance
	 * @param F Pareto frontier
	 */
	protected void crowdingDistanceAssignment(List<T> f) {
		int size = f.size();

		if (size == 0)
			return;
		if (size == 1) {
			f.get(0).setDistance(Double.POSITIVE_INFINITY);
			return;
		}
		if (size == 2) {
			f.get(0).setDistance(Double.POSITIVE_INFINITY);
			f.get(1).setDistance(Double.POSITIVE_INFINITY);
			return;
		}

		// use a new Population List to avoid altering the original Population
		List<T> front = new ArrayList<T>(size);
		front.addAll(f);

		for (int i = 0; i < size; i++)
			front.get(i).setDistance(0.0);

		double objetiveMaxn;
		double objetiveMinn;
		double distance;

		for (final FitnessFunction<?> ff : this.uncoveredGoals) {
			// Sort the population by Fit n
			Collections.sort(front, new SortByFitness(ff, true));

			objetiveMinn = front.get(0).getFitness(ff);
			objetiveMaxn = front.get(front.size() - 1).getFitness(ff);

			// set crowding distance
			front.get(0).setDistance(Double.POSITIVE_INFINITY);
			front.get(size - 1).setDistance(Double.POSITIVE_INFINITY);

			for (int j = 1; j < size - 1; j++) {
				distance = front.get(j + 1).getFitness(ff) - front.get(j - 1).getFitness(ff);
				distance = distance / (objetiveMaxn - objetiveMinn);
				distance += front.get(j).getDistance();
				front.get(j).setDistance(distance);
			}
		}
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
	@Override
	public T getBestIndividual() {
		TestSuiteChromosome best = new TestSuiteChromosome();
		//Chromosome best = new TestSuiteChromosome();
		for (T test : getArchive()) {
			best.addTest((TestChromosome) test);
		}
		// compute overall fitness and coverage
		double coverage = ((double) this.getNumberOfCoveredGoals()) / ((double) this.fitnessFunctions.size());
		best.setCoverage(suiteFitness, coverage);
		best.setFitness(suiteFitness,  this.fitnessFunctions.size() - this.getNumberOfCoveredGoals());
		//suiteFitness.getFitness(best);
		return (T)best;
	}

	@Override
	public List<T> getBestIndividuals() {
		//get final test suite (i.e., non dominated solutions in Archive)
		TestSuiteChromosome bestTestCases = new TestSuiteChromosome();
		for (T test : getFinalTestSuite()) {
			bestTestCases.addTest((TestChromosome) test);
		}
		// compute overall fitness and coverage
		suiteFitness.getFitness(bestTestCases);
		
		List<T> bests = new ArrayList<T>();
		bests.add((T) bestTestCases);
		return bests;
	}
	
	/**
	 * This method implements the traditional "Non-Dominated Sorting Algorithm"
	 * @param solutionSet set of test cases to rank with "Non-Dominated Sorting Algorithm"
	 * @return the list of fronts according to the uncovered goals
	 */
	private List<T>[] nonDominatedSorting(List<T> solutionSet) {
		// re-calculate all fitness gaols for double-check
		this.completeCalculateFitness();
				
		MOSADominanceComparator<T> criterion_ = new MOSADominanceComparator<T>(this.getCoveredGoals());
		List<T> solutionSet_ = solutionSet;

		// dominateMe[i] contains the number of solutions dominating i
		int[] dominateMe = new int[solutionSet_.size()];

		// iDominate[k] contains the list of solutions dominated by k
		List<Integer>[] iDominate = new List[solutionSet_.size()];

		// front[i] contains the list of individuals belonging to the front i
		List<Integer>[] front = new List[solutionSet_.size() + 1];

		// flagDominate is an auxiliary encodings.variable
		int flagDominate;

		// Initialize the fronts
		for (int i = 0; i < front.length; i++)
			front[i] = new LinkedList<Integer>();

		// -> Fast non dominated sorting algorithm
		for (int p = 0; p < solutionSet_.size(); p++) {
			// Initialize the list of individuals that i dominate and the number
			// of individuals that dominate me
			iDominate[p] = new LinkedList<Integer>();
			dominateMe[p] = 0;
		}
		for (int p = 0; p < (solutionSet_.size() - 1); p++) {
			// For all q individuals , calculate if p dominates q or vice versa
			for (int q = p + 1; q < solutionSet_.size(); q++) {
				flagDominate = criterion_.compare(solutionSet.get(p), solutionSet.get(q));

				if (flagDominate == -1) {
					iDominate[p].add(q);
					dominateMe[q]++;
				} else if (flagDominate == 1) {
					iDominate[q].add(p);
					dominateMe[p]++;
				}
			}
			// If nobody dominates p, p belongs to the first front
		}
		for (int p = 0; p < solutionSet_.size(); p++) {
			if (dominateMe[p] == 0) {
				front[0].add(p);
				//solutionSet.get(p).setRank(1);
			}
		}

		// Obtain the rest of fronts
		int i = 0;
		Iterator<Integer> it1, it2; // Iterators
		while (front[i].size() != 0) {
			i++;
			it1 = front[i - 1].iterator();
			while (it1.hasNext()) {
				it2 = iDominate[it1.next()].iterator();
				while (it2.hasNext()) {
					int index = it2.next();
					dominateMe[index]--;
					if (dominateMe[index] == 0) {
						front[i].add(index);
						//solutionSet_.get(index).setRank(i+1);
					}
				}
			}
		}
		List<T>[] fronts = new ArrayList[i];
		// 0,1,2,....,i-1 are front, then i fronts
		for (int j = 0; j < i; j++) {
			fronts[j] = new ArrayList<T>();
			it1 = front[j].iterator();
			while (it1.hasNext()) {
				fronts[j].add(solutionSet.get(it1.next()));
			}
		}
		return fronts;
	} // Ranking

}
