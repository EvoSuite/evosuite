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
package org.evosuite.coverage.epa;

import org.evosuite.coverage.exception.ExceptionCoverageTestFitness;
import org.evosuite.testcase.*;
import org.evosuite.testcase.execution.ExecutionResult;

/**
 * Fitness function for a single test on a single exception
 *
 * @author Gordon Fraser, Jose Miguel Rojas
 */
public class EPAMiningCoverageTestFitness extends TestFitnessFunction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7659713027558487726L;

	/**
	 * The origin state of the transition
	 */
	private final EPAState fromState;

	private final String targetClass;
	/**
	 * actionId
	 */
	private final String actionId;

	/**
	 * The destination state of the transition
	 */
	private final EPAState toState;

	/**
	 * Constructor - fitness is specific to a method
	 * 
	 * @param methodIdentifier
	 *            the method name
	 * @param exceptionClass
	 *            the exception class
	 * @throws IllegalArgumentException
	 */
	public EPAMiningCoverageTestFitness(String targetClass, EPAState fromState, String actionId, EPAState toState)
			throws IllegalArgumentException {
		if ((fromState == null) || (actionId == null) || toState == null) {
			throw new IllegalArgumentException("EPA states and actionId cannot be null");
		}
		this.targetClass = targetClass;
		this.fromState = fromState;
		this.actionId = actionId;
		this.toState = toState;
	}

	public String getKey() {
		return "EPAMiningTransition[" + fromState.getName() + "_" + actionId + "_" + toState.getName() + "]";
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

		for (EPATrace epa_trace : result.getTrace().getEPATraces()) {
			for (EPATransition epa_transition : epa_trace.getEpaTransitions()) {
				EPAState epa_transition_destination = epa_transition.getDestinationState();
				if (epa_transition_destination.equals(EPAState.INVALID_OBJECT_STATE)) {
					break;
				}
				EPAState epa_transition_origin = epa_transition.getOriginState();
				String epa_transition_actionId = epa_transition.getActionName();
				if (epa_transition_origin.equals(this.fromState) && epa_transition_actionId.equals(this.actionId)
						&& epa_transition_destination.equals(this.toState)) {
					return 0.0;
				}
			}
		}

		updateIndividual(this, individual, fitness);

		return fitness;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return getKey();
	}

	@Override
	public int compareTo(TestFitnessFunction other) {
		if (other instanceof EPAMiningCoverageTestFitness) {
			EPAMiningCoverageTestFitness otherMethodFitness = (EPAMiningCoverageTestFitness) other;
			if (actionId.equals(otherMethodFitness.actionId)) {
				if (fromState.equals(((EPAMiningCoverageTestFitness) other).fromState)) {
					return this.toState.getName().compareTo(((EPAMiningCoverageTestFitness) other).toState.getName());
				} else
					return fromState.getName().compareTo(otherMethodFitness.fromState.getName());
			} else
				return actionId.compareTo(otherMethodFitness.actionId);
		}
		return compareClassName(other);
	}

	@Override
	public String getTargetClass() {
		return targetClass;
	}

	@Override
	public String getTargetMethod() {
		return this.actionId;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((actionId == null) ? 0 : actionId.hashCode());
		result = prime * result + ((fromState == null) ? 0 : fromState.hashCode());
		result = prime * result + ((targetClass == null) ? 0 : targetClass.hashCode());
		result = prime * result + ((toState == null) ? 0 : toState.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		EPAMiningCoverageTestFitness other = (EPAMiningCoverageTestFitness) obj;
		if (actionId == null) {
			if (other.actionId != null)
				return false;
		} else if (!actionId.equals(other.actionId))
			return false;
		if (fromState == null) {
			if (other.fromState != null)
				return false;
		} else if (!fromState.equals(other.fromState))
			return false;
		if (targetClass == null) {
			if (other.targetClass != null)
				return false;
		} else if (!targetClass.equals(other.targetClass))
			return false;
		if (toState == null) {
			if (other.toState != null)
				return false;
		} else if (!toState.equals(other.toState))
			return false;
		return true;
	}

}