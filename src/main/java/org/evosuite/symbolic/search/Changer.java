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
package org.evosuite.symbolic.search;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.HashMap;

import org.evosuite.symbolic.expr.Constraint;
import org.evosuite.symbolic.expr.IntegerVariable;
import org.evosuite.symbolic.expr.RealVariable;
import org.evosuite.symbolic.expr.StringVariable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * Changer class.
 * </p>
 * 
 * @author krusev
 */
public class Changer {

	static Logger log = LoggerFactory.getLogger(Changer.class);

	private long longBackUp;

	private double doubleBackUp;

	private double oldDist = Double.MAX_VALUE;

	/**
	 * <p>
	 * Constructor for Changer.
	 * </p>
	 */
	public Changer() {

	}

	private boolean distImpr(double newDistance) {
		return newDistance < oldDist;
	}

	private boolean distWrsn(double newDistance) {
		return newDistance > oldDist;
	}

	private void backup(RealVariable realVar, double newDist) {
		oldDist = newDist;
		doubleBackUp = realVar.execute();
	}

	private void backup(IntegerVariable intVar, double newDist) {
		oldDist = newDist;
		longBackUp = intVar.execute();
	}

	private void backup(StringVariable var, double newDist) {
		var.setMaxValue(var.getMinValue());
		oldDist = newDist;
	}

	private void restore(RealVariable realVar) {
		realVar.setConcreteValue(doubleBackUp);
	}

	private void restore(IntegerVariable intVar) {
		intVar.setConcreteValue(longBackUp);
		log.debug("resoring to: " + intVar + " with dist: " + oldDist);
	}

	private void restore(StringVariable var) {
		var.setMinValue(var.getMaxValue());
	}

	/**
	 * Increments the realVar with the specified value. If we are going out of
	 * the bounds of the variable the new value is set to the the appropriate
	 * bound.
	 * 
	 * @param realVar
	 * @param increment
	 */
	private void increment(RealVariable realVar, double i) {
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
	 * Increments the intVar with the specified value. If we are going out of
	 * the bounds of the variable the new value is set to the the appropriate
	 * bound.
	 * 
	 * @param intVar
	 * @param increment
	 */
	private void increment(IntegerVariable intVar, long i) {
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
	 * <p>
	 * strLocalSearch
	 * </p>
	 * 
	 * @param strVar
	 *            a {@link org.evosuite.symbolic.expr.StringVariable} object.
	 * @param cnstr
	 *            a {@link java.util.Collection} object.
	 * @param varsToChange
	 *            a {@link java.util.HashMap} object.
	 * @return a boolean.
	 */
	public boolean strLocalSearch(StringVariable strVar, Collection<Constraint<?>> cnstr,
	        HashMap<String, Object> varsToChange) {

		// try to remove each
		log.debug("Trying to remove characters");
		boolean improvement = false;

		backup(strVar, DistanceEstimator.getDistance(cnstr));

		String oldString = strVar.execute();
		for (int i = oldString.length() - 1; i >= 0; i--) {
			String newStr = oldString.substring(0, i) + oldString.substring(i + 1);
			strVar.setMinValue(newStr);

			double newDist = DistanceEstimator.getDistance(cnstr);

			if (distImpr(newDist)) {
				improvement = true;
				oldString = newStr;
				varsToChange.put(strVar.getName(), newStr);
				if (newDist == 0) {
					return true;
				}
				backup(strVar, newDist);
			} else {
				restore(strVar);
			}
		}

		// try to replace each 
		log.debug("Trying to replace characters");
		// Backup is done internally
		if (doStringAVM(strVar, cnstr, varsToChange, oldString)) {
			improvement = true;
			oldString = strVar.execute();
		}

		if (oldDist == 0.0)
			return true;

		// try to add everywhere
		log.debug("Trying to add characters");

		backup(strVar, DistanceEstimator.getDistance(cnstr));

		for (int i = 0; i < oldString.length() + 1; i++) {
			boolean add = true;
			while (add) {
				add = false;
				String newStr = oldString.substring(0, i) + '_' + oldString.substring(i);
				strVar.setMinValue(newStr);
				double newDist = DistanceEstimator.getDistance(cnstr);
				log.debug("Adding " + i + ": " + newStr + ": " + newDist);

				if (distImpr(newDist)) {
					improvement = true;
					backup(strVar, newDist);
					if (oldDist == 0.0)
						return true;

					doCharacterAVM(strVar, cnstr, varsToChange, i);
					oldString = strVar.execute();
				} else {
					restore(strVar);
				}
			}
		}
		return improvement;
	}

	/**
	 * Apply AVM to all characters in a string
	 * 
	 * @param strVar
	 * @param cnstr
	 * @param varsToChange
	 * @param oldString
	 * @return
	 */
	private boolean doStringAVM(StringVariable strVar, Collection<Constraint<?>> cnstr,
	        HashMap<String, Object> varsToChange, String oldString) {

		boolean improvement = false;

		for (int i = 0; i < oldString.length(); i++) {
			log.info("Current character: " + i);
			if (doCharacterAVM(strVar, cnstr, varsToChange, i))
				improvement = true;
		}
		return improvement;
	}

	/**
	 * Apply AVM to an individual character within a string
	 * 
	 * @param strVar
	 * @param cnstr
	 * @param varsToChange
	 * @param position
	 * @return
	 */
	private boolean doCharacterAVM(StringVariable strVar,
	        Collection<Constraint<?>> cnstr, HashMap<String, Object> varsToChange,
	        int position) {
		backup(strVar, DistanceEstimator.getDistance(cnstr));
		boolean done = false;
		boolean hasImproved = false;

		while (!done) {
			done = true;
			String origString = strVar.execute();
			char oldChar = origString.charAt(position);

			char[] characters = origString.toCharArray();

			char replacement = oldChar;
			replacement++;
			characters[position] = replacement;
			String newString = new String(characters);
			strVar.setMinValue(newString);
			double newDist = DistanceEstimator.getDistance(cnstr);
			log.debug("Probing increment " + position + ": " + newString + ": " + newDist
			        + " replacement = " + (int) replacement);
			if (distImpr(newDist)) {
				backup(strVar, newDist);
				varsToChange.put(strVar.getName(), newString);

				if (newDist == 0.0)
					return true;
				done = false;
				hasImproved = true;
				iterateCharacterAVM(strVar, cnstr, varsToChange, position, 2);
			} else {
				replacement -= 2;
				characters[position] = replacement;
				newString = new String(characters);
				strVar.setMinValue(newString);
				newDist = DistanceEstimator.getDistance(cnstr);
				log.debug("Probing decrement " + position + ": " + newString + ": "
				        + newDist + " replacement = " + (int) replacement);
				if (distImpr(newDist)) {
					backup(strVar, newDist);
					varsToChange.put(strVar.getName(), newString);

					if (newDist == 0.0)
						return true;

					done = false;
					hasImproved = true;
					iterateCharacterAVM(strVar, cnstr, varsToChange, position, -2);
				} else {
					restore(strVar);
					if (done)
						log.debug("Search finished " + position + ": " + newString + ": "
						        + newDist);
					else
						log.debug("Going for another iteration at position " + position);

				}
			}
		}
		return hasImproved;
	}

	private boolean iterateCharacterAVM(StringVariable strVar,
	        Collection<Constraint<?>> cnstr, HashMap<String, Object> varsToChange,
	        int position, int delta) {

		boolean improvement = false;
		String oldString = strVar.execute();

		log.debug("Trying increment " + delta + " of " + oldString);
		char oldChar = oldString.charAt(position);
		log.info(" -> Character " + position + ": " + oldChar);
		char[] characters = oldString.toCharArray();
		char replacement = oldChar;

		replacement += delta;
		characters[position] = replacement;
		String newString = new String(characters);
		strVar.setMinValue(newString);
		double newDist = DistanceEstimator.getDistance(cnstr);

		while (distImpr(newDist)) {

			backup(strVar, newDist);
			varsToChange.put(strVar.getName(), newString);

			if (newDist == 0.0)
				return true;

			oldString = newString;
			improvement = true;
			delta = 2 * delta;
			replacement += delta;
			log.info("Current delta: " + delta + " -> " + replacement);
			characters[position] = replacement;
			newString = new String(characters);
			log.info(" " + position + " " + oldString + "/" + oldString.length() + " -> "
			        + newString + "/" + newString.length());
			strVar.setMinValue(newString);
			newDist = DistanceEstimator.getDistance(cnstr);
		}
		log.debug("No improvement on " + oldString);
		restore(strVar);
		// strVar.setMinValue(oldString);
		log.debug("Final value of this iteration: " + oldString);

		return improvement;
	}

	/**
	 * <p>
	 * intLocalSearch
	 * </p>
	 * 
	 * @param intVar
	 *            a {@link org.evosuite.symbolic.expr.IntegerVariable} object.
	 * @param cnstr
	 *            a {@link java.util.Collection} object.
	 * @param varsToChange
	 *            a {@link java.util.HashMap} object.
	 * @return a boolean.
	 */
	public boolean intLocalSearch(IntegerVariable intVar,
	        Collection<Constraint<?>> cnstr, HashMap<String, Object> varsToChange) {
		double newDist;
		boolean improvement = false;
		boolean done = false;
		log.debug("Initial distance calculation " + intVar);

		backup(intVar, DistanceEstimator.getDistance(cnstr));
		log.debug("Initial distance calculation done");

		while (!done) {
			done = true;
			// Try increment
			log.debug("Trying to increment " + intVar);
			increment(intVar, 1);
			newDist = DistanceEstimator.getDistance(cnstr);
			log.debug("newDist: " + newDist + " oldDist: " + oldDist);
			if (distImpr(newDist)) {
				improvement = true;
				done = false;
				backup(intVar, newDist);
				iterate(intVar, cnstr, 2);
			} else {
				// restore
				restore(intVar);

				// Try decrement
				log.debug("Trying to decrement " + intVar);
				increment(intVar, -1);
				newDist = DistanceEstimator.getDistance(cnstr);
				log.debug("newDist: " + newDist + " oldDist: " + oldDist);
				if (distImpr(newDist)) {
					improvement = true;
					done = false;
					backup(intVar, newDist);
					iterate(intVar, cnstr, -2);
				} else {
					restore(intVar);
				}
			}
		}
		if (improvement) {
			varsToChange.put(intVar.getName(), intVar.getConcreteValue());
			log.debug("Finished long local search with new value " + intVar);
			//if (DistanceEstimator.getDistance(cnstr) == 0) {
			return true;
			//}
		}

		return false;
	}

	/**
	 * <p>
	 * realLocalSearch
	 * </p>
	 * 
	 * @param realVar
	 *            a {@link org.evosuite.symbolic.expr.RealVariable} object.
	 * @param cnstr
	 *            a {@link java.util.Collection} object.
	 * @param varsToChange
	 *            a {@link java.util.HashMap} object.
	 * @return a boolean.
	 */
	public boolean realLocalSearch(RealVariable realVar, Collection<Constraint<?>> cnstr,
	        HashMap<String, Object> varsToChange) {
		boolean improvement = false;

		improvement = doRealSearch(realVar, cnstr, 1.0, 2.0);

		if (oldDist > 0) {
			//improvement = doRealSearch(realVar, cnstr, Double.MIN_VALUE, 2.0);
			if (afterCommaSearch(realVar, cnstr))
				improvement = true;
		}

		if (improvement) {
			varsToChange.put(realVar.getName(), realVar.getConcreteValue());
			log.debug("Finished real local search with new value " + realVar);
			//if (oldDist <= 0) {
			//	return true;
			//}
			return true;
		}

		return false;
	}

	/**
	 * Regular AVM on real variables
	 * 
	 * @param realVar
	 * @param cnstr
	 * @param delta
	 * @param factor
	 * @return
	 */
	private boolean doRealSearch(RealVariable realVar, Collection<Constraint<?>> cnstr,
	        double delta, double factor) {

		double newDist;
		boolean improvement = false;
		boolean done = false;

		backup(realVar, DistanceEstimator.getDistance(cnstr));

		while (!done) {
			done = true;
			// Try increment
			log.debug("Trying to increment " + realVar + " with: " + delta);
			increment(realVar, delta);
			newDist = DistanceEstimator.getDistance(cnstr);
			log.debug("Old distance: " + oldDist + ", new distance: " + newDist);
			if (distImpr(newDist)) {
				improvement = true;
				done = false;
				backup(realVar, newDist);

				if (newDist == 0.0) {
					break;
				}

				iterate(realVar, cnstr, factor * delta, factor);
			} else {
				// restore
				restore(realVar);

				// Try decrement
				log.debug("Trying to decrement " + realVar + " with: " + delta);
				increment(realVar, -delta);
				newDist = DistanceEstimator.getDistance(cnstr);
				if (distImpr(newDist)) {
					improvement = true;
					done = false;
					backup(realVar, newDist);

					if (newDist == 0.0) {
						break;
					}

					iterate(realVar, cnstr, -factor * delta, factor);
				} else {
					restore(realVar);
				}
			}
		}

		return improvement;
	}

	/**
	 * Try to optimize the digits after the comma
	 * 
	 * @param realVar
	 * @param cnstr
	 * @return
	 */
	private boolean afterCommaSearch(RealVariable realVar, Collection<Constraint<?>> cnstr) {
		boolean improvement = false;

		// Assume that floats have 7 digits after comma and double 15. This is based on Flopsy
		int maxPrecision = realVar.getMaxValue() > Float.MAX_VALUE ? 15 : 7;

		for (int precision = 1; precision <= maxPrecision; precision++) {
			roundPrecision(realVar, cnstr, precision, maxPrecision == 7);
			log.debug("Current precision: " + precision);
			if (doRealSearch(realVar, cnstr, Math.pow(10.0, -precision), 2))
				improvement = true;
			if (oldDist <= 0) {
				break;
			}
		}

		return improvement;
	}

	/**
	 * Cut off digits after comma.
	 * 
	 * @param realVar
	 * @param cnstr
	 * @param precision
	 * @param isFloat
	 */
	private void roundPrecision(RealVariable realVar, Collection<Constraint<?>> cnstr,
	        int precision, boolean isFloat) {

		double value = realVar.getConcreteValue();
		BigDecimal bd = new BigDecimal(value).setScale(precision, RoundingMode.HALF_EVEN);
		if (bd.doubleValue() == value) {
			return;// false;
		}

		double newValue = bd.doubleValue();
		if (isFloat)
			realVar.setConcreteValue((new Float(newValue)));
		else
			realVar.setConcreteValue((new Double(newValue)));

		log.debug("Trying to chop precision " + precision + ": " + value + " -> "
		        + newValue);
		double dist = DistanceEstimator.getDistance(cnstr);
		if (!distWrsn(dist)) {
			backup(realVar, dist);
			return;// true;
		} else {
			restore(realVar);
			return;// false;
		}
	}

	/**
	 * Apply AVM on variable
	 * 
	 * @param realVar
	 * @param cnstr
	 * @param delta
	 * @param factor
	 */
	private void iterate(RealVariable realVar, Collection<Constraint<?>> cnstr,
	        double delta, double factor) {

		log.debug("[Loop] Trying increment " + delta + " of " + realVar.toString());

		increment(realVar, delta);
		double newDist = DistanceEstimator.getDistance(cnstr);
		log.debug("[Loop] Old distance: " + oldDist + ", new distance: " + newDist);
		while (distImpr(newDist)) {
			backup(realVar, newDist);

			delta = factor * delta;
			log.debug("[Loop] Trying increment " + delta + " of " + realVar);
			increment(realVar, delta);
			newDist = DistanceEstimator.getDistance(cnstr);
		}
		log.debug("No improvement on " + realVar);

		restore(realVar);

		log.debug("Final value of this iteration: " + realVar);
	}

	/**
	 * AVM inner loop
	 * 
	 * @param intVar
	 * @param cnstr
	 * @param delta
	 */
	private void iterate(IntegerVariable intVar, Collection<Constraint<?>> cnstr,
	        long delta) {

		log.debug("Trying increment " + delta + " of " + intVar.toString());

		increment(intVar, delta);
		double newDist = DistanceEstimator.getDistance(cnstr);
		log.debug("newDist: " + newDist + " oldDist: " + oldDist);
		while (distImpr(newDist)) {
			backup(intVar, newDist);

			delta = 2 * delta;
			log.debug("Trying increment " + delta + " of " + intVar);
			increment(intVar, delta);
			newDist = DistanceEstimator.getDistance(cnstr);
			log.debug("newDist: " + newDist + " oldDist: " + oldDist);
		}
		log.debug("No improvement on " + intVar);

		restore(intVar);

		log.debug("Final value of this iteration: " + intVar);
	}
}
