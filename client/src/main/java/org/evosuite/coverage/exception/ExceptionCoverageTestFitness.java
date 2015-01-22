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
package org.evosuite.coverage.exception;

import org.evosuite.Properties;
import org.evosuite.testcase.*;
import org.evosuite.testcase.statements.ConstructorStatement;
import org.evosuite.testcase.statements.MethodStatement;
import org.objectweb.asm.Type;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * Fitness function for a single test on a single exception
 *
 * @author Gordon Fraser, Jose Miguel Rojas
 */
public class ExceptionCoverageTestFitness extends TestFitnessFunction {


    private static final long serialVersionUID = 1221020001417476348L;

    protected final String methodName;
    protected final Class<?> clazz;

    /**
     * Constructor - fitness is specific to a method
     * @param methodName the method name
     * @param clazz the exception class
     * @throws IllegalArgumentException
     */
    public ExceptionCoverageTestFitness(String methodName, Class<?> clazz) throws IllegalArgumentException{
        if ((methodName == null) || (clazz == null)) {
            throw new IllegalArgumentException("method name and exception class cannot be null");
        }
        this.clazz = clazz;
        this.methodName = methodName;
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
     *            a {@link org.evosuite.testcase.ExecutionResult} object.
     * @return a double.
     */
    @Override
    public double getFitness(TestChromosome individual, ExecutionResult result) {
        double fitness = 1.0;

        //iterate on the indexes of the statements that resulted in an exception
        for (Integer i : result.getPositionsWhereExceptionsWereThrown()) {
            if (i >= result.test.size()) {
                // Timeouts are put after the last statement if the process was forcefully killed
                continue;
            }
            //not interested in security exceptions when Sandbox is active
            Throwable t = result.getExceptionThrownAtPosition(i);
            if (t instanceof SecurityException && Properties.SANDBOX){
                continue;
            }

            // If the exception was thrown in the test directly, it is also not interesting
            if (t.getStackTrace().length > 0
                    && t.getStackTrace()[0].getClassName().startsWith("org.evosuite.testcase")) {
                continue;
            }

            // Ignore exceptions thrown in the test code itself
            if (t instanceof CodeUnderTestException){
                continue;
            }

            String methodName = "";
            boolean sutException = false;

            if (result.test.getStatement(i) instanceof MethodStatement) {
                MethodStatement ms = (MethodStatement) result.test.getStatement(i);
                Method method = ms.getMethod().getMethod();
                methodName = method.getName() + Type.getMethodDescriptor(method);

                if (method.getDeclaringClass().equals(Properties.getTargetClass())){
                    sutException = true;
                }

            } else if (result.test.getStatement(i) instanceof ConstructorStatement) {
                ConstructorStatement cs = (ConstructorStatement) result.test.getStatement(i);
                Constructor<?> constructor = cs.getConstructor().getConstructor();
                methodName = "<init>" + Type.getConstructorDescriptor(constructor);

                if (constructor.getDeclaringClass().equals(Properties.getTargetClass())){
                    sutException = true;
                }
            }

				/*
				 * We only consider exceptions that were thrown directly in the SUT (not called libraries)
				 */

            if (sutException) {
					/*
					 * FIXME: we need to distinguish whether it is explicit (ie "throw" in the code, eg for validating
					 * input for pre-condition) or implicit ("likely" a real fault).
					 */

                if (this.methodName.equals(methodName) && this.clazz.equals(t.getClass()))
                    fitness = 0.0;
            }
        }
        return fitness;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return methodName + clazz.getName();
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        int iConst = 17;
        return 53 * iConst + methodName.hashCode() * iConst + clazz.hashCode();
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
        ExceptionCoverageTestFitness other = (ExceptionCoverageTestFitness) obj;
        if (! methodName.equals(other.methodName)) {
            return false;
        } else
            return clazz.equals(other.getClazz());
    }

    /* (non-Javadoc)
     * @see org.evosuite.testcase.TestFitnessFunction#compareTo(org.evosuite.testcase.TestFitnessFunction)
     */
    @Override
    public int compareTo(TestFitnessFunction other) {
        if (other instanceof ExceptionCoverageTestFitness) {
            ExceptionCoverageTestFitness otherMethodFitness = (ExceptionCoverageTestFitness) other;
            if (methodName.equals(otherMethodFitness.getMethod())) {
                if (clazz.equals(((ExceptionCoverageTestFitness) other).getClazz()))
                    return 0;
                else
                    return clazz.getName().compareTo(otherMethodFitness.getClazz().getName());
            } else
                return methodName.compareTo(otherMethodFitness.getMethod());
        }
        return 0;
    }


    /* (non-Javadoc)
     * @see org.evosuite.testcase.TestFitnessFunction#getTargetClass()
     */
    @Override
    public String getTargetClass() {
        return null;
    }

    /* (non-Javadoc)
     * @see org.evosuite.testcase.TestFitnessFunction#getTargetMethod()
     */
    @Override
    public String getTargetMethod() {
        return getMethod();
    }

    public Class<?> getClazz() {
        return clazz;
    }
}