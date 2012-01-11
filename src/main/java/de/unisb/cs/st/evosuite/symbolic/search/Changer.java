package de.unisb.cs.st.evosuite.symbolic.search;

import gov.nasa.jpf.JPF;

import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import de.unisb.cs.st.evosuite.symbolic.expr.Comparator;
import de.unisb.cs.st.evosuite.symbolic.expr.Constraint;
import de.unisb.cs.st.evosuite.symbolic.expr.Expression;
import de.unisb.cs.st.evosuite.symbolic.expr.ExpressionHelper;
import de.unisb.cs.st.evosuite.symbolic.expr.IntegerConstant;
import de.unisb.cs.st.evosuite.symbolic.expr.IntegerExpression;
import de.unisb.cs.st.evosuite.symbolic.expr.IntegerVariable;
import de.unisb.cs.st.evosuite.symbolic.expr.Operator;
import de.unisb.cs.st.evosuite.symbolic.expr.RealVariable;
import de.unisb.cs.st.evosuite.symbolic.expr.StringBinaryExpression;
import de.unisb.cs.st.evosuite.symbolic.expr.StringComparison;
import de.unisb.cs.st.evosuite.symbolic.expr.StringExpression;
import de.unisb.cs.st.evosuite.symbolic.expr.StringMultipleExpression;
import de.unisb.cs.st.evosuite.symbolic.expr.StringUnaryExpression;
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
	
	private boolean distanceWorsen(double newDistance) {
		return newDistance > oldDistance;
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
			List<Constraint<?>> cnstr, HashMap<String, Object> varsToChange) {
		
		// try to remove each

		backup(strVar, DistanceEstimator.getStringDist(target));
		
		for (int i = strVar.execute().length() - 1; i >= 0 ; i--) {
			String newStr = strVar.execute().substring(0, i) + strVar.execute().substring(i + 1);
			strVar.setMinValue(newStr);
			//logger.info(" " + i + " " + strVar.execute() + "/" + strVar.execute().length() + " -> "
			//        + newString + "/" + newString.length());
			double newDist = DistanceEstimator.getStringDist(target);
			boolean reachable = DistanceEstimator.areReachable(cnstr);
			if (newDist <= 0 && reachable) {
				varsToChange.put(strVar.getName(), newStr);
				return true;
			}
			if (distanceImproved(newDist) && reachable) {
				backup(strVar, newDist);
			} else {
				restore(strVar);
			}
		}

		
		// try to replace each 
		
		backup(strVar, DistanceEstimator.getStringDist(target));

		spatialLoop:
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

					double newDist = DistanceEstimator.getStringDist(target);
					boolean reachable = DistanceEstimator.areReachable(cnstr);
					if (newDist <= 0 && reachable) {
						varsToChange.put(strVar.getName(), newStr);
						return true;
					}
					if (distanceImproved(newDist) && reachable) {
						backup(strVar, newDist);
						break;
					} else {
						restore(strVar);						
					}
					if (distanceWorsen(newDist)) {
						//skip this place
						continue spatialLoop;
					}
				}
			}
		}
		
		// try to add everywhere
		
		backup(strVar, DistanceEstimator.getStringDist(target));

		for (int i = 0; i < strVar.execute().length() + 1; i++) {
			boolean add = true;
			while (add) {
				add = false;
				for (char replacement = 0; replacement < 128; replacement++) {
					String newStr = strVar.execute().substring(0, i) + replacement + strVar.execute().substring(i);
					strVar.setMinValue(newStr);
					//logger.debug(" " + strVar.execute() + "/" + strVar.execute().length() + " -> " + newString
					//        + "/" + newString.length());
					double newDist = DistanceEstimator.getStringDist(target);
					boolean reachable = DistanceEstimator.areReachable(cnstr);
					if (newDist <= 0 && reachable) {
						varsToChange.put(strVar.getName(), newStr);
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
		}
		return false;
	} 
	
//	public boolean strLocalSearch(StringVariable strVar, Constraint<?> target,
//			List<Constraint<?>> cnstr, HashMap<String, Object> varsToChange) {
//		boolean result = false;
//		
//		Expression<?> exprLeft = target.getLeftOperand();
//		Comparator cmpr = target.getComparator();
//		Expression<?> exprRight = target.getRightOperand();
//		
//		//TODO all this should be in the getDistance function
//		//this will also solve the next todo
//		
//		//check if we have a string comparison
//		if (	exprLeft instanceof StringComparison 
//			 &&	exprRight instanceof IntegerConstant ) {
//			StringComparison scTarget = (StringComparison) exprLeft;
//			
//			if (((IntegerConstant)exprRight).getConcreteValue() == 0 ) {
//				
//				//check whether we want to satisfy the condition
//				if (cmpr == Comparator.NE) {
//					result = solveStrCmpr(strVar, scTarget, cnstr, varsToChange);
//				} else {
//					//TODO what to do if we don't want to satisfy it
//				}
//			}
//		} else { 
//			/* since we have a string variable we always have a String expression
//			 * we must be in some string method that returns int
//			 * 
//			 * The possibilities are
//			 * 	StringUnaryExpression
//			 * 		Length
//			 *	StringBinatyExpression
//			 *		INDEXOFC
//			 *		INDEXOFS
//			 *	StringMultipleExpression
//			 *		INDEXOFCI
//			 *		INDEXOFSI
//			 */
//			
//
//			long left = ExpressionHelper.getLongResult(exprLeft);
//			long right = ExpressionHelper.getLongResult(exprRight);
//			
//			//TODO now that you have the values find the matching variable
//				
//			log.warning("left: " + exprLeft + " val: " + left);
//			log.warning("right: " + exprRight + " val: " + right);
//		}
//		return result;
//	} 

	private boolean solveStrCmpr(StringVariable strVar,
			StringComparison scTarget, List<Constraint<?>> cnstr,
			HashMap<String, Object> varsToChange) {
		// try to remove each

		backup(strVar, DistanceEstimator.getStringDistance(scTarget));
		
		for (int i = strVar.execute().length() - 1; i >= 0 ; i--) {
			String newStr = strVar.execute().substring(0, i) + strVar.execute().substring(i + 1);
			strVar.setMinValue(newStr);
			//logger.info(" " + i + " " + strVar.execute() + "/" + strVar.execute().length() + " -> "
			//        + newString + "/" + newString.length());
			double newDist = DistanceEstimator.getStringDistance(scTarget);
			boolean reachable = DistanceEstimator.areReachable(cnstr);
			if (newDist <= 0 && reachable) {
				varsToChange.put(strVar.getName(), newStr);
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
					if (newDist <= 0 && reachable) {
						varsToChange.put(strVar.getName(), newStr);
						return true;
					}
					if (distanceImproved(newDist) && reachable) {
						backup(strVar, newDist);
						break;
					} else {
						restore(strVar);
					}
				}
			}
		}
		
		// try to add in front and back		
		
		backup(strVar, DistanceEstimator.getStringDistance(scTarget));

		for (int i = 0; i < strVar.execute().length() + 1; i++) {
			boolean add = true;
			while (add) {
				add = false;
				for (char replacement = 0; replacement < 128; replacement++) {
					String newStr = strVar.execute().substring(0, i) + replacement + strVar.execute().substring(i);
					strVar.setMinValue(newStr);
					//logger.debug(" " + strVar.execute() + "/" + strVar.execute().length() + " -> " + newString
					//        + "/" + newString.length());
					double newDist = DistanceEstimator.getStringDistance(scTarget);
					boolean reachable = DistanceEstimator.areReachable(cnstr);
					if (newDist <= 0 && reachable) {
						varsToChange.put(strVar.getName(), newStr);
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
		}
		return false;
	}

	public boolean intLocalSearch(IntegerVariable strVar, Constraint<?> target,
			List<Constraint<?>> cnstr, HashMap<String, Object> varsToChange) {
		// TODO Auto-generated method stub

		return false;
	}

	public boolean realLocalSearch(RealVariable realVar, Constraint<?> target,
			List<Constraint<?>> cnstr, HashMap<String, Object> varsToChange) {
		// TODO Auto-generated method stub
		
		return false;
	} 

}
