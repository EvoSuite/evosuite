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
import java.util.List;

import org.evosuite.symbolic.expr.Constraint;
import org.evosuite.symbolic.expr.IntegerVariable;
import org.evosuite.symbolic.expr.RealVariable;
import org.evosuite.symbolic.expr.StringVariable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * <p>Changer class.</p>
 *
 * @author krusev
 */
public class Changer {

	static Logger log = LoggerFactory.getLogger(Changer.class);

	private long longBackUp;

	private double doubleBackUp;

	private double oldDist = Double.MAX_VALUE;

	/**
	 * <p>Constructor for Changer.</p>
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
	 * <p>strLocalSearch</p>
	 *
	 * @param strVar a {@link org.evosuite.symbolic.expr.StringVariable} object.
	 * @param cnstr a {@link java.util.Collection} object.
	 * @param varsToChange a {@link java.util.HashMap} object.
	 * @return a boolean.
	 */
	public boolean strLocalSearch(StringVariable strVar, Collection<Constraint<?>> cnstr,
	        HashMap<String, Object> varsToChange) {

		// try to remove each
		boolean improvement = false;

		backup(strVar, DistanceEstimator.getDistance(cnstr));

		for (int i = strVar.execute().length() - 1; i >= 0; i--) {
			String newStr = strVar.execute().substring(0, i)
			        + strVar.execute().substring(i + 1);
			strVar.setMinValue(newStr);

			double newDist = DistanceEstimator.getDistance(cnstr);

			if (distImpr(newDist)) {
				improvement = true;
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

		backup(strVar, DistanceEstimator.getDistance(cnstr));

		spatialLoop: for (int i = 0; i < strVar.execute().length(); i++) {
			char oldChar = strVar.execute().charAt(i);
			char[] characters = strVar.execute().toCharArray();
			for (char replacement = 0; replacement < 128; replacement++) {
				if (replacement != oldChar) {
					characters[i] = replacement;
					String newStr = new String(characters);
					strVar.setMinValue(newStr);

					double newDist = DistanceEstimator.getDistance(cnstr);

					if (distImpr(newDist)) {
						improvement = true;

						varsToChange.put(strVar.getName(), newStr);
						if (newDist == 0) {
							return true;
						}
						backup(strVar, newDist);
						break;
					} else {
						restore(strVar);
					}
					if (distWrsn(newDist)) {
						//skip this place
						continue spatialLoop;
					}
				}
			}
		}

		// try to add everywhere

		backup(strVar, DistanceEstimator.getDistance(cnstr));

		for (int i = 0; i < strVar.execute().length() + 1; i++) {
			boolean add = true;
			while (add) {
				add = false;
				for (char replacement = 0; replacement < 128; replacement++) {
					String newStr = strVar.execute().substring(0, i) + replacement
					        + strVar.execute().substring(i);
					strVar.setMinValue(newStr);

					double newDist = DistanceEstimator.getDistance(cnstr);

					if (distImpr(newDist)) {
						improvement = true;
						varsToChange.put(strVar.getName(), newStr);
						if (newDist <= 0) {
							return true;
						}
						backup(strVar, newDist);
						add = true;
						break;
					} else {
						restore(strVar);
					}
				}
			}
		}
		return improvement;
	}

	/**
	 * <p>intLocalSearch</p>
	 *
	 * @param intVar a {@link org.evosuite.symbolic.expr.IntegerVariable} object.
	 * @param cnstr a {@link java.util.Collection} object.
	 * @param varsToChange a {@link java.util.HashMap} object.
	 * @return a boolean.
	 */
	public boolean intLocalSearch(IntegerVariable intVar,
	        Collection<Constraint<?>> cnstr, HashMap<String, Object> varsToChange) {
		double newDist;
		boolean improvement = false;
		boolean done = false;

		backup(intVar, DistanceEstimator.getDistance(cnstr));

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
	 * <p>realLocalSearch</p>
	 *
	 * @param realVar a {@link org.evosuite.symbolic.expr.RealVariable} object.
	 * @param cnstr a {@link java.util.Collection} object.
	 * @param varsToChange a {@link java.util.HashMap} object.
	 * @return a boolean.
	 */
	public boolean realLocalSearch(RealVariable realVar, Collection<Constraint<?>> cnstr,
	        HashMap<String, Object> varsToChange) {
		boolean improvement = false;

		improvement = doRealSearch(realVar, cnstr, 1.0, 2.0);

		if (oldDist > 0) {
			//improvement = doRealSearch(realVar, cnstr, Double.MIN_VALUE, 2.0);
			if (afterCommaSearchV2(realVar, cnstr))
				improvement = true;
		}

		if (improvement) {
			varsToChange.put(realVar.getName(), realVar.getConcreteValue());
			log.debug("Finished long local search with new value " + realVar);
			//if (oldDist <= 0) {
			//	return true;
			//}
			return true;
		}

		return false;
	}

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

	private boolean afterCommaSearchV2(RealVariable realVar,
	        Collection<Constraint<?>> cnstr) {
		boolean improvement = false;
		//		int maxPrecision = realVar.getMaxValue() > Float.MAX_VALUE ? 15 : 7;
		int maxPrecision = 15;
		for (int precision = 1; precision <= maxPrecision; precision++) {
			//roundPrecision(realVar, cnstr, precision, maxPrecision == 7);
			log.debug("Current precision: " + precision);
			if (doRealSearch(realVar, cnstr, Math.pow(10.0, -precision), 2))
				improvement = true;
			if (oldDist <= 0) {
				break;
			}
		}

		return improvement;
	}

	@SuppressWarnings("unused")
	private boolean afterCommaSearchV1(RealVariable realVar, List<Constraint<?>> cnstr) {
		boolean improvement = false;

		//compute interval
		log.debug("Searching after comma");

		//		double left = Math.floor(realVar.getConcreteValue());
		//		double work = Double.MAX_VALUE;//realVar.getConcreteValue();
		//		double right = Math.ceil(realVar.getConcreteValue());
		////		log.debuging("left: " + left +" conc " + realVar.getConcreteValue() + " right: "+ right);
		//		
		//
		//		realVar.setConcreteValue(left);
		//		double distL = DistanceEstimator.getDistance(cnstr);
		//		realVar.setConcreteValue(right);
		//		double distR = DistanceEstimator.getDistance(cnstr);
		//
		//		realVar.setConcreteValue((left+right)/2.0);
		//		double distW = DistanceEstimator.getDistance(cnstr);

		double left = realVar.getConcreteValue() - 1.0;
		double work = Double.MAX_VALUE;//realVar.getConcreteValue();
		double right = realVar.getConcreteValue() + 1.0;

		double distW = DistanceEstimator.getDistance(cnstr);

		increment(realVar, -1.0);
		double distL = DistanceEstimator.getDistance(cnstr);
		increment(realVar, 2.0);
		double distR = DistanceEstimator.getDistance(cnstr);
		increment(realVar, -1.0);

		//TODO this whole story with oldWork != work works but should 
		// be done a "little" bit better ...
		double oldWork = -Double.MAX_VALUE;

		//since we are going in the same direction with left and right
		// we will eventually produce the right result
		// if there is no right result we will arrive at some local min and 
		// work will stay the same
		while (distW > 0.0) {
			if (oldWork == work) {
				//unreachable
				log.debug("Stopping search as old value is new value: " + work + ", "
				        + left + " - " + right + ", but distance is " + distW);

				return false;
			}
			//log.debuging("oldWork: " + oldWork + " work: " + work);
			oldWork = work;

			//			log.debuging("left: " + left +" conc " + (left + right) + " right: "+ right);

			work = (left + right) / 2.0;
			realVar.setConcreteValue(work);
			distW = DistanceEstimator.getDistance(cnstr);

			if (distW < distL || distW < distR) {
				log.debug("improoved");
				improvement = true;
				backup(realVar, distW);
			} else {
				log.debug("restore");
				restore(realVar);
				break;
			}

			log.debug("left: " + left + " " + distL + " work: " + work + " " + distW
			        + " right: " + right + " " + distR);
			if (distL > distR) {
				left = work;
				distL = distW;
			} else {
				right = work;
				distR = distW;
			}
		}

		//log.debuging("newRealVar: " + realVar);

		// TODO Auto-generated method stub
		return improvement;
	}

	@SuppressWarnings("unused")
	private void roundPrecision(RealVariable realVar, List<Constraint<?>> cnstr,
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
