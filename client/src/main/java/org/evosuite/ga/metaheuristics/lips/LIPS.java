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
package org.evosuite.ga.metaheuristics.lips;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.evosuite.ProgressMonitor;
import org.evosuite.Properties;
import org.evosuite.Properties.Criterion;
import org.evosuite.coverage.branch.BranchCoverageSuiteFitness;
import org.evosuite.ga.Chromosome;
import org.evosuite.ga.ChromosomeFactory;
import org.evosuite.ga.ConstructionFailedException;
import org.evosuite.ga.FitnessFunction;
import org.evosuite.ga.comparators.SortByFitness;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.ga.metaheuristics.SearchListener;
import org.evosuite.ga.metaheuristics.mosa.MOSA;
import org.evosuite.ga.metaheuristics.mosa.structural.BranchesManager;
import org.evosuite.rmi.ClientServices;
import org.evosuite.statistics.RuntimeVariable;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.TestFitnessFunction;
import org.evosuite.testcase.execution.ExecutionResult;
import org.evosuite.testcase.execution.TestCaseExecutor;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.evosuite.testsuite.TestSuiteFitnessFunction;
import org.evosuite.utils.ArrayUtil;
import org.evosuite.utils.Randomness;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of the LIPS (Linearly Independent Path based Search) described in:
 * 
 * [1] S. Scalabrino, G. Grano, D. Di Nucci, R. Oliveto, A. De Lucia. "Search-Based Testing of Procedural 
 *     Programs: Iterative Single-Target or Multi-target Approach?". 
 *     International Symposium on Search Based Software Engineering (SSBSE 2016)
 * 
 * @author Annibale Panichella
 */
public class LIPS <T extends Chromosome> extends GeneticAlgorithm<T>{

	private static final long serialVersionUID = 146182080947267628L;

	private static final Logger logger = LoggerFactory.getLogger(LIPS.class);

	/** Map used to store the covered test goals (keys of the map) and the corresponding covering test cases (values of the map) **/
	protected Map<FitnessFunction<T>, T> archive = new  HashMap<FitnessFunction<T>, T>();

	/** Set of branches yet to be covered **/
	protected Set<FitnessFunction<T>> uncoveredBranches = new HashSet<FitnessFunction<T>>();

	/**  Keep track of overall suite fitness and coverage */
	protected TestSuiteFitnessFunction suiteFitness;

	/** Worklist of branches that can be potentially considered as search targets */
	protected LinkedList<FitnessFunction<T>> worklist = new LinkedList<FitnessFunction<T>>();

	/** List of branches that have been already considered as search targets but that are still uncovered */
	protected LinkedList<FitnessFunction<T>> alreadyAttemptedBranches = new LinkedList<FitnessFunction<T>>();

	/** Current branch used as fitness function */
	protected FitnessFunction<T> currentTarget;

	/** Control Flow Graph */
	protected BranchesManager<T> CFG;

	/** To keep track when the search started for the current target */
	protected long startSearch4Branch = 0;

	/** To keep track when the overall search started */
	protected long startGlobalSearch = 0;

	/** Budget allocated for the current target */
	protected long budget4branch;

	/** Object used to keep track of the execution time needed to reach the maximum coverage */
	protected BudgetConsumptionMonitor budgetMonitor;

	/**
	 * Constructor
	 */
	public LIPS(ChromosomeFactory<T> factory) {
		super(factory);
		if (ArrayUtil.contains(Properties.CRITERION, Criterion.BRANCH)) {
			suiteFitness = new BranchCoverageSuiteFitness();
		}
		startGlobalSearch = System.currentTimeMillis();
		budgetMonitor = new BudgetConsumptionMonitor();
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void evolve() {
		List<T> newGeneration = new ArrayList<T>();

		// Elitism. It is not specified in original paper [1]. 
		// However, we assume that LIPS uses elitism given the fact the 
		// elitism has been shown to positively affect the convergence
		// speed of GAs in various optimisation problems
		Collections.sort(population, new SortByFitness(this.currentTarget,false));
		newGeneration.add((T) population.get(0).clone());
		newGeneration.add((T) population.get(1).clone());

		// new_generation.size() < population_size
		while (newGeneration.size() < Properties.POPULATION) {
			T parent1 = selectionFunction.select(population);
			T parent2 = selectionFunction.select(population);

			T offspring1 = (T)parent1.clone();
			T offspring2 = (T)parent2.clone();

			try {
				if (Randomness.nextDouble() <= Properties.CROSSOVER_RATE) {
					crossoverFunction.crossOver(offspring1, offspring2);
				}

				notifyMutation(offspring1);
				offspring1.mutate();
				newGeneration.add(offspring1);

				notifyMutation(offspring2);
				offspring2.mutate();
				newGeneration.add(offspring2);

				if(offspring1.isChanged()) {
					offspring1.updateAge(currentIteration);
				}
				if(offspring2.isChanged()) {
					offspring2.updateAge(currentIteration);
				}
			} catch (ConstructionFailedException e) {
				logger.info("CrossOver/Mutation failed.");
				continue;
			}
		}

		population.clear();
		population = newGeneration;

		// calculate fitness for all test cases in the current (new) population
		calculateFitness();
	}

	@Override
	public void initializePopulation() {
		logger.info("executing initializePopulation function");
		currentIteration = 0;
		generateInitialPopulation(Properties.POPULATION-1);
		this.notifyIteration();
	}

	@Override
	public void generateSolution() {
		logger.info("executing generateSolution function");

		CFG = new BranchesManager<T>(fitnessFunctions);

		// generate the initial test t0
		// and update the worklist 
		searchInitialization(); 

		//  A random population which includes  t0
		//  is then generated to be used by the second iteration of the algorithm.
		initializePopulation();

		// calculate the current fitness
		calculateFitness();

		while (!isFinished() && this.uncoveredBranches.size()>0) {
			evolve();

			// if the current target is covered
			// the last branch added to the worklist is removed and used as new target of the search algorithm
			if (this.archive.containsKey(currentTarget)) {
				if (worklist.size()>0)
					this.currentTarget = worklist.removeLast();
				startSearch4Branch =  System.currentTimeMillis();
			} else if  (Math.abs(System.currentTimeMillis() - startSearch4Branch) >= this.budget4branch) {
				alreadyAttemptedBranches.add(this.currentTarget);
				if (worklist.size() > 0){
					this.currentTarget = worklist.removeLast();
				} else {
					// if the worklist is empty, we re-attempt the yet 
					// uncovered branches with the remaining budget
					worklist.addAll(alreadyAttemptedBranches);
					alreadyAttemptedBranches.clear();
					this.currentTarget = worklist.removeLast();
				}
				startSearch4Branch =  System.currentTimeMillis();
				logger.debug("SWITCHING TARGET");
			}

			// at the iteration i of the test generation process, the search budget must be updated
			updateBudget4Branch();

			// update generation counter
			currentIteration++;

			notifyIteration();
		}

		// storing the time needed to reach the maximum coverage
		ClientServices.getInstance().getClientNode().trackOutputVariable(RuntimeVariable.Time2MaxCoverage, this.budgetMonitor.getTime2MaxCoverage());
		notifySearchFinished();
	}

	/**
	 * This method calculates the fitness fitness function for all test cases in the 
	 * current population. It also performs the following operations:
	 * 1) updating the archive if the current target is covered
	 * 2) updating the branches in the worklist
	 * 3) computing collateral coverage
	 */
	private void calculateFitness() {
		for (T test : population){
			test.setChanged(true);
			runTest(test);

			double value = this.currentTarget.getFitness(test);

			if (value == 0.0){
				// If the search algorithm is able to find a test case that covers the target, a new test case,  "ti"
				// is added to the test suite and all the uncovered branches of decision nodes on the path covered by  "ti"
				// are added to the worklist
				updateArchive(test, currentTarget);
				updateWorkList(test);
			}

			// Sometimes, a test case can cover some branches that are already in the worklist (collateral coverage). 
			// These branches are removed from the worklist and marked as “covered”.
			computeCollateralCoverage(test);

			// update the time needed to reach the max coverage
			budgetMonitor.checkMaxCoverage(this.archive.keySet().size());
		}
	}

	/** 
	 * Initialization of the search process for LIPS
	 */
	protected void searchInitialization(){
		logger.info("generating firts test t0");
		notifySearchStarted();

		// keep track of covered goals
		for (FitnessFunction<T> goal : fitnessFunctions) {
			uncoveredBranches.add(goal);
		}
		worklist.addAll(CFG.getGraph().getRootBranches());

		// The first step is to randomly generate the first test case t0
		T t0 = this.chromosomeFactory.getChromosome();
		runTest(t0);
		this.population.add(t0);

		// For each decision node in the execution path of  t0
		// the uncovered branch of the decision is added to a worklist
		updateWorkList(t0);

		// the last branches added to the worklist is selected as current target
		this.currentTarget = worklist.removeLast();
	}

	/**
	 * This method executes a given test case (i.e., TestChromosome)
	 * 
	 * @param c test case (TestChromosome) to execute
	 */
	protected void runTest(T c){
		if (!c.isChanged())
			return;

		// run the test
		TestCase test = ((TestChromosome) c).getTestCase();
		ExecutionResult result = TestCaseExecutor.runTest(test);
		((TestChromosome) c).setLastExecutionResult(result);
		c.setChanged(false);

		// notify the fitness evaluation (i.e., the test is executed)
		notifyEvaluation(c);
	}

	/**
	 * "Sometimes, a test case can cover some branches that are already in the worklist (collateral 
	 * coverage). These branches are removed from the worklist and marked as 'covered'"
	 * 
	 * @param c test case (TestChromosome) to be analysed for collateral coverage
	 */
	protected void computeCollateralCoverage(T c){

		for (FitnessFunction<T> branch : worklist){
			double value = branch.getFitness(c);
			if (value == 0.0) 
				updateArchive(c, branch);
		}
		this.worklist.removeAll(this.archive.keySet());
		this.alreadyAttemptedBranches.removeAll(this.archive.keySet());
	}

	/** 
	 * For each decision node in the execution path of a test case, the uncovered branch 
	 * of the decision is added to a worklist.
	 * @param c test case (TestChromosome) to analised
	 */
	protected void updateWorkList(T c) {
		// Set of newly covered branches 
		Set<FitnessFunction<T>> coveredBranches = new HashSet<FitnessFunction<T>>();

		for (FitnessFunction<T> branch : fitnessFunctions){
			double value = branch.getFitness(c);
			if (value == 0)
				coveredBranches.add(branch);
		}

		// all the uncovered branches of decision nodes on the path covered by a test ti
		// are added to the worklist
		for (FitnessFunction<T> branch : coveredBranches){
			updateArchive(c, branch);
			for (FitnessFunction<T> dependent : CFG.getGraph().getStructuralChildren(branch)){
				if (this.uncoveredBranches.contains(dependent) && !worklist.contains(dependent))
					worklist.addFirst(dependent);
			}
		}
	}

	/**
	 * Store the test cases that are optimal for the test goal in the archive
	 * @param solution covering test case
	 * @param covered covered branch
	 */
	private void updateArchive(T solution, FitnessFunction<T> covered) {
		// the next two lines are needed since that coverage information are used
		// during EvoSuite post-processing
		TestChromosome tch = (TestChromosome) solution;
		tch.getTestCase().getCoveredGoals().add((TestFitnessFunction) covered);

		if (!archive.containsKey(covered)){
			archive.put(covered, solution);
			this.uncoveredBranches.remove(covered);
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

	/**
	 * This method is used by the Progress Monitor at the and of each generation to show the totol coverage reached by the algorithm.
	 * Copied from {@link MOSA#archive}. 
	 * 
	 * @return "SuiteChromosome" directly consumable by the Progress Monitor.
	 */
	@Override @SuppressWarnings("unchecked")
	public T getBestIndividual() {
		TestSuiteChromosome best = new TestSuiteChromosome();
		for (T test : getArchive()) {
			best.addTest((TestChromosome) test);
		}
		// compute overall fitness and coverage
		double coverage = ((double) this.archive.size()) / ((double) this.fitnessFunctions.size());
		best.setCoverage(suiteFitness, coverage);
		best.setFitness(suiteFitness,  this.fitnessFunctions.size() - this.archive.size());
		//suiteFitness.getFitness(best);
		return (T) best;
	}

	protected List<T> getArchive() {
		Set<T> set = new HashSet<T>(); 
		set.addAll(archive.values());
		List<T> arch = new ArrayList<T>();
		arch.addAll(set);
		return arch;
	}


	@SuppressWarnings("unchecked")
	@Override
	public List<T> getBestIndividuals() {
		//get final test suite (i.e., non dominated solutions in Archive)
		TestSuiteChromosome bestTestCases = new TestSuiteChromosome();
		for (T test : getFinalTestSuite()) {
			bestTestCases.addTest((TestChromosome) test);
		}
		for (FitnessFunction<T> f : this.archive.keySet()){
			bestTestCases.getCoveredGoals().add((TestFitnessFunction) f);
		}
		// compute overall fitness and coverage
		double fitness = this.fitnessFunctions.size() - numberOfCoveredTargets();
		double coverage = ((double) numberOfCoveredTargets()) / ((double) this.fitnessFunctions.size());
		bestTestCases.setFitness(suiteFitness, fitness);
		bestTestCases.setCoverage(suiteFitness, coverage);
		bestTestCases.setNumOfCoveredGoals(suiteFitness, (int) numberOfCoveredTargets());
		bestTestCases.setNumOfNotCoveredGoals(suiteFitness, (int) (this.fitnessFunctions.size()-numberOfCoveredTargets()));

		List<T> bests = new ArrayList<T>(1);
		bests.add((T) bestTestCases);
		return bests;
	}


	protected List<T> getFinalTestSuite() {
		// trivial case where there are no branches to cover or the archive is empty
		if (this.numberOfCoveredTargets()==0) {
			return getArchive();
		}
		if (archive.size() == 0)
			if (population.size() > 0) {
				ArrayList<T> list = new ArrayList<T>();
				list.add(population.get(population.size() - 1));
				return list;
			} else
				return getArchive();
		List<T> final_tests = getArchive();
		return final_tests;
	}

	protected double numberOfCoveredTargets(){
		return this.archive.keySet().size();
	}

	/** At the iteration i of the test generation process, the budget for the specific target to cover is computed as  
	 * SBi/ni, where  SBi is the remaining budget and  ni is the estimated number of remaining targets to be covered
	*/
	protected void updateBudget4Branch(){
		long budgetLeft = Properties.SEARCH_BUDGET * 1000 - (System.currentTimeMillis() - startGlobalSearch);
		long n_targets = this.fitnessFunctions.size() - this.archive.size() - this.alreadyAttemptedBranches.size();
		if (n_targets  > 0)
			this.budget4branch = budgetLeft / n_targets;
	}
}
