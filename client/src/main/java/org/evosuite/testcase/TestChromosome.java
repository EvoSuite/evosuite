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
import java.util.Collections;
import java.util.List;

import org.evosuite.Properties;
import org.evosuite.coverage.mutation.Mutation;
import org.evosuite.coverage.mutation.MutationExecutionResult;
import org.evosuite.ga.Chromosome;
import org.evosuite.ga.ConstructionFailedException;
import org.evosuite.ga.SecondaryObjective;
import org.evosuite.ga.localsearch.LocalSearchObjective;
import org.evosuite.ga.operators.mutation.MutationHistory;
import org.evosuite.setup.TestCluster;
import org.evosuite.symbolic.BranchCondition;
import org.evosuite.symbolic.ConcolicExecution;
import org.evosuite.symbolic.ConcolicMutation;
import org.evosuite.testcase.execution.ExecutionResult;
import org.evosuite.testcase.localsearch.TestCaseLocalSearch;
import org.evosuite.testcase.statements.PrimitiveStatement;
import org.evosuite.testcase.statements.StringPrimitiveStatement;
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
	private static final List<SecondaryObjective<?>> secondaryObjectives = new ArrayList<SecondaryObjective<?>>();

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
	    if (lastExecutionResult == null)
	        return ;
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
		c.setFitnesses(getFitnesses());
		c.setLastFitnesses(getLastFitnesses());
		c.solution = solution;
		c.copyCachedResults(this);
		c.setChanged(isChanged());
		if (Properties.LOCAL_SEARCH_SELECTIVE) {
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

		if (mutationHistory.isEmpty()) {
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

		for (TestMutationHistoryEntry mutation : mutationHistory) {
			logger.info("Considering: " + mutation.getMutationType());

			if (mutation.getMutationType() != TestMutationHistoryEntry.TestMutation.DELETION
			        && mutation.getStatement().getPosition() <= lastPosition) {
				if (Properties.LOCAL_SEARCH_SELECTIVE_PRIMITIVES) {
					if (!(mutation.getStatement() instanceof PrimitiveStatement<?>))
						continue;
				}
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

	/* (non-Javadoc)
	 * @see org.evosuite.ga.Chromosome#localSearch()
	 */
	/** {@inheritDoc} */
	@SuppressWarnings("unchecked")
	@Override
	public boolean localSearch(LocalSearchObjective<? extends Chromosome> objective) {
		TestCaseLocalSearch localSearch = TestCaseLocalSearch.getLocalSearch();
		return localSearch.doSearch(this,
		                            (LocalSearchObjective<TestChromosome>) objective);
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

		int lastPosition = getLastMutatableStatement();
		if(Properties.CHOP_MAX_LENGTH && size() >= Properties.CHROMOSOME_LENGTH) {
			test.chop(lastPosition + 1);
		}
		
		// Delete
		if (Randomness.nextDouble() <= Properties.P_TEST_DELETE) {
			logger.debug("Mutation: delete");
			changed = mutationDelete();
		}

		// Change
		if (Randomness.nextDouble() <= Properties.P_TEST_CHANGE) {
			logger.debug("Mutation: change");
			if (mutationChange())
				changed = true;
		}

		// Insert
		if (Randomness.nextDouble() <= Properties.P_TEST_INSERT) {
			logger.debug("Mutation: insert");
			if (mutationInsert())
				changed = true;
		}

		if (changed) {
			setChanged(true);
		}
		for (Statement s : test) {
			s.isValid();
		}
	}

	private int getLastMutatableStatement() {
		ExecutionResult result = getLastExecutionResult();
		if (result != null && !result.noThrownExceptions()) {
			int pos = result.getFirstPositionOfThrownException();
			// It may happen that pos > size() after statements have been deleted
			if (pos >= test.size())
				return test.size() - 1;
			else
				return pos;
		} else {
			return test.size() - 1;
		}
	}

	/**
	 * Each statement is deleted with probability 1/length
	 * 
	 * @return
	 */
	private boolean mutationDelete() {
		boolean changed = false;
		int lastMutatableStatement = getLastMutatableStatement();
		double pl = 1d / (lastMutatableStatement + 1);
		TestFactory testFactory = TestFactory.getInstance();

		//		for (int num = test.size() - 1; num >= 0; num--) {
		for (int num = lastMutatableStatement; num >= 0; num--) {

			// Each statement is deleted with probability 1/l
			if (Randomness.nextDouble() <= pl) {
				// if(!test.hasReferences(test.getStatement(num).getReturnValue()))
				// {
				try {
					TestCase copy = test.clone();
					// test_factory.deleteStatement(test, num);
					changed = true;
					mutationHistory.addMutationEntry(new TestMutationHistoryEntry(
					        TestMutationHistoryEntry.TestMutation.DELETION));
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
		int lastMutatableStatement = getLastMutatableStatement();
		double pl = 1d / (lastMutatableStatement + 1);
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
			for (int position = 0; position <= lastMutatableStatement; position++) {
				Statement statement = test.getStatement(position);
				//for (StatementInterface statement : test) {
				if (Randomness.nextDouble() <= pl) {
					assert (test.isValid());
					int oldDistance = statement.getReturnValue().getDistance();
					if (statement instanceof StringPrimitiveStatement) {
					}
					if (statement.mutate(test, testFactory)) {
						changed = true;
						mutationHistory.addMutationEntry(new TestMutationHistoryEntry(
						        TestMutationHistoryEntry.TestMutation.CHANGE, statement));
						assert (test.isValid());
					} else if (!statement.isAssignmentStatement()) {
						int pos = statement.getPosition();
						if (testFactory.changeRandomCall(test, statement)) {
							changed = true;
							mutationHistory.addMutationEntry(new TestMutationHistoryEntry(
							        TestMutationHistoryEntry.TestMutation.CHANGE,
							        test.getStatement(pos)));
						}
						assert (test.isValid());
					}
					statement.getReturnValue().setDistance(oldDistance);
					position = statement.getPosition(); // Might have changed due to mutation
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
			int position = testFactory.insertRandomStatement(test,
			                                                 getLastMutatableStatement());
			if (position >= 0 && position < test.size()) {
				changed = true;
				mutationHistory.addMutationEntry(new TestMutationHistoryEntry(
				        TestMutationHistoryEntry.TestMutation.INSERTION,
				        test.getStatement(position)));
			}
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
	/*
	@Override
	public boolean applyDSE(GeneticAlgorithm<?> ga) {
		// TODO Auto-generated method stub
		return false;
	}
	*/

	/** {@inheritDoc} */
	@Override
	public ExecutionResult executeForFitnessFunction(
	        TestSuiteFitnessFunction testSuiteFitnessFunction) {
		return testSuiteFitnessFunction.runTest(this.test);
	}

	@Override
	@SuppressWarnings("unchecked")
	public  <T extends Chromosome> int compareSecondaryObjective(T o) {
		int objective = 0;
		int c = 0;

		while (c == 0 && objective < secondaryObjectives.size()) {
			
			SecondaryObjective<T> so = (SecondaryObjective<T>) secondaryObjectives.get(objective++);
			if (so == null)
				break;
			c = so.compareChromosomes((T) this, o);
		} 
		return c;
	}
	/**
	 * Add an additional secondary objective to the end of the list of
	 * objectives
	 * 
	 * @param objective
	 *            a {@link org.evosuite.ga.SecondaryObjective} object.
	 */
	public static void addSecondaryObjective(SecondaryObjective<?> objective) {
		secondaryObjectives.add(objective);
	}
	
	public static void ShuffleSecondaryObjective() {
		Collections.shuffle(secondaryObjectives);
	}
	
	public static void reverseSecondaryObjective() {
		Collections.reverse(secondaryObjectives);
	}

	/**
	 * Remove secondary objective from list, if it is there
	 * 
	 * @param objective
	 *            a {@link org.evosuite.ga.SecondaryObjective} object.
	 */
	public static void removeSecondaryObjective(SecondaryObjective<?> objective) {
		secondaryObjectives.remove(objective);
	}

	/**
	 * <p>
	 * Getter for the field <code>secondaryObjectives</code>.
	 * </p>
	 * 
	 * @return a {@link java.util.List} object.
	 */
	public static List<SecondaryObjective<?>> getSecondaryObjectives() {
		return secondaryObjectives;
	}

}
