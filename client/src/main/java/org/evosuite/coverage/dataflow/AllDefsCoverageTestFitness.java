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
package org.evosuite.coverage.dataflow;

import java.util.Map;

import org.evosuite.coverage.statement.StatementCoverageTestFitness;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.TestFitnessFunction;
import org.evosuite.testcase.execution.ExecutionResult;

/**
 * Evaluate fitness of a single test case with respect to one Definition-Use
 * pair
 * 
 * For more information look at the comment from method getDistance()
 * 
 * @author Andre Mis
 */
public class AllDefsCoverageTestFitness extends TestFitnessFunction {

	private static final long serialVersionUID = 1L;
	/** Constant <code>singleFitnessTime=0l</code> */
	public static long singleFitnessTime = 0l;

	private final Definition targetDef;
	private final TestFitnessFunction goalDefinitionFitness;
	private final Map<Use, DefUseCoverageTestFitness> uses;

	/**
	 * <p>
	 * Constructor for AllDefsCoverageTestFitness.
	 * </p>
	 * 
	 * @param def
	 *            a {@link org.evosuite.coverage.dataflow.Definition} object.
	 * @param uses
	 *            a {@link java.util.Map} object.
	 */
	public AllDefsCoverageTestFitness(Definition def,
	        Map<Use, DefUseCoverageTestFitness> uses) {
		this.targetDef = def;
		this.goalDefinitionFitness = new StatementCoverageTestFitness(def);
		this.uses = uses;
	}

	/** {@inheritDoc} */
	@Override
	public double getFitness(TestChromosome individual, ExecutionResult result) {

		double defFitness = goalDefinitionFitness.getFitness(individual, result);

		if (defFitness > 0)
			return 1 + normalize(defFitness);

		// TODO: filter all objects
		// TODO: compute minimum over all use-fitnesses
		// TODO: return that minimum after normalization, stop once a use-fitness is 0

		// first inefficient version:
		double min = Double.MAX_VALUE;
		for (Use use : uses.keySet()) {
			double useFitness = uses.get(use).getFitness(individual, result);
			if (useFitness == 0)
				return 0;
			if (useFitness < min)
				min = useFitness;
		}

		return min;
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((targetDef == null) ? 0 : targetDef.hashCode());
		return result;
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
		AllDefsCoverageTestFitness other = (AllDefsCoverageTestFitness) obj;
		if (targetDef == null) {
			if (other.targetDef != null)
				return false;
		} else if (!targetDef.equals(other.targetDef))
			return false;
		return true;
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.TestFitnessFunction#compareTo(org.evosuite.testcase.TestFitnessFunction)
	 */
	@Override
	public int compareTo(TestFitnessFunction other) {
		if (other instanceof AllDefsCoverageTestFitness) {
			AllDefsCoverageTestFitness otherFitness = (AllDefsCoverageTestFitness) other;
			return targetDef.compareTo(otherFitness.targetDef);
		}
		return compareClassName(other);
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "AllDef-Goal " + targetDef.toString();
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.TestFitnessFunction#getTargetClass()
	 */
	@Override
	public String getTargetClass() {
		return targetDef.getClassName();
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.TestFitnessFunction#getTargetMethod()
	 */
	@Override
	public String getTargetMethod() {
		return targetDef.getMethodName();
	}

}
