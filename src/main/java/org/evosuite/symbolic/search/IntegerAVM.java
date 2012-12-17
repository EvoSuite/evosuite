package org.evosuite.symbolic.search;

import java.util.Collection;

import org.evosuite.symbolic.expr.Constraint;
import org.evosuite.symbolic.expr.bv.IntegerVariable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class IntegerAVM {

	public IntegerAVM(IntegerVariable intVar, Collection<Constraint<?>> cnstr) {
		super();
		this.intVar = intVar;
		this.cnstr = cnstr;
	}

	static Logger log = LoggerFactory.getLogger(IntegerAVM.class);

	private long checkpointedConcreteValue;

	private double checkpointedDistance = Double.MAX_VALUE;

	private final IntegerVariable intVar;

	private final Collection<Constraint<?>> cnstr;

	/**
	 * Saves a new checkpoint for the current value and the current distance.
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
	public boolean applyAVM() {
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
			// Try increment
			log.debug("Trying to increment " + intVar);
			incrementVar(1);
			newDist = DistanceEstimator.getDistance(cnstr);
			log.debug("newDist: " + newDist + " oldDist: "
					+ checkpointedDistance);
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
				log.debug("newDist: " + newDist + " oldDist: "
						+ checkpointedDistance);
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
		log.debug("restoring to: " + intVar + " with dist: "
				+ checkpointedDistance);
		intVar.setConcreteValue(checkpointedConcreteValue);
	}

	/**
	 * AVM inner loop
	 * @param delta
	 */
	private void iterateVar(long delta) {

		log.debug("Trying increment " + delta + " of " + intVar.toString());

		incrementVar(delta);
		double newDist = DistanceEstimator.getDistance(cnstr);
		log.debug("newDist: " + newDist + " oldDist: " + checkpointedDistance);
		while (distImpr(newDist)) {
			checkpointVar(newDist);
			if (newDist == 0.0) {
				// solution found
				return;
			}

			delta = 2 * delta;
			log.debug("Trying increment " + delta + " of " + intVar);
			incrementVar(delta);
			newDist = DistanceEstimator.getDistance(cnstr);
			log.debug("newDist: " + newDist + " oldDist: "
					+ checkpointedDistance);
		}
		log.debug("No improvement on " + intVar);

		restoreVar();

		log.debug("Final value of this iteration: " + intVar);
	}

}
