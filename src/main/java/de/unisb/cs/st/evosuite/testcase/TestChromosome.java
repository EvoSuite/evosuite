/*
 * Copyright (C) 2010 Saarland University
 * 
 * This file is part of EvoSuite.
 * 
 * EvoSuite is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * EvoSuite is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser Public License along with
 * EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */

package de.unisb.cs.st.evosuite.testcase;

import java.util.ArrayList;
import java.util.List;

import de.unisb.cs.st.evosuite.Properties;
import de.unisb.cs.st.evosuite.coverage.mutation.Mutation;
import de.unisb.cs.st.evosuite.coverage.mutation.MutationExecutionResult;
import de.unisb.cs.st.evosuite.ga.Chromosome;
import de.unisb.cs.st.evosuite.ga.ConstructionFailedException;
import de.unisb.cs.st.evosuite.ga.GeneticAlgorithm;
import de.unisb.cs.st.evosuite.ga.LocalSearchBudget;
import de.unisb.cs.st.evosuite.ga.LocalSearchObjective;
import de.unisb.cs.st.evosuite.ga.SecondaryObjective;
import de.unisb.cs.st.evosuite.symbolic.BranchCondition;
import de.unisb.cs.st.evosuite.symbolic.ConcolicExecution;
import de.unisb.cs.st.evosuite.symbolic.ConcolicMutation;
import de.unisb.cs.st.evosuite.testsuite.CurrentChromosomeTracker;
import de.unisb.cs.st.evosuite.testsuite.TestSuiteFitnessFunction;
import de.unisb.cs.st.evosuite.utils.Randomness;

/**
 * Chromosome representation of test cases
 * 
 * @author Gordon Fraser
 * 
 */
public class TestChromosome extends ExecutableChromosome {

	private static final long serialVersionUID = 7532366007973252782L;

	/** The test case encoded in this chromosome */
	protected TestCase test = new DefaultTestCase();

	/** Secondary objectives used during ranking */
	private static final List<SecondaryObjective> secondaryObjectives = new ArrayList<SecondaryObjective>();

	public void setTestCase(TestCase testCase) {
		test = testCase;
	}

	public TestCase getTestCase() {
		return test;
	}

	@Override
	public void setChanged(boolean changed) {
		super.setChanged(changed);
		if (changed)
			clearCachedResults();
		CurrentChromosomeTracker.getInstance().changed(this);
	}

	/**
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

		return c;
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.ExecutableChromosome#copyCachedResults(de.unisb.cs.st.evosuite.testcase.ExecutableChromosome)
	 */
	@Override
	protected void copyCachedResults(ExecutableChromosome other) {
		if (test == null)
			throw new RuntimeException("Test is null!");
		this.lastExecutionResult = other.lastExecutionResult; //.clone();
		if (this.lastExecutionResult != null) {
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
	 * Single point cross over
	 * 
	 * @throws ConstructionFailedException
	 */
	@Override
	public void crossOver(Chromosome other, int position1, int position2)
	        throws ConstructionFailedException {
		logger.debug("Crossover starting");
		TestChromosome offspring = new TestChromosome();
		DefaultTestFactory test_factory = DefaultTestFactory.getInstance();

		for (int i = 0; i < position1; i++) {
			offspring.test.addStatement(test.getStatement(i).clone(offspring.test));
		}
		for (int i = position2; i < other.size(); i++) {
			test_factory.appendStatement(offspring.test,
			                             ((TestChromosome) other).test.getStatement(i));
		}
		if (!Properties.CHECK_MAX_LENGTH
		        || offspring.test.size() <= Properties.CHROMOSOME_LENGTH) {
			test = offspring.test;
		}

		setChanged(true);
	}

	/**
	 * Two chromosomes are equal if their tests are equal
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
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

	@Override
	public int hashCode() {
		return test.hashCode();
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.ga.Chromosome#localSearch()
	 */
	@Override
	public void localSearch(LocalSearchObjective objective) {
		//logger.info("Test before local search: " + test.toCode());
		double oldFitness = getFitness();

		for (int i = 0; i < test.size(); i++) {
			if (LocalSearchBudget.isFinished())
				break;

			LocalSearch search = null;
			if (test.getStatement(i) instanceof PrimitiveStatement<?>) {
				Class<?> type = test.getReturnValue(i).getVariableClass();
				if (type.equals(Integer.class) || type.equals(int.class)) {
					search = new IntegerLocalSearch<Integer>();
				} else if (type.equals(Byte.class) || type.equals(byte.class)) {
					search = new IntegerLocalSearch<Byte>();
				} else if (type.equals(Short.class) || type.equals(short.class)) {
					search = new IntegerLocalSearch<Short>();
				} else if (type.equals(Long.class) || type.equals(long.class)) {
					search = new IntegerLocalSearch<Long>();
				} else if (type.equals(Character.class) || type.equals(char.class)) {
					search = new IntegerLocalSearch<Character>();
				} else if (type.equals(Float.class) || type.equals(float.class)) {
					search = new FloatLocalSearch<Float>();
				} else if (type.equals(Double.class) || type.equals(double.class)) {
					search = new FloatLocalSearch<Double>();
				} else if (type.equals(String.class)) {
					search = new StringLocalSearch();
				} else if (type.equals(Boolean.class)) {
					search = new BooleanLocalSearch();
				}

			} else if (test.getStatement(i) instanceof ArrayStatement) {
				search = new ArrayLocalSearch();
			}
			if (search != null)
				search.doSearch(this, i, objective);
		}
		assert (getFitness() <= oldFitness);
		//logger.info("Test after local search: " + test.toCode());

		// TODO: Handle arrays in local search
		// TODO: mutating an int might have an effect on array lengths
	}

	/**
	 * Each statement is mutated with probability 1/l
	 */
	@Override
	public void mutate() {
		boolean changed = false;

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
		DefaultTestFactory test_factory = DefaultTestFactory.getInstance();

		for (int num = test.size() - 1; num >= 0; num--) {

			// Each statement is deleted with probability 1/l
			if (Randomness.nextDouble() <= pl) {
				// if(!test.hasReferences(test.getStatement(num).getReturnValue()))
				// {
				try {
					TestCase copy = test.clone();
					// test_factory.deleteStatement(test, num);
					changed = true;
					test_factory.deleteStatementGracefully(copy, num);
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
		DefaultTestFactory test_factory = DefaultTestFactory.getInstance();

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
					if (statement.mutate(test, test_factory)) {
						changed = true;
						assert (test.isValid());
					} else if (!statement.isAssignmentStatement()) {
						if (test_factory.changeRandomCall(test, statement))
							changed = true;
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
		DefaultTestFactory test_factory = DefaultTestFactory.getInstance();

		while (Randomness.nextDouble() <= Math.pow(ALPHA, count)
		        && (!Properties.CHECK_MAX_LENGTH || size() < Properties.CHROMOSOME_LENGTH)) {
			count++;
			// Insert at position as during initialization (i.e., using helper
			// sequences)
			test_factory.insertRandomStatement(test);
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
		ConcolicExecution concolicExecution = new ConcolicExecution();

		// Apply DSE to gather constraints
		List<BranchCondition> branches = concolicExecution.getSymbolicPath(this);
		logger.debug("Conditions: " + branches);
		if (branches.isEmpty())
			return false;

		boolean mutated = false;
		List<BranchCondition> targetBranches = new ArrayList<BranchCondition>();
		for (BranchCondition branch : branches) {
			if (StaticTestCluster.isTargetClassName(branch.ins.getMethodInfo().getClassName()))
				targetBranches.add(branch);
		}
		// Select random branch
		BranchCondition branch = null;
		if (targetBranches.isEmpty())
			branch = Randomness.choice(branches);
		else
			branch = Randomness.choice(targetBranches);

		logger.debug("Trying to negate branch " + branch.ins.getInstructionIndex()
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
			this.changed = true;
			this.lastExecutionResult = null;
		} else {
			logger.debug("CONCOLIC: Did not create new test");
		}

		return mutated;
	}

	/**
	 * The size of a chromosome is the length of its test case
	 */
	@Override
	public int size() {
		return test.size();
	}

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

	@Override
	public String toString() {
		return test.toCode();
	}

	public boolean hasException() {
		return lastExecutionResult == null ? false
		        : !lastExecutionResult.exceptions.isEmpty();
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.ga.Chromosome#applyDSE()
	 */
	@Override
	public void applyDSE(GeneticAlgorithm ga) {
		// TODO Auto-generated method stub
	}

	@Override
	public ExecutionResult executeForFitnessFunction(
	        TestSuiteFitnessFunction testSuiteFitnessFunction) {
		return testSuiteFitnessFunction.runTest(this.test);
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.ga.Chromosome#compareSecondaryObjective(de.unisb.cs.st.evosuite.ga.Chromosome)
	 */
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
	 */
	public static void addSecondaryObjective(SecondaryObjective objective) {
		secondaryObjectives.add(objective);
	}

	/**
	 * Remove secondary objective from list, if it is there
	 * 
	 * @param objective
	 */
	public static void removeSecondaryObjective(SecondaryObjective objective) {
		secondaryObjectives.remove(objective);
	}

	/**
	 * 
	 * @return
	 */
	public static List<SecondaryObjective> getSecondaryObjectives() {
		return secondaryObjectives;
	}

}
