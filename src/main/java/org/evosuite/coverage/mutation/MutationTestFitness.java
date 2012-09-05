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
/**
 * 
 */
package org.evosuite.coverage.mutation;

import java.util.HashSet;
import java.util.Set;

import org.evosuite.coverage.ControlFlowDistance;
import org.evosuite.coverage.branch.BranchCoverageGoal;
import org.evosuite.ga.stoppingconditions.MaxStatementsStoppingCondition;
import org.evosuite.graphs.GraphPool;
import org.evosuite.graphs.cfg.ActualControlFlowGraph;
import org.evosuite.testcase.ExecutionResult;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.TestCaseExecutor;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.TestFitnessFunction;

/**
 * <p>
 * Abstract MutationTestFitness class.
 * </p>
 * 
 * @author Gordon Fraser
 */
public abstract class MutationTestFitness extends TestFitnessFunction {

	private static final long serialVersionUID = 596930765039928708L;

	protected final Mutation mutation;

	protected final Set<BranchCoverageGoal> controlDependencies = new HashSet<BranchCoverageGoal>();

	protected final int diameter;

	/**
	 * <p>
	 * Constructor for MutationTestFitness.
	 * </p>
	 * 
	 * @param mutation
	 *            a {@link org.evosuite.coverage.mutation.Mutation} object.
	 */
	public MutationTestFitness(Mutation mutation) {
		this.mutation = mutation;
		controlDependencies.addAll(mutation.getControlDependencies());
		ActualControlFlowGraph cfg = GraphPool.getActualCFG(mutation.getClassName(),
		                                                    mutation.getMethodName());
		diameter = cfg.getDiameter();
	}

	/**
	 * <p>
	 * Getter for the field <code>mutation</code>.
	 * </p>
	 * 
	 * @return a {@link org.evosuite.coverage.mutation.Mutation} object.
	 */
	public Mutation getMutation() {
		return mutation;
	}

	/** {@inheritDoc} */
	public ExecutionResult runTest(TestCase test) {
		return TestCaseExecutor.runTest(test);
	}

	/**
	 * <p>
	 * runTest
	 * </p>
	 * 
	 * @param test
	 *            a {@link org.evosuite.testcase.TestCase} object.
	 * @param mutant
	 *            a {@link org.evosuite.coverage.mutation.Mutation} object.
	 * @return a {@link org.evosuite.testcase.ExecutionResult} object.
	 */
	public static ExecutionResult runTest(TestCase test, Mutation mutant) {

		ExecutionResult result = new ExecutionResult(test, mutant);

		try {
			if (mutant != null)
				logger.debug("Executing test for mutant " + mutant.getId() + ": \n"
				        + test.toCode());
			else
				logger.debug("Executing test witout mutant");

			if (mutant != null)
				MutationObserver.activateMutation(mutant);
			result = executor.execute(test);
			if (mutant != null)
				MutationObserver.deactivateMutation(mutant);

			int num = test.size();
			if (!result.noThrownExceptions()) {
				num = result.getFirstPositionOfThrownException();
			}

			//if (mutant == null)
			MaxStatementsStoppingCondition.statementsExecuted(num);

		} catch (Exception e) {
			System.out.println("TG: Exception caught: " + e);
			e.printStackTrace();
			System.exit(1);
		}

		return result;
	}

	/**
	 * <p>
	 * getExecutionDistance
	 * </p>
	 * 
	 * @param result
	 *            a {@link org.evosuite.testcase.ExecutionResult} object.
	 * @return a double.
	 */
	protected double getExecutionDistance(ExecutionResult result) {
		double fitness = 0.0;
		if (!result.getTrace().wasMutationTouched(mutation.getId()))
			fitness += diameter;

		// Get control flow distance
		if (controlDependencies.isEmpty()) {
			// If mutant was not executed, this can be either because of an exception, or because the method was not executed

			String key = mutation.getClassName() + "." + mutation.getMethodName();
			if (result.getTrace().getCoveredMethods().contains(key)) {
				logger.debug("Target method " + key + " was executed");
			} else {
				logger.debug("Target method " + key + " was not executed");
				fitness += diameter;
			}
		} else {
			ControlFlowDistance cfgDistance = null;
			for (BranchCoverageGoal dependency : controlDependencies) {
				logger.debug("Checking dependency...");
				ControlFlowDistance distance = dependency.getDistance(result);
				if (cfgDistance == null)
					cfgDistance = distance;
				else {
					if (distance.compareTo(cfgDistance) < 0)
						cfgDistance = distance;
				}
			}
			if (cfgDistance != null) {
				logger.debug("Found control dependency");
				fitness += cfgDistance.getResultingBranchFitness();
			}
		}

		return fitness;
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.TestFitnessFunction#getFitness(org.evosuite.testcase.TestChromosome, org.evosuite.testcase.ExecutionResult)
	 */
	/** {@inheritDoc} */
	@Override
	public abstract double getFitness(TestChromosome individual, ExecutionResult result);

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	/** {@inheritDoc} */
	@Override
	public String toString() {
		return mutation.toString();
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.TestFitnessFunction#compareTo(org.evosuite.testcase.TestFitnessFunction)
	 */
	@Override
	public int compareTo(TestFitnessFunction other) {
		if (other instanceof MutationTestFitness) {
			return mutation.compareTo(((MutationTestFitness) other).getMutation());
		}
		return 0;
	}
}
