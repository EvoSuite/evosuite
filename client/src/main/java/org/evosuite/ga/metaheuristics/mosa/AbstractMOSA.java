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
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.evosuite.ProgressMonitor;
import org.evosuite.Properties;
import org.evosuite.Properties.Criterion;
import org.evosuite.coverage.FitnessFunctions;
import org.evosuite.coverage.branch.BranchCoverageSuiteFitness;
import org.evosuite.coverage.exception.ExceptionCoverageFactory;
import org.evosuite.coverage.exception.ExceptionCoverageHelper;
import org.evosuite.coverage.exception.ExceptionCoverageSuiteFitness;
import org.evosuite.coverage.exception.ExceptionCoverageTestFitness;
import org.evosuite.coverage.line.LineCoverageSuiteFitness;
import org.evosuite.coverage.method.MethodCoverageSuiteFitness;
import org.evosuite.coverage.mutation.StrongMutationSuiteFitness;
import org.evosuite.coverage.mutation.WeakMutationSuiteFitness;
import org.evosuite.coverage.statement.StatementCoverageSuiteFitness;
import org.evosuite.ga.Chromosome;
import org.evosuite.ga.ChromosomeFactory;
import org.evosuite.ga.ConstructionFailedException;
import org.evosuite.ga.FitnessFunction;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.ga.metaheuristics.SearchListener;
import org.evosuite.ga.metaheuristics.mosa.comparators.MOSADominanceComparator;
import org.evosuite.ga.operators.selection.SelectionFunction;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.TestFitnessFunction;
import org.evosuite.testcase.execution.ExecutionResult;
import org.evosuite.testcase.secondaryobjectives.TestCaseSecondaryObjective;
import org.evosuite.testcase.statements.ArrayStatement;
import org.evosuite.testcase.statements.ConstructorStatement;
import org.evosuite.testcase.statements.MethodStatement;
import org.evosuite.testcase.statements.PrimitiveStatement;
import org.evosuite.testcase.statements.Statement;
import org.evosuite.testcase.statements.StringPrimitiveStatement;
import org.evosuite.testcase.variable.VariableReference;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.evosuite.testsuite.TestSuiteFitnessFunction;
import org.evosuite.utils.ArrayUtil;
import org.evosuite.utils.Randomness;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Abstract class for MOSA
 * 
 * @author Annibale Panichella, Fitsum M. Kifetew
 *
 * @param <T>
 */
public abstract class AbstractMOSA<T extends Chromosome> extends GeneticAlgorithm<T> {

	private static final long serialVersionUID = 146182080947267628L;

	private static final Logger logger = LoggerFactory.getLogger(MOSA.class);

	/**  keep track of overall suite fitness and coverage */
	protected List<TestSuiteFitnessFunction> suiteFitnesses;

	/** Selection function to select parents */
	protected SelectionFunction<T> selectionFunction = new MOSATournamentSelection<T>();

	/** Selected ranking strategy **/
	protected Ranking<T> ranking;

	/**
	 * Constructor
	 * 
	 * @param factory
	 *            a {@link org.evosuite.ga.ChromosomeFactory} object
	 */
	public AbstractMOSA(ChromosomeFactory<T> factory) {
		super(factory);
		suiteFitnesses = new ArrayList<TestSuiteFitnessFunction>();
		for (Properties.Criterion criterion : Properties.CRITERION){
			TestSuiteFitnessFunction fit = FitnessFunctions.getFitnessFunction(criterion);
			suiteFitnesses.add(fit);
		}
		// set the ranking strategy
		if (Properties.RANKING_TYPE ==  Properties.RankingType.PREFERENCE_SORTING)
			ranking = new RankBasedPreferenceSorting<T>();
		else if (Properties.RANKING_TYPE ==  Properties.RankingType.FAST_NON_DOMINATED_SORTING)
			ranking = new FastNonDominatedSorting<T>();
		else
			ranking = new RankBasedPreferenceSorting<T>(); // default ranking strategy

		// set the secondary objectives of test cases (useful when MOSA compares two test
		// cases to, for example, update the archive)
		TestCaseSecondaryObjective.setSecondaryObjectives();
	}

	/**
	 * This method is used to generate new individuals (offsprings) from
	 * the current population
	 * @return offspring population
	 */
	@SuppressWarnings("unchecked")
	protected List<T> breedNextGeneration() {
		List<T> offspringPopulation = new ArrayList<T>(Properties.POPULATION);
		// we apply only Properties.POPULATION/2 iterations since in each generation
		// we generate two offsprings
		for (int i=0; i < Properties.POPULATION/2 && !isFinished(); i++){
			// select best individuals
			T parent1 = selectionFunction.select(population);
			T parent2 = selectionFunction.select(population);
			T offspring1 = (T) parent1.clone();
			T offspring2 = (T) parent2.clone();
			// apply crossover 
			try {
				if (Randomness.nextDouble() <= Properties.CROSSOVER_RATE) {
					crossoverFunction.crossOver(offspring1, offspring2);
				} 
			} catch (ConstructionFailedException e) {
				logger.debug("CrossOver failed.");
				continue;
			} 

			removeUnusedVariables(offspring1);
			removeUnusedVariables(offspring2);

			// apply mutation on offspring1
			mutate(offspring1, parent1);
			if (offspring1.isChanged()) {
				clearCachedResults(offspring1);
				offspring1.updateAge(currentIteration);
				calculateFitness(offspring1); 
				offspringPopulation.add(offspring1);
			}

			// apply mutation on offspring2
			mutate(offspring2, parent2);
			if (offspring2.isChanged()) {
				clearCachedResults(offspring2);
				offspring2.updateAge(currentIteration);
				calculateFitness(offspring2);
				offspringPopulation.add(offspring2);
			}	
		}
		// Add new randomly generate tests
		for (int i = 0; i<Properties.POPULATION * Properties.P_TEST_INSERTION; i++){
			T tch = null;
			if (this.getCoveredGoals().size() == 0 || Randomness.nextBoolean()){
				tch = this.chromosomeFactory.getChromosome();
				tch.setChanged(true);
			} else {
				tch = (T) Randomness.choice(getArchive()).clone();
				tch.mutate(); tch.mutate();
			}
			if (tch.isChanged()) {
				tch.updateAge(currentIteration);
				calculateFitness(tch);
				offspringPopulation.add(tch);
			}
		}
		logger.info("Number of offsprings = {}", offspringPopulation.size());
		return offspringPopulation;
	}

	/**
	 * Method used to mutate an offspring
	 */
	private void mutate(T offspring, T parent){
		offspring.mutate();
		TestChromosome tch = (TestChromosome) offspring;
		if (!offspring.isChanged()) {
			// if offspring is not changed, we try
			// to mutate it once again
			offspring.mutate();
		}
		if (!hasMethodCall(offspring)){
			tch.setTestCase(((TestChromosome) parent).getTestCase().clone());
			boolean changed = tch.mutationInsert();
			if (changed){
				for (Statement s : tch.getTestCase())
					s.isValid();
			} 
			offspring.setChanged(changed);
		}
		notifyMutation(offspring);
	}

	/** This method checks whether the test has only primitive type statements. Indeed,
	 * crossover and mutation can lead to tests with no method calls (methods or constructors call),
	 * thus, when executed they will never cover something in the class under test.

	 * @param test to check
	 * @return true if the test has at least one method or constructor call (i.e., the test may
	 * cover something when executed; false otherwise
	 */
	private boolean hasMethodCall(T test){
		boolean flag = false;
		TestCase tc = ((TestChromosome) test).getTestCase();
		for (Statement s : tc){
			if (s instanceof MethodStatement){
				MethodStatement ms = (MethodStatement) s;
				boolean isTargetMethod = ms.getDeclaringClassName().equals(Properties.TARGET_CLASS);
				if (isTargetMethod)
					return true;
			}
			if (s instanceof ConstructorStatement){
				ConstructorStatement ms = (ConstructorStatement) s;
				boolean isTargetMethod = ms.getDeclaringClassName().equals(Properties.TARGET_CLASS);
				if (isTargetMethod)
					return true;
			}
		}
		return flag;
	}

	/**
	 * This method clears the cached results for a specific chromosome (e.g., fitness function
	 * values computed in previous generations). Since a test case is changed via crossover
	 * and/or mutation, previous data must be recomputed.
	 * @param chromosome TestChromosome to clean
	 */
	public void clearCachedResults(T chromosome){
		((TestChromosome) chromosome).clearCachedMutationResults();
		((TestChromosome) chromosome).clearCachedResults();
		((TestChromosome) chromosome).clearMutationHistory();
		((TestChromosome) chromosome).getFitnessValues().clear();
	}

	/**
	 * When a test case is changed via crossover and/or mutation, it can contains some
	 * primitive variables that are not used as input (or to store the output) of method calls.
	 * Thus, this method removes all these "trash" statements.
	 * @param chromosome
	 * @return true or false depending on whether "unused variables" are removed
	 */
	public boolean removeUnusedVariables(T chromosome) {
		int sizeBefore = chromosome.size();
		TestCase t = ((TestChromosome) chromosome).getTestCase();
		List<Integer> to_delete = new ArrayList<Integer>(chromosome.size());
		boolean has_deleted = false;

		int num = 0;
		for (Statement s : t) {
			VariableReference var = s.getReturnValue();
			boolean delete = false;
			delete = delete || s instanceof PrimitiveStatement;
			delete = delete || s instanceof ArrayStatement;
			delete = delete || s instanceof StringPrimitiveStatement;
			if (!t.hasReferences(var) && delete) {
				to_delete.add(num);
				has_deleted = true;
			}
			num++;
		}
		Collections.sort(to_delete, Collections.reverseOrder());
		for (Integer position : to_delete) {
			t.remove(position);
		}
		int sizeAfter = chromosome.size();
		if (has_deleted)
			logger.debug("Removed {} unused statements", (sizeBefore - sizeAfter));
		return has_deleted;
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
	 * Calculate fitness for the whole population
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

	@SuppressWarnings("unchecked")
	@Override
	public List<T> getBestIndividuals() {
		//get final test suite (i.e., non dominated solutions in Archive)
		List<T> finalTestSuite = this.getFinalTestSuite();
		if (finalTestSuite.isEmpty()) {
			return Arrays.asList((T) new TestSuiteChromosome());
		}

		TestSuiteChromosome bestTestCases = new TestSuiteChromosome();
		for (T test : finalTestSuite) {
			bestTestCases.addTest((TestChromosome) test);
		}
		for (FitnessFunction<T> f : this.getCoveredGoals()){
			bestTestCases.getCoveredGoals().add((TestFitnessFunction) f);
		}
		// compute overall fitness and coverage
		double fitness = this.fitnessFunctions.size() - numberOfCoveredTargets();
		double coverage = ((double) numberOfCoveredTargets()) / ((double) this.fitnessFunctions.size());
		for (TestSuiteFitnessFunction suiteFitness : suiteFitnesses){
			bestTestCases.setFitness(suiteFitness, fitness);
			bestTestCases.setCoverage(suiteFitness, coverage);
			bestTestCases.setNumOfCoveredGoals(suiteFitness, (int) numberOfCoveredTargets());
			bestTestCases.setNumOfNotCoveredGoals(suiteFitness, (int) (this.fitnessFunctions.size()-numberOfCoveredTargets()));
		}
		List<T> bests = new ArrayList<T>(1);
		bests.add((T) bestTestCases);
		return bests;
	}

	/** 
	 * This method computes the fitness scores only for the current goals
	 * @param c chromosome
	 */
	protected abstract void calculateFitness(T c);

	protected abstract List<T> getFinalTestSuite();

	protected abstract List<T> getArchive();

	/**
	 * This method extracts non-dominated solutions (tests) according to all covered goal (e.g., branches)
	 * @param solutionSet set of test cases to analyze with the "dominance" relationship
	 * @return the non-dominated set of test cases
	 */
	protected List<T> getNonDominatedSolutions(List<T> solutions){
		MOSADominanceComparator<T> comparator = new MOSADominanceComparator<>(this.getCoveredGoals());
		List<T> next_front = new ArrayList<T>(solutions.size());
		boolean isDominated;
		for (T p : solutions){
			isDominated = false;
			List<T> dominatedSolutions = new ArrayList<T>(solutions.size());
			for (T best : next_front){
				int flag = comparator.compare(p, best);
				if (flag == -1) {
					dominatedSolutions.add(best);
				}
				if (flag == +1){
					isDominated = true;
				}	
			}
			if (isDominated)
				continue;

			next_front.add(p);
			next_front.removeAll(dominatedSolutions);
		}
		return next_front;
	}

	/**
	 * This method verifies whether two TestCromosome contain
	 * the same test case. Here the equality is computed looking at
	 * the strings composing the tests. This method is strongly needed 
	 * in {@link AbstractMOSA#breedNextGeneration()}.
	 * @param test1 first test
	 * @param test2 second test
	 * @return true if the test1 and test 2 (meant as String) are equal
	 * to each other; false otherwise.
	 */
	protected boolean areEqual(T test1, T test2){
		TestChromosome tch1 = (TestChromosome) test1;
		TestChromosome tch2 = (TestChromosome) test2;

		if (tch1.size() != tch2.size())
			return false;
		if (tch1.size() == 0)
			return false;
		if (tch2.size() == 0)
			return false;

		return tch1.getTestCase().toCode().equals(tch2.getTestCase().toCode());
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

	protected abstract double numberOfCoveredTargets();

	public abstract Set<FitnessFunction<T>> getCoveredGoals();

	/**
	 * This method analyzes the execution results of a TestChromosome looking for generated exceptions.
	 * Such exceptions are converted in instances of the class {@link ExceptionCoverageTestFitness},
	 * which are additional covered goals when using as criterion {@link Properties.Criterion.EXCEPTION}
	 * @param t TestChromosome to analyze
	 * @return list of exception goals being covered by t
	 */
	public List<ExceptionCoverageTestFitness> deriveCoveredExceptions(T t){
		List<ExceptionCoverageTestFitness> covered_exceptions = new ArrayList<ExceptionCoverageTestFitness>();
		TestChromosome testCh = (TestChromosome) t;
		ExecutionResult result = testCh.getLastExecutionResult();

		Map<String, Set<Class<?>>> implicitTypesOfExceptions = new LinkedHashMap<>();
		Map<String, Set<Class<?>>> explicitTypesOfExceptions = new LinkedHashMap<>();
		Map<String, Set<Class<?>>> declaredTypesOfExceptions = new LinkedHashMap<>();

		for (Integer i : result.getPositionsWhereExceptionsWereThrown()) {
			if(ExceptionCoverageHelper.shouldSkip(result,i)){
				continue;
			}

			Class<?> exceptionClass = ExceptionCoverageHelper.getExceptionClass(result,i);
			String methodIdentifier = ExceptionCoverageHelper.getMethodIdentifier(result, i); //eg name+descriptor
			boolean sutException = ExceptionCoverageHelper.isSutException(result,i); // was the exception originated by a direct call on the SUT?

			/*
			 * We only consider exceptions that were thrown by calling directly the SUT (not the other
			 * used libraries). However, this would ignore cases in which the SUT is indirectly tested
			 * through another class
			 */

			if (sutException) {

				boolean notDeclared = ! ExceptionCoverageHelper.isDeclared(result,i);

				if(notDeclared) {
					/*
					 * we need to distinguish whether it is explicit (ie "throw" in the code, eg for validating
					 * input for pre-condition) or implicit ("likely" a real fault).
					 */

					boolean isExplicit = ExceptionCoverageHelper.isExplicit(result,i);

					if (isExplicit) {

						if (!explicitTypesOfExceptions.containsKey(methodIdentifier)) {
							explicitTypesOfExceptions.put(methodIdentifier, new LinkedHashSet<Class<?>>());
						}
						explicitTypesOfExceptions.get(methodIdentifier).add(exceptionClass);
					} else {

						if (!implicitTypesOfExceptions.containsKey(methodIdentifier)) {
							implicitTypesOfExceptions.put(methodIdentifier, new LinkedHashSet<Class<?>>());
						}
						implicitTypesOfExceptions.get(methodIdentifier).add(exceptionClass);
					}
				} else {
					if (!declaredTypesOfExceptions.containsKey(methodIdentifier)) {
						declaredTypesOfExceptions.put(methodIdentifier, new LinkedHashSet<Class<?>>());
					}
					declaredTypesOfExceptions.get(methodIdentifier).add(exceptionClass);
				}


				ExceptionCoverageTestFitness.ExceptionType type = ExceptionCoverageHelper.getType(result,i);
				/*
				 * Add goal to list of fitness functions to solve
				 */
				ExceptionCoverageTestFitness goal = new ExceptionCoverageTestFitness(Properties.TARGET_CLASS, methodIdentifier, exceptionClass, type);
				covered_exceptions.add(goal);
			}
		}
		return covered_exceptions;
	}
}
