/**
 * Copyright (C) 2011,2012 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 * 
 * This file is part of EvoSuite.
 * 
 * EvoSuite is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 * 
 * EvoSuite is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Public License for more details.
 * 
 * You should have received a copy of the GNU Public License along with
 * EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.testcase;

import java.util.ArrayList;
import java.util.List;

import org.evosuite.Properties;
import org.evosuite.Properties.AdaptiveLocalSearchTarget;
import org.evosuite.coverage.mutation.Mutation;
import org.evosuite.coverage.mutation.MutationExecutionResult;
import org.evosuite.ga.Chromosome;
import org.evosuite.ga.ConstructionFailedException;
import org.evosuite.ga.GeneticAlgorithm;
import org.evosuite.ga.LocalSearchBudget;
import org.evosuite.ga.LocalSearchObjective;
import org.evosuite.ga.MutationHistory;
import org.evosuite.ga.SecondaryObjective;
import org.evosuite.setup.TestCluster;
import org.evosuite.symbolic.BranchCondition;
import org.evosuite.symbolic.ConcolicExecution;
import org.evosuite.symbolic.ConcolicMutation;
import org.evosuite.testsuite.CurrentChromosomeTracker;
import org.evosuite.testsuite.TestSuiteFitnessFunction;
import org.evosuite.utils.Randomness;

/**
 * Chromosome representation of test cases
 * 
 * @author Gordon Fraser
 */
public class TestChromosome extends ExecutableChromosome {

	private static final long serialVersionUID = 7532366007973252782L;

	/** The test case encoded in this chromosome */
	protected TestCase test = new DefaultTestCase();

	/** To keep track of what has changed since last fitness evaluation */
	protected MutationHistory<TestMutationHistoryEntry> mutationHistory = new MutationHistory<TestMutationHistoryEntry>();

	/** Secondary objectives used during ranking */
	private static final List<SecondaryObjective> secondaryObjectives = new ArrayList<SecondaryObjective>();

	/**
	 * <p>
	 * setTestCase
	 * </p>
	 * 
	 * @param testCase
	 *            a {@link org.evosuite.testcase.TestCase} object.
	 */
	public void setTestCase(TestCase testCase) {
		test = testCase;
	}

	/**
	 * <p>
	 * getTestCase
	 * </p>
	 * 
	 * @return a {@link org.evosuite.testcase.TestCase} object.
	 */
	public TestCase getTestCase() {
		return test;
	}

	/** {@inheritDoc} */
	@Override
	public void setLastExecutionResult(ExecutionResult lastExecutionResult) {
		assert lastExecutionResult.test.equals(this.test);
		this.lastExecutionResult = lastExecutionResult;
	}

	/** {@inheritDoc} */
	@Override
	public void setChanged(boolean changed) {
		super.setChanged(changed);
		if (changed)
			clearCachedResults();
		CurrentChromosomeTracker.getInstance().changed(this);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * Create a deep copy of the chromosome
	 */
	@Override
	public Chromosome clone() {
		TestChromosome c = new TestChromosome();
		c.test = test.clone();
		//assert (test.toCode().equals(c.test.toCode()));
		/*
		assert (test.isValid());
		try {
			c.test.isValid();
		} catch (Throwable t) {
			logger.warn(c.test.toCode());
		}
		assert (c.test.isValid());
		*/
		c.setFitness(getFitness());
		c.solution = solution;
		c.copyCachedResults(this);
		c.setChanged(isChanged());
		if (Properties.ADAPTIVE_LOCAL_SEARCH != AdaptiveLocalSearchTarget.OFF) {
			for (TestMutationHistoryEntry mutation : mutationHistory) {
			    c.mutationHistory.addMutationEntry(mutation.clone(c.getTestCase()));
			}
		}
		// c.mutationHistory.set(mutationHistory);

		return c;
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.ExecutableChromosome#copyCachedResults(org.evosuite.testcase.ExecutableChromosome)
	 */
	/** {@inheritDoc} */
	@Override
	public void copyCachedResults(ExecutableChromosome other) {
		if (test == null)
			throw new RuntimeException("Test is null!");

		if (other.lastExecutionResult != null) {
			this.lastExecutionResult = other.lastExecutionResult.clone();
			this.lastExecutionResult.test = this.test;
		}

		if (other.lastMutationResult != null) {
			for (Mutation mutation : other.lastMutationResult.keySet()) {
				MutationExecutionResult copy = other.lastMutationResult.get(mutation); //.clone();
				//copy.test = test;
				this.lastMutationResult.put(mutation, copy);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 * 
	 * Single point cross over
	 */
	@Override
	public void crossOver(Chromosome other, int position1, int position2)
	        throws ConstructionFailedException {
		logger.debug("Crossover starting");
		TestChromosome offspring = new TestChromosome();
		TestFactory testFactory = TestFactory.getInstance();

		for (int i = 0; i < position1; i++) {
			offspring.test.addStatement(test.getStatement(i).clone(offspring.test));
		}
		for (int i = position2; i < other.size(); i++) {
			testFactory.appendStatement(offspring.test,
			                            ((TestChromosome) other).test.getStatement(i));
		}
		if (!Properties.CHECK_MAX_LENGTH
		        || offspring.test.size() <= Properties.CHROMOSOME_LENGTH) {
			test = offspring.test;
		}

		setChanged(true);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * Two chromosomes are equal if their tests are equal
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TestChromosome other = (TestChromosome) obj;
		if (test == null) {
			if (other.test != null)
				return false;
		} else if (!test.equals(other.test))
			return false;
		return true;
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		return test.hashCode();
	}

	public MutationHistory<TestMutationHistoryEntry> getMutationHistory() {
		return mutationHistory;
	}

	public boolean hasRelevantMutations() {

	    if(mutationHistory.isEmpty()) {
		logger.info("Mutation history is empty");
		return false;
	    }

		// Only apply local search up to the point where an exception was thrown
		int lastPosition = test.size() - 1;
		if (lastExecutionResult != null && !isChanged()) {
			Integer lastPos = lastExecutionResult.getFirstPositionOfThrownException();
			if (lastPos != null)
				lastPosition = lastPos.intValue();
		}
		logger.info("Mutation history: " + mutationHistory.toString());

		for (TestMutationHistoryEntry mutation : mutationHistory) {
			if (mutation.getMutationType() != TestMutationHistoryEntry.TestMutation.DELETION
			    && mutation.getStatement().getPosition() <= lastPosition
			    && mutation.getStatement() instanceof PrimitiveStatement<?>) {
				if (!test.hasReferences(mutation.getStatement().getReturnValue())
				        && !mutation.getStatement().getReturnClass().equals(Properties.getTargetClass())) {
					continue;
				}

				int newPosition = -1;
				for (int i = 0; i <= lastPosition; i++) {
					if (test.getStatement(i) == mutation.getStatement()) {
						newPosition = i;
						break;
					}
				}

				// Couldn't find statement, may have been deleted in other mutation?
				assert (newPosition >= 0);
				if (newPosition < 0) {
					continue;
				}

				return true;
			}
		}
		return false;
	}

	@Override
	public void applyAdaptiveLocalSearch(LocalSearchObjective objective) {

		double oldFitness = getFitness();
		logger.info("Applying local search on test case");

		// Only apply local search up to the point where an exception was thrown
		int lastPosition = test.size() - 1;
		if (lastExecutionResult != null && !isChanged()) {
			Integer lastPos = lastExecutionResult.getFirstPositionOfThrownException();
			if (lastPos != null)
				lastPosition = lastPos.intValue();
		}

		logger.info("Checking {} mutations", mutationHistory.size());
		for (TestMutationHistoryEntry mutation : mutationHistory) {
			if (LocalSearchBudget.isFinished())
				break;

			if (mutation.getMutationType() != TestMutationHistoryEntry.TestMutation.DELETION
			    && mutation.getStatement().getPosition() <= lastPosition
			        && mutation.getStatement() instanceof PrimitiveStatement<?>) {
				logger.info("Found suitable mutation");

				if (!test.hasReferences(mutation.getStatement().getReturnValue())
				        && !mutation.getStatement().getReturnClass().equals(Properties.getTargetClass())) {
					logger.info("Return value of statement "
					        + " is not referenced and not SUT, not doing local search");
					continue;
				}

				int newPosition = -1;
				for (int i = 0; i <= lastPosition; i++) {
					if (test.getStatement(i) == mutation.getStatement()) {
						newPosition = i;
						break;
					}
				}

				// Couldn't find statement, may have been deleted in other mutation?
				assert (newPosition >= 0);
				if (newPosition < 0) {
					logger.info("Can't find statement, assertion would trigger if they were active");
					continue;
				}

				logger.info("Yes, now applying the search at position {}!", newPosition);
				LocalSearch search = LocalSearch.getLocalSearchFor(mutation.getStatement());
				if (search != null) {
					search.doSearch(this, newPosition, objective);
					newPosition += search.getPositionDelta();
				}
			} else {
				logger.info("Unsuitable mutation");
			}
		}

		LocalSearchBudget.individualImproved(this);

		assert (getFitness() <= oldFitness);
		//logger.info("Test after local search: " + test.toCode());

		// TODO: Handle arrays in local search
		// TODO: mutating an int might have an effect on array lengths
	}

	/* (non-Javadoc)
	 * @see org.evosuite.ga.Chromosome#localSearch()
	 */
	/** {@inheritDoc} */
	@Override
	public void localSearch(LocalSearchObjective objective) {
		//logger.info("Test before local search: " + test.toCode());
		double oldFitness = getFitness();

		// Only apply local search up to the point where an exception was thrown
		int lastPosition = test.size() - 1;
		if (lastExecutionResult != null && !isChanged()) {
			Integer lastPos = lastExecutionResult.getFirstPositionOfThrownException();
			if (lastPos != null)
				lastPosition = lastPos.intValue();
		}

		//We count down to make the code work when lines are
		//added during the search (see NullReferenceSearch).
		for (int i = lastPosition; i >= 0; i--) {
			if (LocalSearchBudget.isFinished())
				break;

			if (i >= test.size()) {
				logger.warn("Test size decreased unexpectedly during local search, aborting local search");
				logger.warn(test.toCode());
				break;
			}

			if (!test.hasReferences(test.getStatement(i).getReturnValue())
			        && !test.getStatement(i).getReturnClass().equals(Properties.getTargetClass())) {
				logger.info("Return value of statement " + i
				        + " is not referenced and not SUT, not doing local search");
				continue;
			}

			LocalSearch search = LocalSearch.getLocalSearchFor(test.getStatement(i));
			if (search != null) {
				search.doSearch(this, i, objective);
				i += search.getPositionDelta();
			}
		}

		LocalSearchBudget.individualImproved(this);

		assert (getFitness() <= oldFitness);
		//logger.info("Test after local search: " + test.toCode());

		// TODO: Handle arrays in local search
		// TODO: mutating an int might have an effect on array lengths
	}

	/**
	 * {@inheritDoc}
	 * 
	 * Each statement is mutated with probability 1/l
	 */
	@Override
	public void mutate() {
		boolean changed = false;
		mutationHistory.clear();

		logger.debug("Mutation: delete");
		// Delete
		if (Randomness.nextDouble() <= Properties.P_TEST_DELETE) {
			changed = mutationDelete();
		}

		logger.debug("Mutation: change");
		// Change
		if (Randomness.nextDouble() <= Properties.P_TEST_CHANGE) {
			if (mutationChange())
				changed = true;
		}

		logger.debug("Mutation: insert");
		// Insert
		if (Randomness.nextDouble() <= Properties.P_TEST_INSERT) {
			if (mutationInsert())
				changed = true;
		}

		if (changed) {
			setChanged(true);
		}

	}

	/**
	 * Each statement is deleted with probability 1/length
	 * 
	 * @return
	 */
	private boolean mutationDelete() {
		boolean changed = false;
		double pl = 1d / test.size();
		TestFactory testFactory = TestFactory.getInstance();

		for (int num = test.size() - 1; num >= 0; num--) {

			// Each statement is deleted with probability 1/l
			if (Randomness.nextDouble() <= pl) {
				// if(!test.hasReferences(test.getStatement(num).getReturnValue()))
				// {
				try {
					TestCase copy = test.clone();
					// test_factory.deleteStatement(test, num);
					changed = true;
					mutationHistory.addMutationEntry(new TestMutationHistoryEntry(TestMutationHistoryEntry.TestMutation.DELETION));
					testFactory.deleteStatementGracefully(copy, num);
					test = copy;

				} catch (ConstructionFailedException e) {
					logger.warn("Deletion of statement failed: "
					        + test.getStatement(num).getCode());
					logger.warn(test.toCode());
				}
				// }
			}
		}

		return changed;
	}

	/**
	 * Each statement is replaced with probability 1/length
	 * 
	 * @return
	 */
	private boolean mutationChange() {
		boolean changed = false;
		double pl = 1d / test.size();
		TestFactory testFactory = TestFactory.getInstance();

		if (Randomness.nextDouble() < Properties.CONCOLIC_MUTATION) {
			try {
				changed = mutationConcolic();
			} catch (Exception exc) {
				logger.info("Encountered exception when trying to use concolic mutation.",
				            exc.getMessage());
				logger.debug("Detailed exception trace: ", exc);
			}
		}

		if (!changed) {
			for (StatementInterface statement : test) {
				if (Randomness.nextDouble() <= pl) {
					assert (test.isValid());
					int oldDistance = statement.getReturnValue().getDistance();
					if (statement instanceof StringPrimitiveStatement) {
						logger.info("Mutating string primitive statement");
					}
					if (statement.mutate(test, testFactory)) {
						changed = true;
						mutationHistory.addMutationEntry(new TestMutationHistoryEntry(TestMutationHistoryEntry.TestMutation.CHANGE, statement));
						assert (test.isValid());
					} else if (!statement.isAssignmentStatement()) {
						if (testFactory.changeRandomCall(test, statement)) {
							changed = true;
							mutationHistory.addMutationEntry(new TestMutationHistoryEntry(TestMutationHistoryEntry.TestMutation.CHANGE, statement));
						}
						assert (test.isValid());
					}
					statement.getReturnValue().setDistance(oldDistance);
					//					} else if (statement.getReturnValue() instanceof ArrayReference) {
				}
			}
		}
		return changed;
	}

	/**
	 * With exponentially decreasing probability, insert statements at random
	 * position
	 * 
	 * @return
	 */
	private boolean mutationInsert() {
		boolean changed = false;
		final double ALPHA = Properties.P_STATEMENT_INSERTION; //0.5;
		int count = 0;
		TestFactory testFactory = TestFactory.getInstance();

		while (Randomness.nextDouble() <= Math.pow(ALPHA, count)
		        && (!Properties.CHECK_MAX_LENGTH || size() < Properties.CHROMOSOME_LENGTH)) {
			count++;
			// Insert at position as during initialization (i.e., using helper
			// sequences)
			int position = testFactory.insertRandomStatement(test);
			if (position >= 0 && position < test.size()) {
			    mutationHistory.addMutationEntry(new TestMutationHistoryEntry(TestMutationHistoryEntry.TestMutation.INSERTION,
				        test.getStatement(position)
											  ));
			}
			changed = true;
		}
		return changed;
	}

	/**
	 * Collect path constraints and negate one of them to derive new integer
	 * inputs
	 * 
	 * @return
	 */
	private boolean mutationConcolic() {
		logger.info("Applying DSE mutation");
		// concolicExecution = new ConcolicExecution();

		// Apply DSE to gather constraints
		List<BranchCondition> branches = ConcolicExecution.getSymbolicPath(this);
		logger.debug("Conditions: " + branches);
		if (branches.isEmpty())
			return false;

		boolean mutated = false;
		List<BranchCondition> targetBranches = new ArrayList<BranchCondition>();
		for (BranchCondition branch : branches) {
			if (TestCluster.isTargetClassName(branch.getClassName()))
				targetBranches.add(branch);
		}
		// Select random branch
		BranchCondition branch = null;
		if (targetBranches.isEmpty())
			branch = Randomness.choice(branches);
		else
			branch = Randomness.choice(targetBranches);

		logger.debug("Trying to negate branch " + branch.getInstructionIndex()
		        + " - have " + targetBranches.size() + "/" + branches.size()
		        + " target branches");

		// Try to solve negated constraint
		TestCase newTest = ConcolicMutation.negateCondition(branch, test);

		// If successful, add resulting test to test suite
		if (newTest != null) {
			logger.debug("CONCOLIC: Created new test");
			// logger.info(newTest.toCode());
			// logger.info("Old test");
			// logger.info(test.toCode());
			this.test = newTest;
			this.setChanged(true);
			this.lastExecutionResult = null;
		} else {
			logger.debug("CONCOLIC: Did not create new test");
		}

		return mutated;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * The size of a chromosome is the length of its test case
	 */
	@Override
	public int size() {
		return test.size();
	}

	/** {@inheritDoc} */
	@Override
	public int compareTo(Chromosome o) {
		int result = super.compareTo(o);
		if (result != 0) {
			return result;
		}
		// make this deliberately not 0
		// because then ordering of results will be random 
		// among tests of equal fitness
		if (o instanceof TestChromosome) {
			return test.toCode().compareTo(((TestChromosome) o).test.toCode());
		}
		return result;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return test.toCode();
	}

	/**
	 * <p>
	 * hasException
	 * </p>
	 * 
	 * @return a boolean.
	 */
	public boolean hasException() {
		return lastExecutionResult == null ? false
		        : !lastExecutionResult.noThrownExceptions();
	}

	/* (non-Javadoc)
	 * @see org.evosuite.ga.Chromosome#applyDSE()
	 */
	/** {@inheritDoc} */
	@Override
	public void applyDSE(GeneticAlgorithm ga) {
		// TODO Auto-generated method stub
	}

	/** {@inheritDoc} */
	@Override
	public ExecutionResult executeForFitnessFunction(
	        TestSuiteFitnessFunction testSuiteFitnessFunction) {
		return testSuiteFitnessFunction.runTest(this.test);
	}

	/* (non-Javadoc)
	 * @see org.evosuite.ga.Chromosome#compareSecondaryObjective(org.evosuite.ga.Chromosome)
	 */
	/** {@inheritDoc} */
	@Override
	public int compareSecondaryObjective(Chromosome o) {
		int objective = 0;
		int c = 0;

		while (c == 0 && objective < secondaryObjectives.size()) {
			SecondaryObjective so = secondaryObjectives.get(objective++);
			if (so == null)
				break;
			c = so.compareChromosomes(this, o);
		}
		//logger.debug("Comparison: " + fitness + "/" + size() + " vs " + o.fitness + "/"
		//        + o.size() + " = " + c);
		return c;
	}

	/**
	 * Add an additional secondary objective to the end of the list of
	 * objectives
	 * 
	 * @param objective
	 *            a {@link org.evosuite.ga.SecondaryObjective} object.
	 */
	public static void addSecondaryObjective(SecondaryObjective objective) {
		secondaryObjectives.add(objective);
	}

	/**
	 * Remove secondary objective from list, if it is there
	 * 
	 * @param objective
	 *            a {@link org.evosuite.ga.SecondaryObjective} object.
	 */
	public static void removeSecondaryObjective(SecondaryObjective objective) {
		secondaryObjectives.remove(objective);
	}

	/**
	 * <p>
	 * Getter for the field <code>secondaryObjectives</code>.
	 * </p>
	 * 
	 * @return a {@link java.util.List} object.
	 */
	public static List<SecondaryObjective> getSecondaryObjectives() {
		return secondaryObjectives;
	}

}
