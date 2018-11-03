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
package org.evosuite.testcase;

import org.evosuite.Properties;
import org.evosuite.coverage.mutation.Mutation;
import org.evosuite.coverage.mutation.MutationExecutionResult;
import org.evosuite.ga.Chromosome;
import org.evosuite.ga.ConstructionFailedException;
import org.evosuite.ga.SecondaryObjective;
import org.evosuite.ga.localsearch.LocalSearchObjective;
import org.evosuite.ga.operators.mutation.MutationHistory;
import org.evosuite.runtime.javaee.injection.Injector;
import org.evosuite.runtime.util.AtMostOnceLogger;
import org.evosuite.setup.TestCluster;
import org.evosuite.symbolic.BranchCondition;
import org.evosuite.symbolic.ConcolicExecution;
import org.evosuite.symbolic.ConcolicMutation;
import org.evosuite.testcase.execution.ExecutionResult;
import org.evosuite.testcase.localsearch.TestCaseLocalSearch;
import org.evosuite.testcase.statements.FunctionalMockStatement;
import org.evosuite.testcase.statements.PrimitiveStatement;
import org.evosuite.testcase.statements.Statement;
import org.evosuite.testcase.variable.VariableReference;
import org.evosuite.testsuite.TestSuiteFitnessFunction;
import org.evosuite.utils.Randomness;
import org.evosuite.utils.generic.GenericAccessibleObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Chromosome representation of test cases
 *
 * @author Gordon Fraser
 */
public class TestChromosome extends ExecutableChromosome {

	private static final long serialVersionUID = 7532366007973252782L;

	private static final Logger logger = LoggerFactory.getLogger(TestChromosome.class);

	/** The test case encoded in this chromosome */
	protected TestCase test = new DefaultTestCase();

	/** To keep track of what has changed since last fitness evaluation */
	protected MutationHistory<TestMutationHistoryEntry> mutationHistory = new MutationHistory<TestMutationHistoryEntry>();

	/** Secondary objectives used during ranking */
	private static final List<SecondaryObjective<TestChromosome>> secondaryObjectives = new ArrayList<SecondaryObjective<TestChromosome>>();

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
		clearCachedResults();
		clearCachedMutationResults();
		setChanged(true);
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
		if (changed) {
			clearCachedResults();
		}
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
		c.setFitnessValues(getFitnessValues());
		c.setPreviousFitnessValues(getPreviousFitnessValues());
		c.copyCachedResults(this);
		c.setChanged(isChanged());
		c.setLocalSearchApplied(hasLocalSearchBeenApplied());
		if (Properties.LOCAL_SEARCH_SELECTIVE) {
			for (TestMutationHistoryEntry mutation : mutationHistory) {
				if(test.contains(mutation.getStatement()))
					c.mutationHistory.addMutationEntry(mutation.clone(c.getTestCase()));
			}
		}
		// c.mutationHistory.set(mutationHistory);
		c.setNumberOfMutations(this.getNumberOfMutations());
		c.setNumberOfEvaluations(this.getNumberOfEvaluations());
		c.setKineticEnergy(getKineticEnergy());
		c.setNumCollisions(getNumCollisions());

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
			this.lastExecutionResult.setTest(this.test);
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
		TestChromosome otherChromosome = (TestChromosome)other;
		TestChromosome offspring = new TestChromosome();
		TestFactory testFactory = TestFactory.getInstance();

		for (int i = 0; i < position1; i++) {
			offspring.test.addStatement(test.getStatement(i).clone(offspring.test));
		}

		for (int i = position2; i < other.size(); i++) {
			GenericAccessibleObject<?> accessibleObject = otherChromosome.test.getStatement(i).getAccessibleObject();
			if(accessibleObject != null) {
				if (accessibleObject.getDeclaringClass().equals(Injector.class))
					continue;
				if(!ConstraintVerifier.isValidPositionForInsertion(accessibleObject, offspring.test, offspring.test.size())) {
					continue;
				}
			}
			testFactory.appendStatement(offspring.test,
					otherChromosome.test.getStatement(i));
		}
		if (!Properties.CHECK_MAX_LENGTH
				|| offspring.test.size() <= Properties.CHROMOSOME_LENGTH) {
			test = offspring.test;
			setChanged(true);
		}
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

	public void clearMutationHistory() {
		mutationHistory.clear();
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
				final Class<?> targetClass = Properties.getTargetClassAndDontInitialise();

				if (!test.hasReferences(mutation.getStatement().getReturnValue())
				        && !mutation.getStatement().getReturnClass().equals(targetClass)) {
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
		TestCaseLocalSearch localSearch = TestCaseLocalSearch.selectTestCaseLocalSearch();
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

		if(mockChange()){
			changed = true;
		}

		if(Properties.CHOP_MAX_LENGTH && size() >= Properties.CHROMOSOME_LENGTH) {
			int lastPosition = getLastMutatableStatement();
			test.chop(lastPosition + 1);
		}

		// Delete
		if (Randomness.nextDouble() <= Properties.P_TEST_DELETE) {
			logger.debug("Mutation: delete");
			if(mutationDelete())
				changed = true;
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
			this.increaseNumberOfMutations();
			setChanged(true);
			test.clearCoveredGoals();
		}
		for (Statement s : test) {
			s.isValid();
		}

		// be sure that mutation did not break any constraint.
		// if it happens, it means a bug in EvoSuite
		assert ConstraintVerifier.verifyTest(test);
		assert ! ConstraintVerifier.hasAnyOnlyForAssertionMethod(test);
	}


	private boolean mockChange()  {

		/*
			Be sure to update the mocked values if there has been any change in
			behavior in the last execution.

			Note: mock "expansion" cannot be done after a test has been mutated and executed,
			as the expansion itself might have side effects. Therefore, it has to be done
			before a test is evaluated.
		 */

		boolean changed = false;

		for(int i=0; i<test.size(); i++){
			Statement st = test.getStatement(i);
			if(! (st instanceof FunctionalMockStatement)){
				continue;
			}

			FunctionalMockStatement fms = (FunctionalMockStatement) st;
			if(! fms.doesNeedToUpdateInputs()){
				continue;
			}

			int preLength = test.size();

			try {
				List<Type> missing = fms.updateMockedMethods();
				int pos = st.getPosition();
				logger.debug("Generating parameters for mock call");
				// Added 'null' as additional parameter - fix for @NotNull annotations issue on evo mailing list
				List<VariableReference> refs = TestFactory.getInstance().satisfyParameters(test, null, missing,null, pos, 0, true, false,true);
				fms.addMissingInputs(refs);
			} catch (Exception e){
				//shouldn't really happen because, in the worst case, we could create mocks for missing parameters
				String msg = "Functional mock problem: "+e.toString();
				AtMostOnceLogger.warn(logger, msg);
				fms.fillWithNullRefs();
				return changed;
			}
			changed = true;

			int increase = test.size() - preLength;
			i += increase;
		}

		return changed;
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

		if(test.isEmpty()){
			return false; //nothing to delete
		}

		boolean changed = false;
		int lastMutableStatement = getLastMutatableStatement();
		double pl = 1d / (lastMutableStatement + 1);
		TestFactory testFactory = TestFactory.getInstance();

		for (int num = lastMutableStatement; num >= 0; num--) {

			if(num >= test.size()){
				continue; //in case the delete remove more than one statement
			}

			// Each statement is deleted with probability 1/l
			if (Randomness.nextDouble() <= pl) {
				changed |= deleteStatement(testFactory, num);

				if(changed){
					assert ConstraintVerifier.verifyTest(test);
				}
			}
		}

		if(changed){
			assert ConstraintVerifier.verifyTest(test);
		}

		return changed;
	}

	protected boolean deleteStatement(TestFactory testFactory, int num) {

		try {

            TestCase copy = test.clone();

            mutationHistory.addMutationEntry(new TestMutationHistoryEntry(
					TestMutationHistoryEntry.TestMutation.DELETION));
            boolean modified = testFactory.deleteStatementGracefully(copy, num);

            test = copy;
           	return modified;

        } catch (ConstructionFailedException e) {
            logger.warn("Deletion of statement failed: " + test.getStatement(num).getCode());
            logger.warn(test.toCode());
			return false; //modifications were on copy
        }
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
				logger.warn("Encountered exception when trying to use concolic mutation: {}", exc.getMessage());
				logger.debug("Detailed exception trace: ", exc);
			}
		}

		if (!changed) {
			for (int position = 0; position <= lastMutatableStatement; position++) {
				if (Randomness.nextDouble() <= pl) {
					assert (test.isValid());

					Statement statement = test.getStatement(position);

					if(statement.isReflectionStatement())
						continue;

					int oldDistance = statement.getReturnValue().getDistance();

					//constraints are handled directly in the statement mutations
					if (statement.mutate(test, testFactory)) {
						changed = true;
						mutationHistory.addMutationEntry(new TestMutationHistoryEntry(
						        TestMutationHistoryEntry.TestMutation.CHANGE, statement));
						assert (test.isValid());
						assert ConstraintVerifier.verifyTest(test);

					} else if (!statement.isAssignmentStatement() &&
							ConstraintVerifier.canDelete(test,position)) {
						//if a statement should not be deleted, then it cannot be either replaced by another one

						int pos = statement.getPosition();
						if (testFactory.changeRandomCall(test, statement)) {
							changed = true;
							mutationHistory.addMutationEntry(new TestMutationHistoryEntry(
							        TestMutationHistoryEntry.TestMutation.CHANGE,
							        test.getStatement(pos)));
							assert ConstraintVerifier.verifyTest(test);
						}
						assert (test.isValid());
					}

					statement.getReturnValue().setDistance(oldDistance);
					position = statement.getPosition(); // Might have changed due to mutation
				}
			}
		}

		if(changed){
			assert ConstraintVerifier.verifyTest(test);
		}

		return changed;
	}

	/**
	 * With exponentially decreasing probability, insert statements at random
	 * position
	 *
	 * @return
	 */
	public boolean mutationInsert() {
		boolean changed = false;
		final double ALPHA = Properties.P_STATEMENT_INSERTION; //0.5;
		int count = 0;
		TestFactory testFactory = TestFactory.getInstance();

		while (Randomness.nextDouble() <= Math.pow(ALPHA, count)
		        && (!Properties.CHECK_MAX_LENGTH || size() < Properties.CHROMOSOME_LENGTH)) {

			count++;
			// Insert at position as during initialization (i.e., using helper sequences)
			int position = testFactory.insertRandomStatement(test, getLastMutatableStatement());

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
		TestCase newTest = ConcolicMutation.negateCondition(branches, branch, test);

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
	public static void addSecondaryObjective(SecondaryObjective<TestChromosome> objective) {
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
	public static List<SecondaryObjective<TestChromosome>> getSecondaryObjectives() {
		return secondaryObjectives;
	}

}
