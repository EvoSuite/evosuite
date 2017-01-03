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
package org.evosuite.symbolic.solver.avm;

import java.util.Collection;

import org.evosuite.symbolic.expr.Constraint;
import org.evosuite.symbolic.expr.bv.IntegerVariable;
import org.evosuite.symbolic.solver.DistanceEstimator;
import org.evosuite.symbolic.solver.SolverTimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class IntegerAVM extends VariableAVM {



	public IntegerAVM(IntegerVariable intVar, Collection<Constraint<?>> cnstr, long startTimeMillis, long timeout) {
		super(cnstr, startTimeMillis, timeout);
		this.intVar = intVar;
	}

	static Logger log = LoggerFactory.getLogger(IntegerAVM.class);

	private long checkpointedConcreteValue;

	private double checkpointedDistance = Double.MAX_VALUE;

	private final IntegerVariable intVar;

	/**
	 * Saves a new checkpoint for the current value and the current distance.
	 * 
	 * @param newDist
	 */
	private void checkpointVar(double newDist) {
		checkpointedDistance = newDist;
		checkpointedConcreteValue = intVar.getConcreteValue();
	}

	/**
	 * Increments the intVar with the specified value. If we are going out of
	 * the bounds of the variable the new value is set to the the appropriate
	 * bound.
	 * 
	 * @param intVar
	 * @param increment
	 */
	private void incrementVar(long i) {
		long oldVal = intVar.getConcreteValue();
		long newVal;
		if (i > 0) {
			if (oldVal <= intVar.getMaxValue() - i) {
				newVal = oldVal + i;
			} else {
				newVal = intVar.getMaxValue();
			}
		} else {
			if (oldVal >= intVar.getMinValue() - i) {
				newVal = oldVal + i;
			} else {
				newVal = intVar.getMinValue();
			}
		}
		intVar.setConcreteValue(newVal);
	}

	/**
	 * Returns if the new distance is smaller than the checkpointing old
	 * distance
	 * 
	 * @param newDistance
	 * @return
	 */
	private boolean distImpr(double newDistance) {
		return newDistance < checkpointedDistance;
	}

	/**
	 * Apply AVM to the integer variable. The search is guided using the
	 * constraint system.
	 * 
	 * @param intVar
	 *            an integer variable on which AVM will be applied
	 * @param cnstr
	 *            a constraint system to guide AVM
	 * @return true iff a new value was found such that distance is smaller than
	 *         before
	 */
	public boolean applyAVM() throws SolverTimeoutException {
		double newDist;
		boolean improvement = false;

		log.debug("Initial distance calculation " + intVar);
		final double initial_distance = DistanceEstimator.getDistance(cnstr);
		checkpointVar(initial_distance);
		log.debug("Initial distance calculation done");

		if (initial_distance == 0.0) {
			// no improvement (already solved)
			return false;
		}

		while (true) {
			if (isFinished()) {
				throw new SolverTimeoutException();
			}
			
			// Try increment
			log.debug("Trying to increment " + intVar);
			incrementVar(1);
			newDist = DistanceEstimator.getDistance(cnstr);
			log.debug("newDist: " + newDist + " oldDist: " + checkpointedDistance);
			if (distImpr(newDist)) {
				improvement = true;
				checkpointVar(newDist);
				if (newDist == 0.0) {
					// solution found
					break;
				}
				iterateVar(2);
			} else {
				// restore
				restoreVar();

				// Try decrement
				log.debug("Trying to decrement " + intVar);
				incrementVar(-1);
				newDist = DistanceEstimator.getDistance(cnstr);
				log.debug("newDist: " + newDist + " oldDist: " + checkpointedDistance);
				if (distImpr(newDist)) {
					improvement = true;
					checkpointVar(newDist);
					if (newDist == 0.0) {
						// solution found
						break;
					}
					iterateVar(-2);
				} else {
					restoreVar();
					break;
				}
			}
		}
		if (improvement) {
			log.debug("Finished long local search with new value " + intVar);
			return true;
		}

		return false;
	}

	/**
	 * Restores the intVar concrete value using the check-pointed value.
	 */
	private void restoreVar() {
		log.debug("restoring to: " + intVar + " with dist: " + checkpointedDistance);
		intVar.setConcreteValue(checkpointedConcreteValue);
	}

	/**
	 * AVM inner loop
	 * 
	 * @param delta
	 */
	private void iterateVar(long delta) throws SolverTimeoutException {

		log.debug("Trying increment " + delta + " of " + intVar.toString());

		incrementVar(delta);
		double newDist = DistanceEstimator.getDistance(cnstr);
		log.debug("newDist: " + newDist + " oldDist: " + checkpointedDistance);
		while (distImpr(newDist)) {
			if (isFinished()) {
				throw new SolverTimeoutException();
			}

			checkpointVar(newDist);
			if (newDist == 0.0) {
				// solution found
				return;
			}

			delta = 2 * delta;
			log.debug("Trying increment " + delta + " of " + intVar);
			incrementVar(delta);
			newDist = DistanceEstimator.getDistance(cnstr);
			log.debug("newDist: " + newDist + " oldDist: " + checkpointedDistance);
		}
		log.debug("No improvement on " + intVar);

		restoreVar();

		log.debug("Final value of this iteration: " + intVar);
	}

}
