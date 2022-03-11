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
package org.evosuite.symbolic.solver.avm;

import org.evosuite.symbolic.expr.Constraint;
import org.evosuite.symbolic.expr.fp.RealVariable;
import org.evosuite.symbolic.solver.DistanceEstimator;
import org.evosuite.symbolic.solver.SolverTimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;

final class RealAVM extends VariableAVM {

    public RealAVM(RealVariable realVar, Collection<Constraint<?>> cnstr, long start_time, long timeout) {
        super(cnstr, start_time, timeout);
        this.realVar = realVar;
    }

    static Logger log = LoggerFactory.getLogger(RealAVM.class);

    private double checkpointedConcreteValue;

    private double checkpointedDistance = Double.MAX_VALUE;

    private final RealVariable realVar;

    public boolean applyAVM() throws SolverTimeoutException {
        boolean improvement = false;

        improvement = doRealSearch(1.0, 2.0);

        if (checkpointedDistance > 0) {
            if (afterCommaSearch(realVar, cnstr))
                improvement = true;
        }

        if (improvement) {
            log.debug("Finished real local search with new value " + realVar);
            return true;
        }

        return false;
    }

    private boolean doRealSearch(double delta, double factor) throws SolverTimeoutException {

        boolean improvement = false;

        final double initial_distance = DistanceEstimator.getDistance(cnstr);
        checkpointVar(initial_distance);
        if (initial_distance == 0.0) {
            // already solved, no improvement found
            return false;
        }

        while (true) {
            if (isFinished()) {
                throw new SolverTimeoutException();
            }

            // Try increment
            log.debug("Trying to increment " + realVar + " with: " + delta);
            incrementVar(delta);
            double newDist = DistanceEstimator.getDistance(cnstr);
            log.debug("Old distance: " + this.checkpointedDistance + ", new distance: " + newDist);
            if (distImpr(newDist)) {
                improvement = true;
                checkpointVar(newDist);
                if (newDist == 0.0) {
                    // solution found
                    break;
                }
                iterateVar(factor * delta, factor);
            } else {
                // restore
                restoreVar();
                // Try decrement
                log.debug("Trying to decrement " + realVar + " with: " + delta);
                incrementVar(-delta);
                newDist = DistanceEstimator.getDistance(cnstr);
                if (distImpr(newDist)) {
                    improvement = true;
                    checkpointVar(newDist);
                    if (newDist == 0.0) {
                        // solution found
                        break;
                    }
                    iterateVar(-factor * delta, factor);
                } else {
                    restoreVar();
                    break;
                }
            }
        }

        return improvement;
    }

    private void checkpointVar(double newDist) {
        this.checkpointedDistance = newDist;
        this.checkpointedConcreteValue = realVar.getConcreteValue();
    }

    /**
     * Increments the realVar with the specified value. If we are going out of
     * the bounds of the variable the new value is set to the the appropriate
     * bound.
     *
     * @param increment
     */
    private void incrementVar(double i) {
        double oldVal = realVar.getConcreteValue();
        double newVal;
        if (i > 0) {
            if (oldVal <= realVar.getMaxValue() - i) {
                newVal = oldVal + i;
            } else {
                newVal = realVar.getMaxValue();
            }
        } else {
            if (oldVal >= realVar.getMinValue() - i) {
                newVal = oldVal + i;
            } else {
                newVal = realVar.getMinValue();
            }
        }
        realVar.setConcreteValue(newVal);
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
     * Restores the intVar concrete value using the check-pointed value.
     */
    private void restoreVar() {
        log.debug("restoring to: " + realVar + " with dist: " + checkpointedDistance);
        realVar.setConcreteValue(checkpointedConcreteValue);
    }

    /**
     * Try to optimize the digits after the comma
     *
     * @param realVar
     * @param cnstr
     * @return
     */
    private boolean afterCommaSearch(RealVariable realVar, Collection<Constraint<?>> cnstr) throws SolverTimeoutException {
        boolean improvement = false;

        // Assume that floats have 7 digits after comma and double 15. This is
        // based on Flopsy
        int maxPrecision = realVar.getMaxValue() > Float.MAX_VALUE ? 15 : 7;

        for (int precision = 1; precision <= maxPrecision; precision++) {
            chopOffPrecision(precision, maxPrecision == 7);
            log.debug("Current precision: " + precision);
            final double delta = Math.pow(10.0, -precision);
            final double factor = 2;
            if (doRealSearch(delta, factor))
                improvement = true;
            if (this.checkpointedDistance <= 0) {
                break;
            }
        }

        return improvement;
    }

    /**
     * Cut off digits after comma.
     *
     * @param precision
     * @param isFloat
     */
    private void chopOffPrecision(int precision, boolean isFloat) {

        double value = realVar.getConcreteValue();
        BigDecimal bd = new BigDecimal(value).setScale(precision, RoundingMode.HALF_EVEN);
        double newValue = bd.doubleValue();
        if (newValue == value) {
            return;// false;
        }
        realVar.setConcreteValue(newValue);

        log.debug("Trying to chop precision " + precision + ": " + value + " -> " + newValue);
        double dist = DistanceEstimator.getDistance(cnstr);
        if (!distWrsn(dist)) {
            checkpointVar(dist);
            return;// true;
        } else {
            restoreVar();
            return;// false;
        }
    }

    private boolean distWrsn(double newDistance) {
        return newDistance > this.checkpointedDistance;
    }

    /**
     * Apply AVM on variable
     *
     * @param delta
     * @param factor
     */
    private void iterateVar(double delta, double factor) throws SolverTimeoutException {

        log.debug("[Loop] Trying increment " + delta + " of " + realVar.toString());

        incrementVar(delta);
        double newDist = DistanceEstimator.getDistance(cnstr);
        log.debug("[Loop] Old distance: " + this.checkpointedDistance + ", new distance: " + newDist);
        while (distImpr(newDist)) {
            if (isFinished()) {
                throw new SolverTimeoutException();
            }

            checkpointVar(newDist);
            if (newDist == 0.0) {
                // solution found
                return;
            }
            delta = factor * delta;
            log.debug("[Loop] Trying increment " + delta + " of " + realVar);
            incrementVar(delta);
            newDist = DistanceEstimator.getDistance(cnstr);
        }
        log.debug("No improvement on " + realVar);
        restoreVar();

        log.debug("Final value of this iteration: " + realVar);
    }

}
