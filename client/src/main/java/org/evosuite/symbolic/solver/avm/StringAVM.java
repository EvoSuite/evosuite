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
import org.evosuite.symbolic.expr.ExpressionEvaluator;
import org.evosuite.symbolic.expr.constraint.StringConstraint;
import org.evosuite.symbolic.expr.str.StringValue;
import org.evosuite.symbolic.expr.str.StringVariable;
import org.evosuite.symbolic.expr.token.HasMoreTokensExpr;
import org.evosuite.symbolic.solver.DistanceEstimator;
import org.evosuite.symbolic.solver.SolverTimeoutException;
import org.evosuite.utils.Randomness;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

final class StringAVM extends VariableAVM {

    public StringAVM(StringVariable strVar, Collection<Constraint<?>> cnstr, long start_time, long timeout) {
        super(cnstr, start_time, timeout);
        this.strVar = strVar;
    }

    static Logger log = LoggerFactory.getLogger(StringAVM.class);

    private double checkpointDistance = Double.MAX_VALUE;

    private String checkpointStringValue;

    private final StringVariable strVar;

    /**
     * <p>
     * strLocalSearch
     * </p>
     *
     * @return a boolean.
     */
    public boolean applyAVM() throws SolverTimeoutException {

        ExpressionEvaluator exprExecutor = new ExpressionEvaluator();
        // try to remove each
        log.debug("Trying to remove characters");
        boolean improvement = false;

        checkpointVar(DistanceEstimator.getDistance(cnstr));

        // First chop characters from the back until distance doesn't improve
        String oldString = strVar.getConcreteValue();
        boolean improved = true;
        while (improved && oldString.length() > 0) {

            if (isFinished()) {
                throw new SolverTimeoutException();
            }

            String newStr = oldString.substring(0, oldString.length() - 1);
            strVar.setConcreteValue(newStr);
            log.debug("Current attempt: " + newStr);
            improved = false;

            double newDist = DistanceEstimator.getDistance(cnstr);

            // if (distImpr(newDist)) {
            if (newDist <= checkpointDistance) {
                log.debug("Distance improved or did not increase, keeping change");
                checkpointVar(newDist);
                improvement = true;
                improved = true;
                oldString = newStr;
                if (newDist == 0) {
                    return true;
                }
            } else {
                log.debug("Distance did not improve, reverting change");
                restoreVar();
            }
        }

        // next try to replace each character using AVM
        log.debug("Trying to replace characters");
        // Backup is done internally
        if (doStringAVM(oldString)) {
            improvement = true;
            oldString = strVar.getConcreteValue();
        }

        if (checkpointDistance == 0.0) {
            return true;
        }

        // try to add at the end
        log.debug("Trying to add characters");

        checkpointVar(DistanceEstimator.getDistance(cnstr));

        // Finally add new characters at the end of the string
        improved = true;
        while (improved) {

            if (isFinished()) {
                throw new SolverTimeoutException();
            }

            improved = false;
            char charToInsert = Randomness.nextChar();
            String newStr = oldString + charToInsert;
            strVar.setConcreteValue(newStr);
            double newDist = DistanceEstimator.getDistance(cnstr);
            log.debug("Adding: " + newStr + ": " + newDist);
            if (distImpr(newDist)) {
                improvement = true;
                improved = true;
                checkpointVar(newDist);
                if (checkpointDistance == 0.0) {
                    log.debug("Search seems successful, stopping at " + checkpointDistance + "/" + newDist);
                    return true;
                }

                doCharacterAVM(newStr.length() - 1);
                oldString = strVar.getConcreteValue();
            } else {
                restoreVar();
            }
        }

        // try to insert delimiters (if any)
        Set<StringValue> delimiters = getTokenDelimiters(cnstr);
        for (StringValue delimiter : delimiters) {

            if (isFinished()) {
                throw new SolverTimeoutException();
            }

            improved = true;
            String delimiterStr = (String) delimiter.accept(exprExecutor, null);
            while (improved) {

                if (isFinished()) {
                    throw new SolverTimeoutException();
                }

                improved = false;
                char charToInsert = Randomness.nextChar();
                String newStr = oldString + delimiterStr + charToInsert;
                strVar.setConcreteValue(newStr);
                double newDist = DistanceEstimator.getDistance(cnstr);
                log.debug("Adding: " + newStr + ": " + newDist);
                if (distImpr(newDist)) {
                    improvement = true;
                    improved = true;
                    checkpointVar(newDist);
                    if (checkpointDistance == 0.0) {
                        log.debug("Search seems successful, stopping at " + checkpointDistance + "/" + newDist);
                        return true;
                    }

                    doCharacterAVM(newStr.length() - 1);
                    oldString = strVar.getConcreteValue();
                } else {
                    restoreVar();
                }
            }

        }

        return improvement;
    }

    private void checkpointVar(double newDist) {
        checkpointStringValue = strVar.getConcreteValue();
        checkpointDistance = newDist;
    }

    private boolean distImpr(double newDistance) {
        return newDistance < checkpointDistance;
    }

    private void restoreVar() {
        strVar.setConcreteValue(checkpointStringValue);
    }

    /**
     * Apply AVM to an individual character within a string
     *
     * @param position
     * @param varsToChange
     * @return
     */
    private boolean doCharacterAVM(int position) throws SolverTimeoutException {
        checkpointVar(DistanceEstimator.getDistance(cnstr));
        boolean done = false;
        boolean hasImproved = false;

        while (!done) {

            if (isFinished()) {
                throw new SolverTimeoutException();
            }

            done = true;
            String origString = strVar.getConcreteValue();
            char oldChar = origString.charAt(position);

            char[] characters = origString.toCharArray();

            // Try +1
            char replacement = oldChar;
            replacement = nextChar(replacement, 1);
            characters[position] = replacement;
            String newString = new String(characters);
            strVar.setConcreteValue(newString);
            double newDist = DistanceEstimator.getDistance(cnstr);
            log.debug("Probing increment " + position + ": " + newString + ": " + newDist + " replacement = "
                    + (int) replacement);
            if (distImpr(newDist)) {
                checkpointVar(newDist);

                if (newDist == 0.0)
                    return true;
                done = false;
                hasImproved = true;
                iterateCharacterAVM(position, 2);
            } else {
                replacement = nextChar(replacement, -2);
                characters[position] = replacement;
                newString = new String(characters);
                strVar.setConcreteValue(newString);
                newDist = DistanceEstimator.getDistance(cnstr);
                log.debug("Probing decrement " + position + ": " + newString + ": " + newDist + " replacement = "
                        + (int) replacement);
                if (distImpr(newDist)) {
                    checkpointVar(newDist);

                    if (newDist == 0.0)
                        return true;

                    done = false;
                    hasImproved = true;
                    iterateCharacterAVM(position, -2);
                } else {

                    // Try +32
                    replacement = (char) (oldChar + 32);
                    characters[position] = replacement;
                    newString = new String(characters);
                    strVar.setConcreteValue(newString);
                    newDist = DistanceEstimator.getDistance(cnstr);
                    log.debug("Probing increment [32] " + position + ": " + newString + ": " + newDist
                            + " replacement = " + (int) replacement);
                    if (distImpr(newDist)) {
                        checkpointVar(newDist);

                        done = false;
                        hasImproved = true;
                        // Now try +/-1 as usual
                        break;
                    } else {
                        // Try -32
                        replacement = (char) (oldChar + 32);
                        characters[position] = replacement;
                        newString = new String(characters);
                        strVar.setConcreteValue(newString);
                        newDist = DistanceEstimator.getDistance(cnstr);
                        log.debug("Probing increment [32] " + position + ": " + newString + ": " + newDist
                                + " replacement = " + (int) replacement);
                        if (distImpr(newDist)) {
                            checkpointVar(newDist);

                            done = false;
                            hasImproved = true;
                            // Now try +/-1 as usual
                        } else {
                            restoreVar();
                        }
                    }

                    if (done)
                        log.debug("Search finished " + position + ": " + newString + ": " + newDist);
                    else
                        log.debug("Going for another iteration at position " + position);

                }
            }
        }
        return hasImproved;
    }

    /**
     * Apply AVM to all characters in a string
     *
     * @param oldString
     * @param varsToChange
     * @return
     */
    private boolean doStringAVM(String oldString) throws SolverTimeoutException {

        boolean improvement = false;

        for (int i = 0; i < oldString.length(); i++) {
            if (isFinished()) {
                throw new SolverTimeoutException();
            }

            log.info("Current character: " + i);
            if (doCharacterAVM(i))
                improvement = true;
        }
        return improvement;
    }

    private boolean iterateCharacterAVM(int position, int delta) throws SolverTimeoutException {

        boolean improvement = false;
        String oldString = strVar.getConcreteValue();

        log.debug("Trying increment " + delta + " of " + oldString);
        char oldChar = oldString.charAt(position);
        log.info(" -> Character " + position + ": " + oldChar);
        char[] characters = oldString.toCharArray();
        char replacement = oldChar;

        replacement = nextChar(replacement, delta);
        characters[position] = replacement;
        String newString = new String(characters);
        strVar.setConcreteValue(newString);
        double newDist = DistanceEstimator.getDistance(cnstr);

        while (distImpr(newDist)) {
            if (isFinished()) {
                throw new SolverTimeoutException();
            }

            checkpointVar(newDist);
            if (newDist == 0.0)
                return true;

            oldString = newString;
            improvement = true;
            delta = 2 * delta;
            replacement = nextChar(replacement, delta);

            log.info("Current delta: " + delta + " -> " + replacement);
            characters[position] = replacement;
            newString = new String(characters);
            log.info(" " + position + " " + oldString + "/" + oldString.length() + " -> " + newString + "/"
                    + newString.length());
            strVar.setConcreteValue(newString);
            newDist = DistanceEstimator.getDistance(cnstr);
        }
        log.debug("No improvement on " + oldString);
        restoreVar();
        // strVar.setMinValue(oldString);
        log.debug("Final value of this iteration: " + oldString);

        return improvement;
    }

    /**
     * This method avoids overflow when computing the next char.
     *
     * @param oldChar
     * @param delta
     * @return
     */
    private char nextChar(char oldChar, int delta) {
        char nextChar = (char) (oldChar + delta);
        if (delta >= 0) {
            if (nextChar < oldChar) {
                nextChar = Character.MAX_VALUE;
            }
        } else {
            if (nextChar > oldChar) {
                nextChar = Character.MIN_VALUE;
            }
        }
        return nextChar;
    }

    private static Set<StringValue> getTokenDelimiters(Collection<Constraint<?>> constraints) {

        Set<StringValue> delimiters = new HashSet<>();
        for (Constraint<?> constraint : constraints) {

            if (constraint instanceof StringConstraint) {
                StringConstraint stringConstraint = (StringConstraint) constraint;

                if (stringConstraint.getLeftOperand() instanceof HasMoreTokensExpr) {
                    HasMoreTokensExpr hasMoreTokensExpr = (HasMoreTokensExpr) stringConstraint.getLeftOperand();
                    StringValue delimiter = hasMoreTokensExpr.getTokenizerExpr().getDelimiter();
                    delimiters.add(delimiter);
                }

            }
        }
        return delimiters;
    }

}
