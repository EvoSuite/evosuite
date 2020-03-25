/*
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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.evosuite.ProgressMonitor;
import org.evosuite.Properties;
import org.evosuite.Properties.SelectionFunction;
import org.evosuite.coverage.FitnessFunctions;
import org.evosuite.coverage.exception.ExceptionCoverageSuiteFitness;
import org.evosuite.ga.Chromosome;
import org.evosuite.ga.ChromosomeFactory;
import org.evosuite.ga.ConstructionFailedException;
import org.evosuite.ga.FitnessFunction;
import org.evosuite.ga.archive.Archive;
import org.evosuite.ga.comparators.DominanceComparator;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.ga.metaheuristics.SearchListener;
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
import org.evosuite.utils.BudgetConsumptionMonitor;
import org.evosuite.utils.LoggingUtils;
import org.evosuite.utils.Randomness;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toList;

/**
 * Abstract class for MOSA or variants of MOSA.
 * 
 * @author Annibale Panichella, Fitsum M. Kifetew
 */
public abstract class AbstractMOSA<T extends Chromosome> extends GeneticAlgorithm<T> {

	private static final long serialVersionUID = 146182080947267628L;

	private static final Logger logger = LoggerFactory.getLogger(AbstractMOSA.class);

	/** Keep track of overall suite fitness functions and correspondent test fitness functions */
	protected final Map<TestSuiteFitnessFunction, Class<?>> suiteFitnessFunctions;

	/** Object used to keep track of the execution time needed to reach the maximum coverage */
	protected final BudgetConsumptionMonitor budgetMonitor;

	/**
	 * Constructor.
	 * 
	 * @param factory a {@link org.evosuite.ga.ChromosomeFactory} object
	 */
	public AbstractMOSA(ChromosomeFactory<T> factory) {
		super(factory);

		this.suiteFitnessFunctions = new LinkedHashMap<>();
		for (Properties.Criterion criterion : Properties.CRITERION) {
			TestSuiteFitnessFunction suiteFit = FitnessFunctions.getFitnessFunction(criterion);
			Class<?> testFit = FitnessFunctions.getTestFitnessFunctionClass(criterion);
			this.suiteFitnessFunctions.put(suiteFit, testFit);
		}

		this.budgetMonitor = new BudgetConsumptionMonitor();

		// set the secondary objectives of test cases (useful when MOSA compares two test
		// cases to, for example, update the archive)
		TestCaseSecondaryObjective.setSecondaryObjectives();

		if (Properties.SELECTION_FUNCTION != SelectionFunction.RANK_CROWD_DISTANCE_TOURNAMENT) {
		  LoggingUtils.getEvoLogger()
		  .warn("Originally, MOSA was implemented with a '"
		      + SelectionFunction.RANK_CROWD_DISTANCE_TOURNAMENT.name()
		      + "' selection function. You may want to consider using it.");
		}
	}

	/**
	 * This method is used to generate new individuals (offspring) from
	 * the current population. The offspring population has the same size as the parent population.
	 *
	 * @return offspring population
	 */
	@SuppressWarnings("unchecked")
	protected List<T> breedNextGeneration() {
		List<T> offspringPopulation = new ArrayList<>(Properties.POPULATION);
		// we apply only Properties.POPULATION/2 iterations since in each generation
		// we generate two offsprings
		for (int i = 0; i < Properties.POPULATION / 2 && !this.isFinished(); i++) {
			// select best individuals
			T parent1 = this.selectionFunction.select(this.population);
			T parent2 = this.selectionFunction.select(this.population);
			T offspring1 = (T) parent1.clone();
			T offspring2 = (T) parent2.clone();
			// apply crossover
			try {
				if (Randomness.nextDouble() <= Properties.CROSSOVER_RATE) {
					this.crossoverFunction.crossOver(offspring1, offspring2);
				}
			} catch (ConstructionFailedException e) {
				logger.debug("CrossOver failed.");
				continue;
			}

			this.removeUnusedVariables(offspring1);
			this.removeUnusedVariables(offspring2);

			// apply mutation on offspring1
			this.mutate(offspring1, parent1);
			if (offspring1.isChanged()) {
				this.clearCachedResults(offspring1);
				offspring1.updateAge(this.currentIteration);
				this.calculateFitness(offspring1);
				if (!shouldIgnore(offspring1))
					offspringPopulation.add(offspring1);
			}

			// apply mutation on offspring2
			this.mutate(offspring2, parent2);
			if (offspring2.isChanged()) {
				this.clearCachedResults(offspring2);
				offspring2.updateAge(this.currentIteration);
				this.calculateFitness(offspring2);
				if (!shouldIgnore(offspring2))
					offspringPopulation.add(offspring2);
			}
		}
		// Add new randomly generate tests
		for (int i = 0; i < Properties.POPULATION * Properties.P_TEST_INSERTION; i++) {
			T tch = null;
			if (this.getCoveredGoals().size() == 0 || Randomness.nextBoolean()) {
				tch = this.chromosomeFactory.getChromosome();
				tch.setChanged(true);
			} else {
				tch = (T) Randomness.choice(this.getSolutions()).clone();
				tch.mutate(); tch.mutate(); // TODO why is it mutated twice?
			}
			if (tch.isChanged()) {
				tch.updateAge(this.currentIteration);
				this.calculateFitness(tch);
				offspringPopulation.add(tch);
			}
		}
		logger.info("Number of offsprings = {}", offspringPopulation.size());
		return offspringPopulation;
	}

	/**
	 * This method check whether the offspring should be included in the new population
	 * @param offspring
	 * @return true (i.e., the test shuld be ignored) if it reached the timeout (too expensive test) or if
	 * it has a test exception
	 */
	protected boolean shouldIgnore(T offspring){
		TestChromosome tch = (TestChromosome) offspring;
		ExecutionResult result = tch.getLastExecutionResult();
		if (result == null)
			return true;

		return result.hasTimeout() || result.hasTestException();
	}

	/**
	 * Method used to mutate an offspring.
	 *
	 * @param offspring the offspring chromosome
	 * @param parent the parent chromosome that {@code offspring} was created from
	 */
	private void mutate(T offspring, T parent) {
		offspring.mutate();
		TestChromosome tch = (TestChromosome) offspring;
		if (!offspring.isChanged()) {
			// if offspring is not changed, we try to mutate it once again
			offspring.mutate();
		}
		if (!this.hasMethodCall(offspring)) {
			tch.setTestCase(((TestChromosome) parent).getTestCase().clone());
			boolean changed = tch.mutationInsert();
			if (changed) {
				tch.getTestCase().forEach(Statement::isValid);
			}
			offspring.setChanged(changed);
		}
		this.notifyMutation(offspring);
	}

	/**
	 * This method checks whether the test has only primitive type statements. Indeed,
	 * crossover and mutation can lead to tests with no method calls (methods or constructors
	 * call), thus, when executed they will never cover something in the class under test.
	 * 
	 * @param test to check
	 * @return true if the test has at least one method or constructor call (i.e., the test may
	 * cover something when executed; false otherwise
	 */
	private boolean hasMethodCall(T test) {
		boolean flag = false;
		TestCase tc = ((TestChromosome) test).getTestCase();
		for (Statement s : tc) {
			if (s instanceof MethodStatement) {
				MethodStatement ms = (MethodStatement) s;
				boolean isTargetMethod = ms.getDeclaringClassName().equals(Properties.TARGET_CLASS);
				if (isTargetMethod) {
					return true;
				}
			}
			if (s instanceof ConstructorStatement) {
				ConstructorStatement ms = (ConstructorStatement) s;
				boolean isTargetMethod = ms.getDeclaringClassName().equals(Properties.TARGET_CLASS);
				if (isTargetMethod) {
					return true;
				}
			}
		}
		return flag;
	}

	/**
	 * This method clears the cached results for a specific chromosome (e.g., fitness function
	 * values computed in previous generations). Since a test case is changed via crossover
	 * and/or mutation, previous data must be recomputed.
	 * 
	 * @param chromosome TestChromosome to clean
	 */
	private void clearCachedResults(T chromosome) {
		final TestChromosome testChromosome = (TestChromosome) chromosome;
		testChromosome.clearCachedMutationResults();
		testChromosome.clearCachedResults();
		testChromosome.clearMutationHistory();
		testChromosome.getFitnessValues().clear();
	}

	/**
	 * When a test case is changed via crossover and/or mutation, it can contains some
	 * primitive variables that are not used as input (or to store the output) of method calls.
	 * Thus, this method removes all these "trash" statements.
	 * 
	 * @param chromosome
	 * @return true or false depending on whether "unused variables" are removed
	 */
	private boolean removeUnusedVariables(T chromosome) {
		final int sizeBefore = chromosome.size();
		final TestCase t = ((TestChromosome) chromosome).getTestCase();
		final List<Integer> to_delete = new ArrayList<>(chromosome.size());
		boolean has_deleted = false;

		int num = 0;
		for (Statement s : t) {
			final VariableReference var = s.getReturnValue();
			final boolean delete = s instanceof PrimitiveStatement || s instanceof ArrayStatement;
			if (!t.hasReferences(var) && delete) {
				to_delete.add(num);
				has_deleted = true;
			}
			num++;
		}
		to_delete.sort(Collections.reverseOrder());
		for (int position : to_delete) {
			t.remove(position);
		}
		int sizeAfter = chromosome.size();
		if (has_deleted) {
			logger.debug("Removed {} unused statements", (sizeBefore - sizeAfter));
		}
		return has_deleted;
	}

	/**
	 * This method extracts non-dominated solutions (tests) according to all covered goal
	 * (e.g., branches).
	 * 
	 * @param solutions list of test cases to analyze with the "dominance" relationship
	 * @return the non-dominated set of test cases
	 */
	private List<T> getNonDominatedSolutions(List<T> solutions) {
		final DominanceComparator<T> comparator = new DominanceComparator<>(this.getCoveredGoals());
		final List<T> next_front = new ArrayList<>(solutions.size());
		boolean isDominated;
		for (T p : solutions) {
			isDominated = false;
			List<T> dominatedSolutions = new ArrayList<>(solutions.size());
			for (T best : next_front) {
				final int flag = comparator.compare(p, best);
				if (flag < 0) {
					dominatedSolutions.add(best);
				}
				if (flag > 0) {
					isDominated = true;
				}
			}
			if (isDominated) {
				continue;
			}

			next_front.add(p);
			next_front.removeAll(dominatedSolutions);
		}
		return next_front;
	}

	/** 
	 * {@inheritDoc}
	 */
	@Override
	public void initializePopulation() {
		logger.info("executing initializePopulation function");

		this.notifySearchStarted();
		this.currentIteration = 0;

		// Create a random parent population P0
		this.generateInitialPopulation(Properties.POPULATION);

		// Determine fitness
		this.calculateFitness();
		this.notifyIteration();
	}

    /**
     * Returns the goals that have been covered by the test cases stored in the archive.
     * 
     * @return
     */
    @SuppressWarnings("unchecked")
    protected Set<FitnessFunction<T>> getCoveredGoals() {
		return Archive.getArchiveInstance().getCoveredTargets().stream()
				.map(ff -> (FitnessFunction<T>) ff)
				.collect(toCollection(LinkedHashSet::new));
    }

    /**
     * Returns the number of goals that have been covered by the test cases stored in the archive.
     * 
     * @return
     */
    protected int getNumberOfCoveredGoals() {
      return Archive.getArchiveInstance().getNumberOfCoveredTargets();
    }

    protected void addUncoveredGoal(FitnessFunction<T> goal) {
      Archive.getArchiveInstance().addTarget((TestFitnessFunction) goal);
    }

    /**
     * Returns the goals that have not been covered by the test cases stored in the archive.
     * 
     * @return
     */
    @SuppressWarnings("unchecked")
    protected Set<FitnessFunction<T>> getUncoveredGoals() {
		return Archive.getArchiveInstance().getUncoveredTargets().stream()
				.map(ff -> (FitnessFunction<T>) ff)
				.collect(toCollection(LinkedHashSet::new));
    }

    /**
     * Returns the goals that have not been covered by the test cases stored in the archive.
     * 
     * @return
     */
    protected int getNumberOfUncoveredGoals() {
      return Archive.getArchiveInstance().getNumberOfUncoveredTargets();
    }

    /**
     * Returns the total number of goals, i.e., number of covered goals + number of uncovered goals.
     * 
     * @return
     */
    protected int getTotalNumberOfGoals() {
      return Archive.getArchiveInstance().getNumberOfTargets();
    }

    /**
     * Return the test cases in the archive as a list.
     * 
     * @return
     */
    @SuppressWarnings("unchecked")
    protected List<T> getSolutions() {
		return Archive.getArchiveInstance().getSolutions().stream()
				.map(test -> (T) test)
				.collect(toList());
    }

    /**
     * Generates a {@link org.evosuite.testsuite.TestSuiteChromosome} object with all test cases
     * in the archive.
     * 
     * @return
     */
    protected TestSuiteChromosome generateSuite() {
      TestSuiteChromosome suite = new TestSuiteChromosome();
      Archive.getArchiveInstance().getSolutions().forEach(suite::addTest);
      return suite;
    }

	///// ----------------------

	/**
	 * Some methods of the super class (i.e., {@link org.evosuite.ga.metaheuristics.GeneticAlgorithm}
	 * class) require a {@link org.evosuite.testsuite.TestSuiteChromosome} object. However, MOSA
	 * evolves {@link org.evosuite.testsuite.TestChromosome} objects. Therefore, we must override
	 * those methods and create a {@link org.evosuite.testsuite.TestSuiteChromosome} object with all
	 * the evolved {@link org.evosuite.testsuite.TestChromosome} objects (either in the population or
	 * in the {@link org.evosuite.ga.archive.Archive}).
	 */

	/**
     * Notify all search listeners but ProgressMonitor of fitness evaluation.
     * 
     * @param chromosome a {@link org.evosuite.ga.Chromosome} object.
     */
    @Override
	protected void notifyEvaluation(Chromosome chromosome) {
		// ProgressMonitor requires a TestSuiteChromosome
		listeners.stream()
				.filter(l -> !(l instanceof ProgressMonitor))
				.forEach(l -> l.fitnessEvaluation(chromosome));
	}

    /**
     * Notify all search listeners but ProgressMonitor of a mutation.
     * 
     * @param chromosome a {@link org.evosuite.ga.Chromosome} object.
     */
    @Override
    protected void notifyMutation(Chromosome chromosome) {
		// ProgressMonitor requires a TestSuiteChromosome
		listeners.stream()
				.filter(l -> !(l instanceof ProgressMonitor))
				.forEach(l -> l.modification(chromosome));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void calculateFitness(T c) {
        this.fitnessFunctions.forEach(fitnessFunction -> fitnessFunction.getFitness(c));

        // if one of the coverage criterion is Criterion.EXCEPTION, then we have to analyse the results
        // of the execution to look for generated exceptions
        if (ArrayUtil.contains(Properties.CRITERION, Properties.Criterion.EXCEPTION)) {
          TestChromosome testChromosome = (TestChromosome) c;
          ExceptionCoverageSuiteFitness.calculateExceptionInfo(
				  Collections.singletonList(testChromosome.getLastExecutionResult()),
              new HashMap<>(), new HashMap<>(), new HashMap<>(), new ExceptionCoverageSuiteFitness());
        }

        this.notifyEvaluation(c);
        // update the time needed to reach the max coverage
        this.budgetMonitor.checkMaxCoverage(this.getNumberOfCoveredGoals());
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<T> getBestIndividuals() {
        // get final test suite (i.e., non dominated solutions in Archive)
        TestSuiteChromosome bestTestCases = Archive.getArchiveInstance().mergeArchiveAndSolution(new TestSuiteChromosome());
        if (bestTestCases.getTestChromosomes().isEmpty()) {
          for (T test : this.getNonDominatedSolutions(this.population)) {
            bestTestCases.addTest((TestChromosome) test);
          }
        }

        // compute overall fitness and coverage
        this.computeCoverageAndFitness(bestTestCases);

		return Collections.singletonList((T) bestTestCases);
    }

    /**
     * {@inheritDoc}
     * 
     * <p>This method is used by the Progress Monitor at the and of each generation to show the total coverage reached by the algorithm.
     * Since the Progress Monitor requires a {@link org.evosuite.testsuite.TestSuiteChromosome} object, this method artificially creates
     * a {@link org.evosuite.testsuite.TestSuiteChromosome} object as the union of all solutions stored in the {@link
     * org.evosuite.ga.archive.Archive}.</p>
     * 
     * <p>The coverage score of the {@link org.evosuite.testsuite.TestSuiteChromosome} object is given by the percentage of targets marked
     * as covered in the archive.</p>
     * 
     * @return a {@link org.evosuite.testsuite.TestSuiteChromosome} object to be consumable by the Progress Monitor.
     */
    @SuppressWarnings("unchecked")
    @Override
    public T getBestIndividual() {
        TestSuiteChromosome best = this.generateSuite();
        if (best.getTestChromosomes().isEmpty()) {
          for (T test : this.getNonDominatedSolutions(this.population)) {
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

    protected void computeCoverageAndFitness(TestSuiteChromosome suite) {
      for (Entry<TestSuiteFitnessFunction, Class<?>> entry : this.suiteFitnessFunctions
          .entrySet()) {
        TestSuiteFitnessFunction suiteFitnessFunction = entry.getKey();
        Class<?> testFitnessFunction = entry.getValue();

        int numberCoveredTargets =
            Archive.getArchiveInstance().getNumberOfCoveredTargets(testFitnessFunction);
        int numberUncoveredTargets =
            Archive.getArchiveInstance().getNumberOfUncoveredTargets(testFitnessFunction);
        int totalNumberTargets = numberCoveredTargets + numberUncoveredTargets;

        double coverage = totalNumberTargets == 0 ? 1.0
            : ((double) numberCoveredTargets) / ((double) totalNumberTargets);

        suite.setFitness(suiteFitnessFunction, ((double) numberUncoveredTargets));
        suite.setCoverage(suiteFitnessFunction, coverage);
        suite.setNumOfCoveredGoals(suiteFitnessFunction, numberCoveredTargets);
        suite.setNumOfNotCoveredGoals(suiteFitnessFunction, numberUncoveredTargets);
      }
    }

}
