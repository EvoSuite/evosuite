package de.unisb.cs.st.evosuite.symbolic.search;

import gov.nasa.jpf.JPF;

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

	private double oldDist = 1;
	
	public Changer () {

	}
	
	private boolean distImpr(double newDistance) {
		return newDistance < oldDist;
	}
	
	private boolean distWrsn(double newDistance) {
		return newDistance > oldDist;
	}
	
	private void backup(IntegerVariable intVar, double newDist) {
		oldDist = newDist;
		longBackUp = intVar.execute();		
	}
	
	private void backup(StringVariable var, double newDist) {
		var.setMaxValue(var.getMinValue());
		oldDist  = newDist;
	}
	
	private void restore(IntegerVariable intVar) {		
		intVar.setConcreteValue(longBackUp);
	}
	
	private void restore(StringVariable var) {		
		var.setMinValue(var.getMaxValue());
	}

	private double getDistForVal(List<Constraint<?>> cnstr, IntegerVariable intVar, long val) {
		long backUp = intVar.getConcreteValue();
		intVar.setConcreteValue(val);
		double dist = DistanceEstimator.getDistance(cnstr);
		intVar.setConcreteValue(backUp);
		return dist;
	}
	
	/**
	 * Increments the intVar with the specified value. 
	 * If we are going out of the bounds of the variable the new value is set
	 * to the the appropriate bound.
	 * 
	 * @param intVar
	 * @param increment
	 */
	private void increment(IntegerVariable intVar, long i) {
		long oldVal = intVar.getConcreteValue();
		long newVal;
		if (i > 0) {
			if (oldVal <= intVar.getMaxValue()-i ) {
				newVal = oldVal + i;
			} else {
				newVal = intVar.getMaxValue();
			}
		} else {
			if (oldVal >= intVar.getMinValue()-i ) {
				newVal = oldVal + i;
			} else {
				newVal = intVar.getMinValue();
			}
		}
		intVar.setConcreteValue(newVal);
	}
	
	//TODO fix for other expressions that land here e.g. RealExpression
	public boolean strLocalSearch(StringVariable strVar, 
			List<Constraint<?>> cnstr, 
			HashMap<String, Object> varsToChange) {
		
		// try to remove each

		backup(strVar, DistanceEstimator.getDistance(cnstr));
		
		for (int i = strVar.execute().length() - 1; i >= 0 ; i--) {
			String newStr = 	strVar.execute().substring(0, i) 
							+ 	strVar.execute().substring(i + 1);
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

		spatialLoop:
		for (int i = 0; i < strVar.execute().length(); i++) {
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
					String newStr = strVar.execute().substring(0, i) + replacement + strVar.execute().substring(i);
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
	
	int counter = 0;
	
	public boolean intLocalSearchV2(IntegerVariable intVar, 
			List<Constraint<?>> cnstr, 
			HashMap<String, Object> varsToChange) { 
		double newDist;
		
		long left = 0;
		long right = 0;
		
		counter = 0;
		
		intVar.setConcreteValue((long)0);
		
		backup(intVar, DistanceEstimator.getDistance(cnstr));


		// Try increment
		increment(intVar, 1);
		counter++;
		newDist = DistanceEstimator.getDistance(cnstr);
		if (distImpr(newDist)) {
			right = intVar.getMaxValue();
			backup(intVar, DistanceEstimator.getDistance(cnstr));
		} else {
			// restore
			restore(intVar);

			// Try decrement
			increment(intVar, -1);
			counter++;
			newDist = DistanceEstimator.getDistance(cnstr);
			if (distImpr(newDist)) {
				left = intVar.getMinValue();
				backup(intVar, DistanceEstimator.getDistance(cnstr));
			} else {
				restore(intVar);
				return false;
			}
		}

		double distL = getDistForVal(cnstr, intVar, left);
		double distR = getDistForVal(cnstr, intVar, right);
	
		long work = (left+right)/2;
		double distW = getDistForVal(cnstr, intVar, work);
			
		while ( distW != 0.0 && Math.abs(left - right) > 1 ) {
				
//				log.warning("left: " + left + " distL: " + distL + " right: " + right + " distR: " + distR);
				counter++;
				work = (left+right)/2;
				intVar.setConcreteValue(work);
				distW = DistanceEstimator.getDistance(cnstr);
				
				if (distL > distR) {
					left = work;
					distL = distW;
				} else {
					right = work;
					distR = distW;
				}
		}
		
		backup(intVar, DistanceEstimator.getDistance(cnstr));
		
		log.warning("nr: "+counter);
		varsToChange.put(intVar.getName(), intVar.getConcreteValue());
		log.info("Finished long local search with new value " + intVar);
		if (DistanceEstimator.getDistance(cnstr) == 0) {
			return true;
		}
		return false;
	}
	
	public boolean intLocalSearch(IntegerVariable intVar, 
								List<Constraint<?>> cnstr, 
								HashMap<String, Object> varsToChange) {
		double newDist;
		boolean improvement = false;
		boolean done = false;
		counter = 0;
		
		backup(intVar, DistanceEstimator.getDistance(cnstr));

		while (!done) {
			done = true;
			// Try increment
			log.info("Trying to increment " + intVar);
			increment(intVar, 1);
			counter++;
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
				counter++;
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
		log.warning("nr: "+counter);
		if (improvement) {
			varsToChange.put(intVar.getName(), intVar.getConcreteValue());
			log.info("Finished long local search with new value " + intVar);
			if (DistanceEstimator.getDistance(cnstr) == 0) {
				return true;
			}
		}

		return false;
	}


	public boolean realLocalSearch(RealVariable realVar, 
								List<Constraint<?>> cnstr, 
								HashMap<String, Object> varsToChange) {
		// TODO Auto-generated method stub
		
		return false;
	} 
	
	private void iterate(IntegerVariable intVar , List<Constraint<?>> cnstr,
			long delta) {

		log.info("Trying increment " + delta + " of " + intVar.toString());

		increment(intVar, delta);
		double newDist = DistanceEstimator.getDistance(cnstr);
		while (distImpr(newDist)) {
			backup(intVar, newDist);

			delta = 2 * delta;
			log.info("Trying increment " + delta + " of " + intVar);
			increment(intVar, delta);
			counter++;
		}
		log.info("No improvement on " + intVar);

		restore(intVar);

		log.info("Final value of this iteration: " + intVar);
	}

	/**
	 * 
	 * @param intVar
	 * @return String representation of the value of the integer variable
	 */

}
