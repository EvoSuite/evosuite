package de.unisb.cs.st.evosuite.symbolic.search;

import gov.nasa.jpf.JPF;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import de.unisb.cs.st.evosuite.symbolic.expr.Constraint;
import de.unisb.cs.st.evosuite.symbolic.expr.IntegerVariable;
import de.unisb.cs.st.evosuite.symbolic.expr.RealVariable;
import de.unisb.cs.st.evosuite.symbolic.expr.StringVariable;

/**
 * @author krusev
 * 
 */
public class Changer {

	static Logger log = JPF.getLogger("de.unisb.cs.st.evosuite.symbolic.search.Changer");

	private long longBackUp;

	private double doubleBackUp;

	private double oldDist = Double.MAX_VALUE;

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

	public boolean strLocalSearch(StringVariable strVar, List<Constraint<?>> cnstr,
	        HashMap<String, Object> varsToChange) {

		// try to remove each

		backup(strVar, DistanceEstimator.getDistance(cnstr));

		for (int i = strVar.execute().length() - 1; i >= 0; i--) {
			String newStr = strVar.execute().substring(0, i)
			        + strVar.execute().substring(i + 1);
			strVar.setMinValue(newStr);

			double newDist = DistanceEstimator.getDistance(cnstr);

			if (distImpr(newDist)) {
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
		return false;
	}

	public boolean intLocalSearch(IntegerVariable intVar, List<Constraint<?>> cnstr,
	        HashMap<String, Object> varsToChange) {
		double newDist;
		boolean improvement = false;
		boolean done = false;

		backup(intVar, DistanceEstimator.getDistance(cnstr));

		while (!done) {
			done = true;
			// Try increment
			log.info("Trying to increment " + intVar);
			increment(intVar, 1);
			newDist = DistanceEstimator.getDistance(cnstr);
			if (distImpr(newDist)) {
				improvement = true;
				done = false;
				backup(intVar, newDist);
				iterate(intVar, cnstr, 2);
			} else {
				// restore
				restore(intVar);

				// Try decrement
				log.info("Trying to decrement " + intVar);
				increment(intVar, -1);
				newDist = DistanceEstimator.getDistance(cnstr);
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
			log.info("Finished long local search with new value " + intVar);
			if (DistanceEstimator.getDistance(cnstr) == 0) {
				return true;
			}
		}

		return false;
	}
	
	public boolean realLocalSearch(RealVariable realVar, List<Constraint<?>> cnstr,
	        HashMap<String, Object> varsToChange) {
		boolean improvement = false;
		
		improvement = doRealSearch(realVar, cnstr, 1.0, 2.0);
		
		if (oldDist > 0) {
			improvement = afterCommaSearchV2(realVar, cnstr);
		}
		
		if (improvement) {
			varsToChange.put(realVar.getName(), realVar.getConcreteValue());
			log.info("Finished long local search with new value " + realVar);
			if (oldDist <= 0) {
				return true;
			}
		}
		
		return false;
	}

	private boolean doRealSearch(RealVariable realVar, List<Constraint<?>> cnstr,
			double delta, double factor) {
		
		double newDist;
		boolean improvement = false;
		boolean done = false;

		backup(realVar, DistanceEstimator.getDistance(cnstr));

		while (!done) {
			done = true;
			// Try increment
			log.info("Trying to increment " + realVar + " with: " + delta);
			increment(realVar, delta);
			newDist = DistanceEstimator.getDistance(cnstr);
			log.info("Old distance: " + oldDist + ", new distance: " + newDist);
			if (distImpr(newDist)) {
				improvement = true;
				done = false;
				backup(realVar, newDist);
				
				if (newDist == 0.0) {
					break;
				}

				iterate(realVar, cnstr, factor*delta, factor);
			} else {
				// restore
				restore(realVar);

				// Try decrement
				log.info("Trying to decrement " + realVar + " with: " + delta);
				increment(realVar, -delta);
				newDist = DistanceEstimator.getDistance(cnstr);
				if (distImpr(newDist)) {
					improvement = true;
					done = false;
					backup(realVar, newDist);
					
					if (newDist == 0.0) {
						break;
					}
					
					iterate(realVar, cnstr, -factor*delta, factor);
				} else {
					restore(realVar);
				}
			}
		}

		return improvement;
	}

	private boolean afterCommaSearchV2(RealVariable realVar,
			List<Constraint<?>> cnstr) {
		boolean improvement = false;
//		int maxPrecision = realVar.getMaxValue() > Float.MAX_VALUE ? 15 : 7;
		int maxPrecision = 15;
		for (int precision = 1; precision <= maxPrecision; precision++) {
			roundPrecision(realVar, cnstr, precision, maxPrecision == 7);
			log.info("Current precision: " + precision);
			improvement = doRealSearch(realVar, cnstr, Math.pow(10.0, -precision), 2);
			if (oldDist <=0){
				break;
			}
		}
		
		return improvement;
	}
	
	@SuppressWarnings("unused")
	private boolean afterCommaSearchV1(RealVariable realVar,
			List<Constraint<?>> cnstr) {
		boolean improvement = false;
		
		
		//compute interval
		log.info("Searching after comma");
		
//		double left = Math.floor(realVar.getConcreteValue());
//		double work = Double.MAX_VALUE;//realVar.getConcreteValue();
//		double right = Math.ceil(realVar.getConcreteValue());
////		log.warning("left: " + left +" conc " + realVar.getConcreteValue() + " right: "+ right);
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
				log.warning("Stopping search as old value is new value: " + work + ", "
				        + left + " - " + right + ", but distance is " + distW);

				return false;
			}
			//log.warning("oldWork: " + oldWork + " work: " + work);
			oldWork = work;
			
//			log.warning("left: " + left +" conc " + (left + right) + " right: "+ right);

			work = (left + right) / 2.0;
			realVar.setConcreteValue(work);
			distW = DistanceEstimator.getDistance(cnstr);
			
			if (distW < distL || distW < distR) {
				log.info("improoved");
				improvement = true;
				backup(realVar, distW);
			} else {
				log.info("restore");
				restore(realVar);
				break;
			}
			
			log.info("left: " + left + " " + distL + " work: " + work +" "+ distW + " right: "+ right + " " + distR);
			if (distL > distR) {
				left = work;
				distL = distW;
			} else {
				right = work;
				distR = distW;
			}
		}

		//log.warning("newRealVar: " + realVar);
		
		
		// TODO Auto-generated method stub
		return improvement;
	}

	private void roundPrecision(RealVariable realVar,
			List<Constraint<?>> cnstr, int precision, boolean isFloat) {

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
	
		log.info("Trying to chop precision " + precision + ": " + value + " -> "
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
	
	private void iterate(RealVariable realVar, List<Constraint<?>> cnstr, double delta, double factor) {

		log.info("[Loop] Trying increment " + delta + " of " + realVar.toString());

		increment(realVar, delta);
		double newDist = DistanceEstimator.getDistance(cnstr);
		log.info("[Loop] Old distance: " + oldDist + ", new distance: " + newDist);
		while (distImpr(newDist)) {
			backup(realVar, newDist);

			delta = factor * delta;
			log.info("[Loop] Trying increment " + delta + " of " + realVar);
			increment(realVar, delta);
			newDist = DistanceEstimator.getDistance(cnstr);
		}
		log.info("No improvement on " + realVar);

		restore(realVar);

		log.info("Final value of this iteration: " + realVar);
	}

	private void iterate(IntegerVariable intVar, List<Constraint<?>> cnstr, long delta) {

		log.info("Trying increment " + delta + " of " + intVar.toString());

		increment(intVar, delta);
		double newDist = DistanceEstimator.getDistance(cnstr);
		while (distImpr(newDist)) {
			backup(intVar, newDist);

			delta = 2 * delta;
			log.info("Trying increment " + delta + " of " + intVar);
			increment(intVar, delta);
			newDist = DistanceEstimator.getDistance(cnstr);
		}
		log.info("No improvement on " + intVar);

		restore(intVar);

		log.info("Final value of this iteration: " + intVar);
	}
}
