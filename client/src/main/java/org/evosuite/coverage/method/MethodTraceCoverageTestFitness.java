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

import java.util.Objects;

/**
 * Fitness function for a single test on a single method.
 * 
 * @author Gordon Fraser, Jose Miguel Rojas
 */
public class MethodTraceCoverageTestFitness extends TestFitnessFunction {

    private static final long serialVersionUID = -8880071948317243336L;

    /** Target method */
	protected final String className;
	protected final String methodName;

	/**
	 * Constructor - fitness is specific to a method
	 * @param className the class name
	 * @param methodName the method name
	 */
	public MethodTraceCoverageTestFitness(String className, String methodName) throws IllegalArgumentException{
		this.className = Objects.requireNonNull(className, "className cannot be null");
		this.methodName = Objects.requireNonNull(methodName, "methodName cannot be null");
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
		String thisCanonicalName = className + "." + methodName;
		for (String key : result.getTrace().getMethodExecutionCount().keySet()) {
			String canonicalName = key.replace('$', '.'); // Goals contain canonical method names
			if (canonicalName.equals(thisCanonicalName)) {
				fitness = 0.0;
				break;
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

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return className + "." + methodName;
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
		MethodTraceCoverageTestFitness other = (MethodTraceCoverageTestFitness) obj;
		if (! className.equals(other.className)) {
			return false;
		} else return methodName.equals(other.methodName);
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.TestFitnessFunction#compareTo(org.evosuite.testcase.TestFitnessFunction)
	 */
	@Override
	public int compareTo(TestFitnessFunction other) {
		if (other instanceof MethodTraceCoverageTestFitness) {
			MethodTraceCoverageTestFitness otherMethodFitness = (MethodTraceCoverageTestFitness) other;
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
