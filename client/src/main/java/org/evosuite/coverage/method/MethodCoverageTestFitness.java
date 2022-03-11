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
package org.evosuite.coverage.method;

import org.evosuite.Properties;
import org.evosuite.ga.archive.Archive;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.TestFitnessFunction;
import org.evosuite.testcase.execution.ExecutionResult;
import org.evosuite.testcase.statements.ConstructorStatement;
import org.evosuite.testcase.statements.EntityWithParametersStatement;
import org.evosuite.testcase.statements.MethodStatement;
import org.evosuite.testcase.statements.Statement;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * Fitness function for a single test on a single method (including calls that throw exceptions)
 *
 * @author Gordon Fraser, Jose Miguel Rojas
 */
public class MethodCoverageTestFitness extends TestFitnessFunction {

    private static final long serialVersionUID = 3624503060256855484L;

    /**
     * Target method
     */
    protected final String className;
    protected final String methodName;

    /**
     * Constructor - fitness is specific to a method
     *
     * @param className  the class name
     * @param methodName the method name
     */
    public MethodCoverageTestFitness(String className, String methodName) {
        this.className = Objects.requireNonNull(className, "className cannot be null");
        this.methodName = Objects.requireNonNull(methodName, "methodName cannot be null");
    }

    /**
     * <p>
     * getClassName
     * </p>
     *
     * @return a {@link String} object.
     */
    public String getClassName() {
        return className;
    }

    /**
     * <p>
     * getMethod
     * </p>
     *
     * @return a {@link String} object.
     */
    public String getMethod() {
        return methodName;
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

        List<Integer> exceptionPositions = asSortedList(result.getPositionsWhereExceptionsWereThrown());
        for (Statement stmt : result.test) {
            if (!isValidPosition(exceptionPositions, stmt.getPosition())) {
                break;
            }

            if ((stmt instanceof MethodStatement || stmt instanceof ConstructorStatement)) {
                EntityWithParametersStatement ps = (EntityWithParametersStatement) stmt;
                String className = ps.getDeclaringClassName();
                String methodDesc = ps.getDescriptor();
                String methodName = ps.getMethodName() + methodDesc;

                if (this.className.equals(className) && this.methodName.equals(methodName)) {
                    fitness = 0.0;
                    break;
                }
            }
        }

        updateIndividual(individual, fitness);

        if (fitness == 0.0) {
            individual.getTestCase().addCoveredGoal(this);
        }

        if (Properties.TEST_ARCHIVE) {
            Archive.getArchiveInstance().updateArchive(this, individual, fitness);
        }

        return fitness;
    }

    private boolean isValidPosition(List<Integer> exceptionPositions, Integer position) {
        if (Properties.BREAK_ON_EXCEPTION) {
            return exceptionPositions.isEmpty() || position <= exceptionPositions.get(0);
        } else {
            return true;
        }
    }

    private <T extends Comparable<? super T>> List<T> asSortedList(Collection<T> c) {
        List<T> list = new ArrayList<>(c);
        java.util.Collections.sort(list);
        return list;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "[METHOD] " + className + "." + methodName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        int iConst = 13;
        return 51 * iConst + className.hashCode() * iConst + methodName.hashCode();
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
        MethodCoverageTestFitness other = (MethodCoverageTestFitness) obj;
        if (!className.equals(other.className)) {
            return false;
        } else return methodName.equals(other.methodName);
    }

    /* (non-Javadoc)
     * @see org.evosuite.testcase.TestFitnessFunction#compareTo(org.evosuite.testcase.TestFitnessFunction)
     */
    @Override
    public int compareTo(TestFitnessFunction other) {
        if (other instanceof MethodCoverageTestFitness) {
            MethodCoverageTestFitness otherMethodFitness = (MethodCoverageTestFitness) other;
            if (className.equals(otherMethodFitness.getClassName()))
                return methodName.compareTo(otherMethodFitness.getMethod());
            else
                return className.compareTo(otherMethodFitness.getClassName());
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
