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
package org.evosuite.coverage.output;

import org.evosuite.testcase.ExecutionResult;
import org.evosuite.testcase.MethodCall;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.TestFitnessFunction;

/**
 * @author Jose Miguel Rojas
 *
 */
public class OutputCoverageTestFitness extends TestFitnessFunction {

	private static final long serialVersionUID = 1383064944691491355L;

	/** Target method */
	private final String className;
	private final String methodName;
	private final String value;
	
	/**
	 * Constructor - fitness is specific to a method
	 * @param className the class name
	 * @param methodName the method name
	 * @throws IllegalArgumentException
	 */
	public OutputCoverageTestFitness(String className, String methodName, String value) throws IllegalArgumentException{
		if ((className == null) || (methodName == null)) {
			throw new IllegalArgumentException("className and methodName cannot be null");
		}
		this.className = className;
		this.methodName = methodName;
		this.value = value;
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
	 * getValue
	 * </p>
	 * 
	 * @return a {@link java.lang.String} object.
	 */
	public String getValue() {
		return value;
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
		for (MethodCall call : result.getTrace().getMethodCalls()) {
			if (call.className.equals(className) && call.methodName.equals(methodName)) {
				fitness = 0.0;
				break;
			}
		}
		updateIndividual(this, individual, fitness);
		return fitness;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return className + "." + methodName + " : " + value;
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
		OutputCoverageTestFitness other = (OutputCoverageTestFitness) obj;
		if (className != other.className) {
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
		if (other instanceof OutputCoverageTestFitness) {
			OutputCoverageTestFitness otherOutputFitness = (OutputCoverageTestFitness) other;
			if (className.equals(otherOutputFitness.getClassName()))
				return methodName.compareTo(otherOutputFitness.getMethod());
			else
				return className.compareTo(otherOutputFitness.getClassName());
		} else
			return -1;
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