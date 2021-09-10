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
package org.evosuite.coverage.mutation;

import org.evosuite.TestGenerationContext;
import org.evosuite.coverage.ControlFlowDistance;
import org.evosuite.coverage.branch.BranchCoverageGoal;
import org.evosuite.ga.stoppingconditions.MaxStatementsStoppingCondition;
import org.evosuite.graphs.GraphPool;
import org.evosuite.graphs.cfg.ActualControlFlowGraph;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.TestFitnessFunction;
import org.evosuite.testcase.execution.ExecutionResult;
import org.evosuite.testcase.execution.TestCaseExecutor;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashSet;
import java.util.Set;

/**
 * <p>
 * Abstract MutationTestFitness class.
 * </p>
 *
 * @author Gordon Fraser
 */
public abstract class MutationTestFitness extends TestFitnessFunction {

    private static final long serialVersionUID = 596930765039928708L;

    protected transient Mutation mutation;

    protected int mutantId;

    protected final Set<BranchCoverageGoal> controlDependencies = new HashSet<>();

    protected final int diameter;

    /**
     * <p>
     * Constructor for MutationTestFitness.
     * </p>
     *
     * @param mutation a {@link org.evosuite.coverage.mutation.Mutation} object.
     */
    public MutationTestFitness(Mutation mutation) {
        this.mutation = mutation;
        this.mutantId = mutation.getId();
        controlDependencies.addAll(mutation.getControlDependencies());
        ActualControlFlowGraph cfg = GraphPool.getInstance(TestGenerationContext.getInstance().getClassLoaderForSUT()).getActualCFG(mutation.getClassName(),
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

    /**
     * {@inheritDoc}
     */
    @Override
    public ExecutionResult runTest(TestCase test) {
        return runTest(test, null);
    }

    /**
     * <p>
     * runTest
     * </p>
     *
     * @param test   a {@link org.evosuite.testcase.TestCase} object.
     * @param mutant a {@link org.evosuite.coverage.mutation.Mutation} object.
     * @return a {@link org.evosuite.testcase.execution.ExecutionResult} object.
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
            result = TestCaseExecutor.getInstance().execute(test);
            if (mutant != null)
                MutationObserver.deactivateMutation(mutant);

            int num = test.size();
            if (!result.noThrownExceptions()) {
                num = result.getFirstPositionOfThrownException();
            }

            //if (mutant == null)
            MaxStatementsStoppingCondition.statementsExecuted(num);

        } catch (Exception e) {
            throw new Error(e);
        }

        return result;
    }

    /**
     * <p>
     * getExecutionDistance
     * </p>
     *
     * @param result a {@link org.evosuite.testcase.execution.ExecutionResult} object.
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

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract double getFitness(TestChromosome individual, ExecutionResult result);

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */

    /**
     * {@inheritDoc}
     */
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
        return compareClassName(other);
    }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((controlDependencies == null) ? 0 : controlDependencies.hashCode());
        result = prime * result + diameter;
        result = prime * result + mutantId;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        MutationTestFitness other = (MutationTestFitness) obj;
        if (controlDependencies == null) {
            if (other.controlDependencies != null)
                return false;
        } else if (!controlDependencies.equals(other.controlDependencies))
            return false;
        if (diameter != other.diameter)
            return false;
        return mutantId == other.mutantId;
    }

    /* (non-Javadoc)
     * @see org.evosuite.testcase.TestFitnessFunction#getTargetClass()
     */
    @Override
    public String getTargetClass() {
        return mutation.getClassName();
    }

    /* (non-Javadoc)
     * @see org.evosuite.testcase.TestFitnessFunction#getTargetMethod()
     */
    @Override
    public String getTargetMethod() {
        return mutation.getMethodName();
    }

    private void writeObject(ObjectOutputStream oos) throws IOException {
        oos.defaultWriteObject();
        oos.writeInt(mutantId);
    }

    private void readObject(ObjectInputStream ois) throws ClassNotFoundException,
            IOException {
        ois.defaultReadObject();

        mutantId = ois.readInt();
        this.mutation = MutationPool.getInstance(TestGenerationContext.getInstance().getClassLoaderForSUT()).getMutant(mutantId);
        assert (this.mutation != null) : "mutation id not found " + mutantId;
    }
}
