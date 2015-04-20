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
package org.evosuite.coverage.line;

import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.TestFitnessFunction;
import org.evosuite.testcase.execution.ExecutionResult;

/**
 * Fitness function for a single test on a single branch
 * 
 * @author Gordon Fraser, Jose Miguel Rojas
 */
public class LineCoverageTestFitness extends TestFitnessFunction {

	private static final long serialVersionUID = 3624503060256855484L;

	/** Target line */
	private final String className;
	private final String methodName;
	private final Integer line;

	/**
	 * Constructor - fitness is specific to a method
	 * @param className the class name
	 * @param methodName the method name
	 * @throws IllegalArgumentException
	 */
	public LineCoverageTestFitness(String className, String methodName, Integer line) throws IllegalArgumentException{
		if ((className == null) || (methodName == null) || (line == null)) {
			throw new IllegalArgumentException("className, methodName and line number cannot be null");
		}
		this.className = className;
		this.methodName = methodName;
		this.line = line;
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
		for ( Integer coveredLine : result.getTrace().getCoveredLines()) {
			if (coveredLine.intValue() == this.line.intValue()) {
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
		return className + (methodName == "" ? "" : "." + methodName) + ": Line " + line;
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		int iConst = 13;
		return 51 * iConst + className.hashCode() * iConst + methodName.hashCode() + iConst + line.hashCode();        
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
		LineCoverageTestFitness other = (LineCoverageTestFitness) obj;
		if (className != other.className) {
			return false;
		} else if (! methodName.equals(other.methodName)) {
			return false;
		} else if (line.intValue() != other.line.intValue())
			return false;
		return true;
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
			else
				if (methodName.compareTo(otherLineFitness.getMethod()) != 0)
					return methodName.compareTo(otherLineFitness.getMethod());
				else
					return line.compareTo(otherLineFitness.getLine());
		}
		return 0;
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