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
package org.evosuite.coverage.statement;

import org.evosuite.Properties;
import org.evosuite.TestGenerationContext;
import org.evosuite.coverage.branch.BranchCoverageFactory;
import org.evosuite.coverage.branch.BranchCoverageTestFitness;
import org.evosuite.ga.archive.Archive;
import org.evosuite.graphs.cfg.BytecodeInstruction;
import org.evosuite.graphs.cfg.BytecodeInstructionPool;
import org.evosuite.graphs.cfg.ControlDependency;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.TestFitnessFunction;
import org.evosuite.testcase.execution.ExecutionResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class StatementCoverageTestFitness extends TestFitnessFunction {

    private static final long serialVersionUID = 5222436175279169394L;

    /**
     * Target statement
     */
    private final String className;
    private final String methodName;
    private final Integer instructionID;

    protected final List<BranchCoverageTestFitness> branchFitnesses = new ArrayList<>();

    protected transient BytecodeInstruction goalInstruction;

    /**
     * <p>
     * Constructor for StatementCoverageTestFitness.
     * </p>
     *
     * @param goalInstruction a {@link org.evosuite.graphs.cfg.BytecodeInstruction} object.
     */
    public StatementCoverageTestFitness(BytecodeInstruction goalInstruction) {
        Objects.requireNonNull(goalInstruction);

        this.className = goalInstruction.getClassName();
        this.methodName = goalInstruction.getMethodName();
        this.instructionID = goalInstruction.getInstructionId();

        this.setupDependencies(goalInstruction);
    }

    /**
     * <p>
     * Constructor for StatementCoverageTestFitness.
     * </p>
     *
     * @param className     the class name
     * @param methodName    the method name
     * @param instructionID the instruction identifier
     */
    public StatementCoverageTestFitness(String className, String methodName, Integer instructionID) {
        this.className = Objects.requireNonNull(className, "className cannot be null");
        this.methodName = Objects.requireNonNull(methodName, "methodName cannot be null");
        this.instructionID = Objects.requireNonNull(instructionID, "instructionID cannot be null");

        BytecodeInstruction goalInstruction = BytecodeInstructionPool.getInstance(TestGenerationContext.getInstance().
                getClassLoaderForSUT()).getInstruction(this.className, this.methodName, this.instructionID);
        this.setupDependencies(goalInstruction);
    }

    private void setupDependencies(BytecodeInstruction goalInstruction) {
        this.goalInstruction = goalInstruction;

        Set<ControlDependency> cds = goalInstruction.getControlDependencies();
        for (ControlDependency cd : cds) {
            BranchCoverageTestFitness fitness = BranchCoverageFactory.createBranchCoverageTestFitness(cd);

            this.branchFitnesses.add(fitness);
        }

        if (goalInstruction.isRootBranchDependent())
            this.branchFitnesses.add(BranchCoverageFactory.createRootBranchTestFitness(goalInstruction));

        if (cds.isEmpty() && !goalInstruction.isRootBranchDependent())
            throw new IllegalStateException(
                    "expect control dependencies to be empty only for root dependent instructions: "
                            + this);

        if (this.branchFitnesses.isEmpty())
            throw new IllegalStateException(
                    "an instruction is at least on the root branch of it's method");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getFitness(TestChromosome individual, ExecutionResult result) {
        if (this.branchFitnesses.isEmpty())
            throw new IllegalStateException(
                    "expect to know at least one fitness for goalInstruction");

        if (result.hasTimeout() || result.hasTestException()) {
            updateIndividual(individual, Double.MAX_VALUE);
            return Double.MAX_VALUE;
        }

        double r = Double.MAX_VALUE;

        // Deactivate coverage archive while measuring fitness, since BranchCoverage fitness
        // evaluating will attempt to claim coverage for it in the archive
        boolean archive = Properties.TEST_ARCHIVE;
        Properties.TEST_ARCHIVE = false;

        // Find minimum distance to satisfying any of the control dependencies
        for (BranchCoverageTestFitness branchFitness : this.branchFitnesses) {
            double newFitness = branchFitness.getFitness(individual, result);
            if (newFitness == 0.0) {
                r = 0.0;
                // Although the BranchCoverage goal has been covered, it is not part of the
                // optimisation
                individual.getTestCase().removeCoveredGoal(branchFitness);
                break;
            }
            if (newFitness < r)
                r = newFitness;
        }

        Properties.TEST_ARCHIVE = archive;

        updateIndividual(individual, r);

        if (r == 0.0) {
            individual.getTestCase().addCoveredGoal(this);
        }

        if (Properties.TEST_ARCHIVE) {
            Archive.getArchiveInstance().updateArchive(this, individual, r);
        }

        return r;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        StringBuilder r = new StringBuilder();

        r.append("StatementCoverageTestFitness for ");
        r.append("\tClass: " + this.className);
        r.append("\tMethod: " + this.methodName);
        r.append("\tInstructionID: " + this.instructionID);

        r.append("\n");
        r.append("CDS:\n");
        for (BranchCoverageTestFitness branchFitness : this.branchFitnesses) {
            r.append("\t" + branchFitness.toString());
        }
        return r.toString();
    }

    /* (non-Javadoc)
     * @see org.evosuite.testcase.TestFitnessFunction#compareTo(org.evosuite.testcase.TestFitnessFunction)
     */
    @Override
    public int compareTo(TestFitnessFunction other) {
        if (other == null) {
            return 1;
        }

        if (other instanceof StatementCoverageTestFitness) {
            StatementCoverageTestFitness otherStatementFitness = (StatementCoverageTestFitness) other;
            if (this.getTargetClass().compareTo(otherStatementFitness.getTargetClass()) != 0) {
                return this.getTargetClass().compareTo(otherStatementFitness.getTargetClass());
            } else if (this.getTargetMethod().compareTo(otherStatementFitness.getTargetMethod()) != 0) {
                return this.getTargetMethod().compareTo(otherStatementFitness.getTargetMethod());
            } else if (this.instructionID.compareTo(otherStatementFitness.instructionID) != 0) {
                return this.instructionID - otherStatementFitness.instructionID;
            }
        }

        return compareClassName(other);
    }

    @Override
    public int hashCode() {
        final int iConst = 13;
        return 51 * iConst + this.getTargetClass().hashCode() * iConst +
                this.getTargetMethod().hashCode() * iConst + this.instructionID * iConst;
    }

    public List<BranchCoverageTestFitness> getBranchFitnesses() {
        return branchFitnesses;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        StatementCoverageTestFitness other = (StatementCoverageTestFitness) obj;
        if (!this.className.equals(other.className)) {
            return false;
        } else if (!this.methodName.equals(other.methodName)) {
            return false;
        } else return this.instructionID.intValue() == other.instructionID.intValue();
    }

    /* (non-Javadoc)
     * @see org.evosuite.testcase.TestFitnessFunction#getTargetClass()
     */
    @Override
    public String getTargetClass() {
        return this.className;
    }

    /* (non-Javadoc)
     * @see org.evosuite.testcase.TestFitnessFunction#getTargetMethod()
     */
    @Override
    public String getTargetMethod() {
        return this.methodName;
    }

    public BytecodeInstruction getGoalInstruction() {
        return this.goalInstruction;
    }
}
