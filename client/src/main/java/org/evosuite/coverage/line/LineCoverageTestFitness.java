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
package org.evosuite.coverage.line;

import org.evosuite.Properties;
import org.evosuite.TestGenerationContext;
import org.evosuite.coverage.branch.BranchCoverageFactory;
import org.evosuite.coverage.branch.BranchCoverageTestFitness;
import org.evosuite.ga.archive.Archive;
import org.evosuite.graphs.GraphPool;
import org.evosuite.graphs.cfg.BytecodeInstruction;
import org.evosuite.graphs.cfg.BytecodeInstructionPool;
import org.evosuite.graphs.cfg.ControlDependency;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.TestFitnessFunction;
import org.evosuite.testcase.execution.ExecutionResult;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.*;
import java.util.stream.Stream;

/**
 * Fitness function for a single test on a single line
 *
 * @author Gordon Fraser, Jose Miguel Rojas
 */
public class LineCoverageTestFitness extends TestFitnessFunction {

    private static final long serialVersionUID = 3624503060256855484L;

    /**
     * Target line
     */
    private final String className;
    private final String methodName;
    private final Integer line;

    protected transient BytecodeInstruction goalInstruction;
    protected transient List<BranchCoverageTestFitness> branchFitnesses = new ArrayList<>();

    /**
     * Constructor - fitness is specific to a method
     *
     * @param className  the class name
     * @param methodName the method name
     * @throws IllegalArgumentException
     */
    public LineCoverageTestFitness(String className, String methodName, Integer line) {
        this.className = Objects.requireNonNull(className, "className cannot be null");
        this.methodName = Objects.requireNonNull(methodName, "methodName cannot be null");
        this.line = Objects.requireNonNull(line, "line number cannot be null");
        setupDependencies();
    }

    /**
     * <p>
     * getClassName
     * </p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getClassName() {
        return className;
    }

    /**
     * <p>
     * getMethod
     * </p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getMethod() {
        return methodName;
    }

    /**
     * <p>
     * getLine
     * </p>
     *
     * @return a {@link java.lang.Integer} object.
     */
    public Integer getLine() {
        return line;
    }


    private void setupDependencies() {
        goalInstruction = BytecodeInstructionPool.getInstance(TestGenerationContext.getInstance().getClassLoaderForSUT()).getFirstInstructionAtLineNumber(className, methodName, line);

        if (goalInstruction == null)
            return;

        Set<ControlDependency> cds = goalInstruction.getControlDependencies();

        for (ControlDependency cd : cds) {
            BranchCoverageTestFitness fitness = BranchCoverageFactory.createBranchCoverageTestFitness(cd);

            branchFitnesses.add(fitness);
        }

        if (goalInstruction.isRootBranchDependent())
            branchFitnesses.add(BranchCoverageFactory.createRootBranchTestFitness(goalInstruction));

        if (cds.isEmpty() && !goalInstruction.isRootBranchDependent())
            throw new IllegalStateException(
                    "expect control dependencies to be empty only for root dependent instructions: "
                            + this);

        if (branchFitnesses.isEmpty())
            throw new IllegalStateException(
                    "an instruction is at least on the root branch of it's method");


        branchFitnesses.sort(Comparator.naturalOrder());
    }

    @Override
    public boolean isCovered(ExecutionResult result) {
        Stream<Integer> coveredLines = result.getTrace().getCoveredLines().stream();
        return coveredLines.anyMatch(coveredLine -> coveredLine.intValue() == this.line.intValue());
    }

    /**
     * {@inheritDoc}
     * <p>
     * Calculate fitness
     *
     * @param individual a {@link org.evosuite.testcase.ExecutableChromosome} object.
     * @param result     a {@link org.evosuite.testcase.execution.ExecutionResult} object.
     * @return a double.
     */
    @Override
    public double getFitness(TestChromosome individual, ExecutionResult result) {
        double fitness = 1.0;

        // Deactivate coverage archive while measuring fitness, since branchcoverage fitness
        // evaluating will attempt to claim coverage for it in the archive
        boolean archive = Properties.TEST_ARCHIVE;
        Properties.TEST_ARCHIVE = false;
        if (result.getTrace().getCoveredLines().contains(this.line)) {
            fitness = 0.0;
        } else {
            double r = Double.MAX_VALUE;

            // Find minimum distance to satisfying any of the control dependencies
            for (BranchCoverageTestFitness branchFitness : branchFitnesses) {
                double newFitness = branchFitness.getFitness(individual, result);
                if (newFitness == 0.0) {
                    // Although the BranchCoverage goal has been covered, it is not part of the
                    // optimisation
                    individual.getTestCase().removeCoveredGoal(branchFitness);
                    // If the control dependency was covered, then likely
                    // an exception happened before the line was reached
                    newFitness = 1.0;
                } else {
                    newFitness = 1.0 + normalize(newFitness);
                }
                if (newFitness < r)
                    r = newFitness;
            }

            fitness = r;
        }
        Properties.TEST_ARCHIVE = archive;
        updateIndividual(individual, fitness);

        if (fitness == 0.0) {
            individual.getTestCase().addCoveredGoal(this);
        }

        if (Properties.TEST_ARCHIVE) {
            Archive.getArchiveInstance().updateArchive(this, individual, fitness);
        }

        return fitness;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return className + (methodName.isEmpty() ? "" : "." + methodName) + ": Line " + line;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        int iConst = 13;
        return 51 * iConst + className.hashCode() * iConst + methodName.hashCode() + iConst + line.hashCode();
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
        LineCoverageTestFitness other = (LineCoverageTestFitness) obj;
        if (!className.equals(other.className)) {
            return false;
        } else if (!methodName.equals(other.methodName)) {
            return false;
        } else {
            return line.intValue() == other.line.intValue();
        }
    }

    /* (non-Javadoc)
     * @see org.evosuite.testcase.TestFitnessFunction#compareTo(org.evosuite.testcase.TestFitnessFunction)
     */
    @Override
    public int compareTo(TestFitnessFunction other) {
        if (other == null) return 1;
        if (other instanceof LineCoverageTestFitness) {
            LineCoverageTestFitness otherLineFitness = (LineCoverageTestFitness) other;
            if (className.compareTo(otherLineFitness.getClassName()) != 0)
                return className.compareTo(otherLineFitness.getClassName());
            else if (methodName.compareTo(otherLineFitness.getMethod()) != 0)
                return methodName.compareTo(otherLineFitness.getMethod());
            else
                return line.compareTo(otherLineFitness.getLine());
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

    private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
        ois.defaultReadObject();
        branchFitnesses = new ArrayList<>();
        if (GraphPool.getInstance(TestGenerationContext.getInstance().getClassLoaderForSUT()).getActualCFG(className,
                methodName) != null) {
            // TODO: Figure out why the CFG may not exist
            setupDependencies();
        }
    }

    private void writeObject(ObjectOutputStream oos) throws ClassNotFoundException, IOException {
        oos.defaultWriteObject();
    }

}
