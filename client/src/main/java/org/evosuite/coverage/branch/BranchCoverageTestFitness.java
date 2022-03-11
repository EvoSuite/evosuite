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
package org.evosuite.coverage.branch;

import org.evosuite.Properties;
import org.evosuite.coverage.ControlFlowDistance;
import org.evosuite.ga.archive.Archive;
import org.evosuite.testcase.ExecutableChromosome;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.TestFitnessFunction;
import org.evosuite.testcase.execution.ExecutionResult;
import org.evosuite.testcase.execution.MethodCall;
import org.evosuite.utils.ArrayUtil;

import java.util.Objects;

/**
 * Fitness function for a single test on a single branch
 *
 * @author Gordon Fraser
 */
public class BranchCoverageTestFitness extends TestFitnessFunction {

    private static final long serialVersionUID = -6310967747257242580L;

    /**
     * Target branch
     */
    private final BranchCoverageGoal goal;

    /**
     * Constructor - fitness is specific to a branch
     *
     * @param goal a {@link org.evosuite.coverage.branch.BranchCoverageGoal}
     *             object.
     */
    public BranchCoverageTestFitness(BranchCoverageGoal goal) {
        this.goal = Objects.requireNonNull(goal, "goal cannot be null");
    }

    /**
     * <p>
     * getBranch
     * </p>
     *
     * @return a {@link org.evosuite.coverage.branch.Branch} object.
     */
    public Branch getBranch() {
        return goal.getBranch();
    }

    public boolean getValue() {
        return goal.getValue();
    }

    public BranchCoverageGoal getBranchGoal() {
        return goal;
    }

    /**
     * <p>
     * getClassName
     * </p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getClassName() {
        return goal.getClassName();
    }

    /**
     * <p>
     * getMethod
     * </p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getMethod() {
        return goal.getMethodName();
    }

    /**
     * <p>
     * getBranchExpressionValue
     * </p>
     *
     * @return a boolean.
     */
    public boolean getBranchExpressionValue() {
        return goal.getValue();
    }

    /**
     * <p>
     * getUnfitness
     * </p>
     *
     * @param individual a {@link org.evosuite.testcase.ExecutableChromosome} object.
     * @param result     a {@link org.evosuite.testcase.execution.ExecutionResult} object.
     * @return a double.
     */
    public double getUnfitness(ExecutableChromosome<?> individual, ExecutionResult result) {

        double sum = 0.0;
        boolean methodExecuted = false;

        for (MethodCall call : result.getTrace().getMethodCalls()) {
            if (call.className.equals(goal.getClassName())
                    && call.methodName.equals(goal.getMethodName())) {
                methodExecuted = true;
                if (goal.getBranch() != null) {
                    for (int i = 0; i < call.branchTrace.size(); i++) {
                        if (call.branchTrace.get(i) == goal.getBranch().getInstruction().getInstructionId()) {
                            if (goal.getValue())
                                sum += call.falseDistanceTrace.get(i);
                            else
                                sum += call.trueDistanceTrace.get(i);
                        }
                    }
                }
            }
        }

        if (goal.getBranch() == null) {
            // logger.info("Branch is null? " + goal.branch);
            if (goal.getValue())
                sum = methodExecuted ? 1.0 : 0.0;
            else
                sum = methodExecuted ? 0.0 : 1.0;

        }

        return sum;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Calculate approach level + branch distance
     */
    @Override
    public double getFitness(TestChromosome individual, ExecutionResult result) {
        ControlFlowDistance distance = goal.getDistance(result);

        double fitness = distance.getResultingBranchFitness();

        if (logger.isDebugEnabled()) {
            logger.debug("Goal at line " + goal.getLineNumber() + ": approach level = " + distance.getApproachLevel()
                    + " / branch distance = " + distance.getBranchDistance() + ", fitness = " + fitness);
        }

        updateIndividual(individual, fitness);

        if (fitness == 0.0) {
            individual.getTestCase().addCoveredGoal(this);
        }

        if (Properties.TEST_ARCHIVE) {
            // the next if condition is needed for DynaMOSA when branch coverage is not
            // set as coverage criterion to optimize. However, branches are
            // the backbone for all other criteria and thus they are always used in DynaMOSA
            if (ArrayUtil.contains(Properties.CRITERION, Properties.Criterion.BRANCH))
                Archive.getArchiveInstance().updateArchive(this, individual, fitness);
        }

        return fitness;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return goal.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((goal == null) ? 0 : goal.hashCode());
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        BranchCoverageTestFitness other = (BranchCoverageTestFitness) obj;
        if (goal == null) {
            return other.goal == null;
        } else return goal.equals(other.goal);
    }

    /* (non-Javadoc)
     * @see org.evosuite.testcase.TestFitnessFunction#compareTo(org.evosuite.testcase.TestFitnessFunction)
     */
    @Override
    public int compareTo(TestFitnessFunction other) {
        if (other instanceof BranchCoverageTestFitness) {
            BranchCoverageTestFitness otherBranchFitness = (BranchCoverageTestFitness) other;
            return goal.compareTo(otherBranchFitness.goal);
        }
        return compareClassName(other);
    }

    /* (non-Javadoc)
     * @see org.evosuite.testcase.TestFitnessFunction#getTargetClass()
     */
    @Override
    public String getTargetClass() {
        return getClassName();
    }

    /* (non-Javadoc)
     * @see org.evosuite.testcase.TestFitnessFunction#getTargetMethod()
     */
    @Override
    public String getTargetMethod() {
        return getMethod();
    }

}
