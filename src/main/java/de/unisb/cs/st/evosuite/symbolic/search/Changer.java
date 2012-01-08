package de.unisb.cs.st.evosuite.symbolic.search;

import gov.nasa.jpf.JPF;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import de.unisb.cs.st.evosuite.symbolic.expr.Comparator;
import de.unisb.cs.st.evosuite.symbolic.expr.Constraint;
import de.unisb.cs.st.evosuite.symbolic.expr.Expression;
import de.unisb.cs.st.evosuite.symbolic.expr.IntegerVariable;
import de.unisb.cs.st.evosuite.symbolic.expr.RealVariable;
import de.unisb.cs.st.evosuite.symbolic.expr.StringComparison;
import de.unisb.cs.st.evosuite.symbolic.expr.StringVariable;

/**
 * @author krusev
 *
 */
public class Changer {

	static Logger log = JPF.getLogger("de.unisb.cs.st.evosuite.symbolic.search.Changer");

	private double oldDistance = Double.MAX_VALUE;
	
	public Changer () {

	}
	
	private boolean distanceImproved(double newDistance) {
		return newDistance < oldDistance;
	}
	
	private void backup(StringVariable var, double fitness) {
		//oldStrValue = var.execute();
		
		var.setMaxValue(var.getMinValue());
		oldDistance = fitness;
	}
	
	private void restore(StringVariable var) {		
		var.setMinValue(var.getMaxValue());
	}
	
	
	public boolean strLocalSearch(StringVariable strVar, Constraint<?> target,
			List<Constraint<?>> cnstr, HashMap<String, Object> result) {
		Expression<?> expr = target.getLeftOperand();
		//String result = strVar.execute();
		StringComparison scTarget;
		boolean satisfy = true;
		

		if (!(expr instanceof StringComparison)) {

			return false;
		}
		scTarget = (StringComparison) expr;
		// See if we want to satisfy the condition
		satisfy = (target.getComparator() == Comparator.NE); 
		if (!satisfy) {

			return false; //TODO think about what to do here
		}
		
		
		//TODO this will not work for spp and sepp 
		//FIX!!!

		
		// try to remove each

		backup(strVar, DistanceEstimator.getStringDistance(scTarget));
		//String workStr = strVar.execute(); 
		
		for (int i = strVar.execute().length() - 1; i >= 0 ; i--) {
			String newStr = strVar.execute().substring(0, i) + strVar.execute().substring(i + 1);
			strVar.setMinValue(newStr);
			//logger.info(" " + i + " " + strVar.execute() + "/" + strVar.execute().length() + " -> "
			//        + newString + "/" + newString.length());
			double newDist = DistanceEstimator.getStringDistance(scTarget);
			boolean reachable = DistanceEstimator.areReachable(cnstr);
			if (newDist == 0 && reachable) {
				log.warning("newVar: " + newStr);
				result.put(strVar.getName(), newStr);
				return true;
			}
			if (distanceImproved(newDist) && reachable) {
				backup(strVar, newDist);
			} else {
				restore(strVar);
			}
		}

		
		
		
		// try to replace each 
		
		backup(strVar, DistanceEstimator.getStringDistance(scTarget));

		for (int i = 0; i < strVar.execute().length(); i++) {
			char oldChar = strVar.execute().charAt(i);
			char[] characters = strVar.execute().toCharArray();
			for (char replacement = 0; replacement < 128; replacement++) {
				if (replacement != oldChar) {
					characters[i] = replacement;
					String newStr = new String(characters);
					strVar.setMinValue(newStr);
					//logger.debug(" " + i + " " + strVar.execute() + "/" + strVar.execute().length()
					//        + " -> " + newString + "/" + newString.length());

					double newDist = DistanceEstimator.getStringDistance(scTarget);
					boolean reachable = DistanceEstimator.areReachable(cnstr);
					if (newDist == 0 && reachable) {
						log.warning("newVar: " + newStr);
						result.put(strVar.getName(), newStr);
						return true;
					}
					if (distanceImproved(newDist) && reachable) {
						backup(strVar, newDist);
						break;//TODO check if this is correct
					} else {
						restore(strVar);
					}
				}
			}
		}
		
		// try to add in front and back		
		
		backup(strVar, DistanceEstimator.getStringDistance(scTarget));

		boolean add = true;

		while (add) {
			add = false;
			int position = strVar.execute().length();
			char[] characters = Arrays.copyOf(strVar.execute().toCharArray(), position + 1);
			for (char replacement = 0; replacement < 128; replacement++) {
				characters[position] = replacement;
				String newStr = new String(characters);
				strVar.setMinValue(newStr);
				//logger.debug(" " + strVar.execute() + "/" + strVar.execute().length() + " -> " + newString
				//        + "/" + newString.length());
				
				double newDist = DistanceEstimator.getStringDistance(scTarget);
				boolean reachable = DistanceEstimator.areReachable(cnstr);
				if (newDist == 0 && reachable) {
					log.warning("newVar: " + newStr);
					result.put(strVar.getName(), newStr);
					return true;
				}
				if (distanceImproved(newDist) && reachable) {
					backup(strVar, newDist);
					add = true;
					break;
				} else {
					restore(strVar);
				}
			}
		}

		add = true;
		while (add) {
			add = false;
			int position = 0;
			char[] characters = (" " + strVar.execute()).toCharArray();
			for (char replacement = 0; replacement < 128; replacement++) {
				characters[position] = replacement;
				String newStr = new String(characters);
				strVar.setMinValue(newStr);
				//logger.debug(" " + strVar.execute() + "/" + strVar.execute().length() + " -> " + newString
				//        + "/" + newString.length());

				double newDist = DistanceEstimator.getStringDistance(scTarget);
				boolean reachable = DistanceEstimator.areReachable(cnstr);
				if (newDist == 0 && reachable) {
					log.warning("newVar: " + newStr);
					result.put(strVar.getName(), newStr);
					return true;
				}
				if (distanceImproved(newDist) && reachable) {
					backup(strVar, newDist);
					add = true;
					break;
				} else {
					restore(strVar);
				}
			}
		}

		return false;
	} 


	public boolean intLocalSearch(IntegerVariable strVar, Constraint<?> target,
			List<Constraint<?>> cnstr, HashMap<String, Object> result) {
		// TODO Auto-generated method stub
	
		return false;
	}

	public boolean realLocalSearch(RealVariable realVar, Constraint<?> target,
			List<Constraint<?>> cnstr, HashMap<String, Object> result) {
		// TODO Auto-generated method stub
		
		return false;
	} 

}
