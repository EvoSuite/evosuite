/**
 * Copyright (C) 2010-2016 Gordon Fraser, Andrea Arcuri and EvoSuite
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

import org.evosuite.testcase.*;
import org.evosuite.testcase.execution.ExecutionResult;
import org.evosuite.testcase.statements.ConstructorStatement;
import org.evosuite.testcase.statements.EntityWithParametersStatement;
import org.evosuite.testcase.statements.MethodStatement;
import org.evosuite.testcase.statements.Statement;

import java.util.Set;

/**
 * Fitness function for a single test on a single method (no exception)
 *
 * @author Gordon Fraser, Jose Miguel Rojas
 */
public class MethodCoverageTestFitness extends TestFitnessFunction {

    private static final long serialVersionUID = 3624503060256855484L;

    /** Target method */
    protected final String className;
    protected final String methodName;

    /**
     * Constructor - fitness is specific to a method
     * @param className the class name
     * @param methodName the method name
     * @throws IllegalArgumentException
     */
    public MethodCoverageTestFitness(String className, String methodName) throws IllegalArgumentException{
        if ((className == null) || (methodName == null)) {
            throw new IllegalArgumentException("className and methodName cannot be null");
        }
        this.className = className;
        this.methodName = methodName;
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
     *
     * Calculate fitness
     *
     * @param individual
     *            a {@link org.evosuite.testcase.ExecutableChromosome} object.
     * @param result
     *            a {@link org.evosuite.testcase.execution.ExecutionResult} object.
     * @return a double.
     */
    @Override
    public double getFitness(TestChromosome individual, ExecutionResult result) {
        double fitness = 1.0;

        Set<Integer> exceptionPositions = result.getPositionsWhereExceptionsWereThrown();
        for (Statement stmt : result.test) {
            if ((stmt instanceof MethodStatement || stmt instanceof ConstructorStatement)) {
                EntityWithParametersStatement ps = (EntityWithParametersStatement)stmt;
                String className  = ps.getDeclaringClassName();
                String methodDesc = ps.getDescriptor();
                String methodName = ps.getMethodName() + methodDesc;

                if (this.className.equals(className) && this.methodName.equals(methodName)) {
                    fitness = 0.0;
                    break;
                }
            }
            if(exceptionPositions.contains(stmt.getPosition()))
                break;
        }
        updateIndividual(this, individual, fitness);
        return fitness;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "[METHOD] " + className + "." + methodName;
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        int iConst = 13;
        return 51 * iConst + className.hashCode() * iConst + methodName.hashCode();
    }

    /** {@inheritDoc} */
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
        } else if (! methodName.equals(other.methodName))
            return false;
        return true;
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