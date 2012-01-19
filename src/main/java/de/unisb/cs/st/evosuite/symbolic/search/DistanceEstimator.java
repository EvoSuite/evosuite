package de.unisb.cs.st.evosuite.symbolic.search;
import gov.nasa.jpf.JPF;

import java.util.logging.Logger;

import de.unisb.cs.st.evosuite.symbolic.expr.Comparator;
import de.unisb.cs.st.evosuite.symbolic.expr.Constraint;
import de.unisb.cs.st.evosuite.symbolic.expr.Expression;
import de.unisb.cs.st.evosuite.symbolic.expr.ExpressionHelper;
import de.unisb.cs.st.evosuite.symbolic.expr.IntegerConstant;
import de.unisb.cs.st.evosuite.symbolic.expr.IntegerExpression;
import de.unisb.cs.st.evosuite.symbolic.expr.IntegerVariable;
import de.unisb.cs.st.evosuite.symbolic.expr.RealConstant;
import de.unisb.cs.st.evosuite.symbolic.expr.RealExpression;
import de.unisb.cs.st.evosuite.symbolic.expr.RealVariable;
import de.unisb.cs.st.evosuite.symbolic.expr.StringBinaryExpression;
import de.unisb.cs.st.evosuite.symbolic.expr.StringComparison;
import de.unisb.cs.st.evosuite.symbolic.expr.StringUnaryExpression;

/**
 * @author krusev
 *
 */
public abstract class DistanceEstimator {
	
	static Logger log = JPF.getLogger("de.unisb.cs.st.evosuite.symbolic.search.DistanceEstimator");

	/**
	 * 
	 * @param constraints
	 * @return normalized distance in [0,1]
	 */
	public static double getDistance(java.util.List<Constraint<?>> constraints){
		double result = 0;
		
		/*
		 * Since the min distance that we can have (and is of interest) is 1 
		 * and the max is Long.MAX_VALUE the following can happen:
		 * 
		 * distance normalization dist/Long.MAX_VALUE:
		 * 
		 * (double)1L/Long.MAX_VALUE = 1.0842021724855044E-19
		 * ((double)43432545433L)/(Long.MAX_VALUE) == 4.708966011503397E-9
		 * 		!= ((double)43432545434L)/(Long.MAX_VALUE) == 4.708966011611818E-9
		 * (double)Long.MAX_VALUE/Long.MAX_VALUE = 1
		 * 
		 * => This works
		 * 
		 * distance normalization dist/(1+dist):
		 * 
		 * (double)1L/(1+1L) = 0.5
		 * ((double)43432545433L)/(43432545433L+1L) 
		 * 		== ((double)43432545434L)/(43432545434L+1L) == 0.9999999999769758
		 * 
		 * => for very large (and different) values  this logarithmically-like growing 
		 * function gives the same output. This means that even if we make a step in the
		 * right direction we won't be able to tell since the dist is the same
		 * 
		 * 
		 * 
		 * }=> Use the first 
		 */
		
		
		try {
			for (Constraint<?> c : constraints) {
				if (isStrConstraint(c)) {
					long strD = getStrDist(c);
					result += (double)strD/Long.MAX_VALUE; // (1.0 + strD);
//					log.warning("str" + strD + " result " + result);
				} else if (isLongConstraint(c)) {
					long intD = getIntegerDist(c);
					result += (double)intD/Long.MAX_VALUE; //(1.0 + intD);
//					log.warning("int" + intD + " result " + result);
				} else if (isRealConstraint(c)) {
					double realD = getRealDist(c);
					result += realD/Double.MAX_VALUE; //(1.0 + realD);
//					log.warning("real" + realD + " result " + result);
				} else {
					log.warning("DistanceEstimator.getDistance(): " +
							"got an unknown constraint: " + c);
					return Double.MAX_VALUE;
				}
			}
			return result;
		} catch (Exception e) {
			return 1;
		}
	}
	
	private static boolean isRealConstraint(Constraint<?> c) {
		Expression<?> exprLeft = c.getLeftOperand();
		Expression<?> exprRight = c.getRightOperand();
		
		boolean leftSide = 	exprLeft instanceof RealVariable
					||		exprLeft instanceof RealConstant
					||		exprLeft instanceof RealExpression;
		
		boolean rightSide =	exprRight instanceof RealVariable
					||		exprRight instanceof RealConstant
					||		exprRight instanceof RealExpression;
		
		return leftSide && rightSide;
	}

	private static boolean isLongConstraint(Constraint<?> c) {
		Expression<?> exprLeft = c.getLeftOperand();
		Expression<?> exprRight = c.getRightOperand();
		
		boolean leftSide = 	exprLeft instanceof IntegerVariable
					||		exprLeft instanceof IntegerConstant
					||		exprLeft instanceof IntegerExpression
//					||		exprLeft instanceof IntegerUnaryExpression
//					||		exprLeft instanceof IntegerBinaryExpression
					||		exprLeft instanceof StringUnaryExpression
					||		exprLeft instanceof StringBinaryExpression;
		
		boolean rightSide =	exprRight instanceof IntegerVariable
					||		exprRight instanceof IntegerConstant
					||		exprRight instanceof IntegerExpression
//					||		exprRight instanceof IntegerUnaryExpression
//					||		exprRight instanceof IntegerBinaryExpression
					||		exprRight instanceof StringUnaryExpression
					||		exprRight instanceof StringBinaryExpression;
		
		return leftSide && rightSide;
	}

	private static boolean isStrConstraint(Constraint<?> c) {
		Expression<?> exprLeft = c.getLeftOperand();
		Comparator cmpr = c.getComparator();
		Expression<?> exprRight = c.getRightOperand();
		
		if (exprLeft instanceof StringComparison 
				&&	exprRight instanceof IntegerConstant) {
			if (((IntegerConstant)exprRight).getConcreteValue() == 0 
					&& (cmpr == Comparator.EQ || cmpr == Comparator.NE)) {
				return true;
			}
		} 
		return false;
	}

	private static double getRealDist(Constraint<?> target) {
		double left = (Double)(target.getLeftOperand().execute());
		double right = (Double)(target.getRightOperand().execute());

		
		Comparator cmpr = target.getComparator();
		
		switch (cmpr) {
		
		case EQ:
			
			return Math.abs(left - right);
		case NE:
						
			return (left - right) != 0 ? 0 : 1;
		case LT:
			
			return left - right < 0 ? 0 : left - right + 1;
		case LE:
			
			return left - right <= 0 ? 0 : left - right;
		case GT:
			
			return left - right > 0 ? 0 : right - left + 1;
		case GE:
			
			return left - right >= 0 ? 0 : right - left;
			
		default:
			log.warning("getIntegerDist: unimplemented comparator");
			return Double.MAX_VALUE;
		}
	}

	
	public static long getIntegerDist(Constraint<?> target) {

		long left = ExpressionHelper.getLongResult(target.getLeftOperand());
		long right = ExpressionHelper.getLongResult(target.getRightOperand());

		Comparator cmpr = target.getComparator();
		
		switch (cmpr) {
		
		case EQ:
			
			return Math.abs(left - right);
		case NE:
						
			return (left - right) != 0 ? 0 : 1;
		case LT:
			
			return left - right < 0 ? 0 : left - right + 1;
		case LE:
			
			return left - right <= 0 ? 0 : left - right;
		case GT:
			
			return left - right > 0 ? 0 : right - left + 1;
		case GE:
			
			return left - right >= 0 ? 0 : right - left;
			
		default:
			log.warning("getIntegerDist: unimplemented comparator");
			return Long.MAX_VALUE;
		}
		
	}
	
	public static long getStrDist(Constraint<?> target) {
		Expression<?> exprLeft = target.getLeftOperand();
		Comparator cmpr = target.getComparator();
		StringComparison scTarget = (StringComparison) exprLeft;
		
		if (cmpr == Comparator.NE) {
			return scTarget.execute();
		} else {
			//if we don't want to satisfy return 0 
			//	if not satisfied Long.MAX_VALUE else
			return scTarget.execute() > 0 ? 0 : Long.MAX_VALUE;
		}
	}

	public static int min(int a, int b, int c) {
		if (a < b)
			return Math.min(a, c);
		else
			return Math.min(b, c);
	}

	
	public static int editDistance(String s, String t) {
		int n = s.length(); // length of s
		int m = t.length(); // length of t

		if (n == 0) {
			return m;
		} else if (m == 0) {
			return n;
		}

		int p[] = new int[n + 1]; //'previous' cost array, horizontally
		int d[] = new int[n + 1]; // cost array, horizontally
		int _d[]; //placeholder to assist in swapping p and d

		// indexes into strings s and t
		int i; // iterates through s
		int j; // iterates through t

		char t_j; // jth character of t

		int cost; // cost

		for (i = 0; i <= n; i++) {
			p[i] = i;
		}

		for (j = 1; j <= m; j++) {
			t_j = t.charAt(j - 1);
			d[0] = j;

			for (i = 1; i <= n; i++) {
				cost = s.charAt(i - 1) == t_j ? 0 : 1;
				// minimum of cell to the left+1, to the top+1, diagonally left and up +cost				
				d[i] = Math.min(Math.min(d[i - 1] + 1, p[i] + 1), p[i - 1] + cost);
			}

			// copy current distance counts to 'previous row' distance counts
			_d = p;
			p = d;
			d = _d;
		}

		// our last action in the above loop was to switch d and p, so p now 
		// actually has the most recent cost counts
		return p[n];
	}

	public static int StrEquals(String first, Object second) {
		if (first.equals(second))
			return 0; // Identical
		else {
			return editDistance(first, second.toString());
		}
	}

	public static int StrEqualsIgnoreCase(String first, String second) {
		return StrEquals(first.toLowerCase(), second.toLowerCase());
	}

	public static int StrStartsWith(String value, String prefix, int start) {
		int len = Math.min(prefix.length(), value.length());
		return StrEquals(value.substring(start, start + len), prefix);
	}

	public static int StrEndsWith(String value, String suffix) {
		int len = Math.min(suffix.length(), value.length());
		String val1 = value.substring(value.length() - len);
		return StrEquals(val1, suffix);
	}

	public static int StrIsEmpty(String value) {
		int len = value.length();
		if (len == 0) {
			return 0;
		} else {
			return len;
		}
	}

	public static int StrRegionMatches(String value, int thisStart, String string,
	        int start, int length, boolean ignoreCase) {
		if (value == null || string == null)
			throw new NullPointerException();

		if (start < 0 || string.length() - start < length) {
			return length - string.length() + start;
		}

		if (thisStart < 0 || value.length() - thisStart < length) {
			return length - value.length() + thisStart;
		}
		if (length <= 0) {
			return 0;
		}

		String s1 = value;
		String s2 = string;
		if (ignoreCase) {
			s1 = s1.toLowerCase();
			s2 = s2.toLowerCase();
		}

		return StrEquals(s1.substring(thisStart, length + thisStart), s2.substring(start, length+start));
	}

	public static int StrContains(String val, CharSequence subStr) {
		int val_length = val.length();
		int subStr_length = subStr.length();
		int min_dist = Integer.MAX_VALUE;
		String sub = subStr.toString();
		
		if (subStr_length > val_length) {
			return editDistance(val, sub);
		} else {
			int diff = val_length - subStr_length;
			for (int i = 0; i < diff+1; i++) {
				int res = StrEquals(val.substring(i, subStr_length + i), sub);
				if (res < min_dist) {
					min_dist = res;
				}
			}
		}
		return min_dist;
	}




	
}
