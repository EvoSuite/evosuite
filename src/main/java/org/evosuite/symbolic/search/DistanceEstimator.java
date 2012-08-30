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

import java.util.Collection;

import org.evosuite.symbolic.expr.Comparator;
import org.evosuite.symbolic.expr.Constraint;
import org.evosuite.symbolic.expr.Expression;
import org.evosuite.symbolic.expr.ExpressionHelper;
import org.evosuite.symbolic.expr.IntegerConstant;
import org.evosuite.symbolic.expr.IntegerExpression;
import org.evosuite.symbolic.expr.IntegerVariable;
import org.evosuite.symbolic.expr.RealConstant;
import org.evosuite.symbolic.expr.RealExpression;
import org.evosuite.symbolic.expr.RealVariable;
import org.evosuite.symbolic.expr.StringBinaryExpression;
import org.evosuite.symbolic.expr.StringComparison;
import org.evosuite.symbolic.expr.StringUnaryExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * Abstract DistanceEstimator class.
 * </p>
 * 
 * @author krusev
 */
public abstract class DistanceEstimator {

	static Logger log = LoggerFactory.getLogger(DistanceEstimator.class);

	//	static Logger log = JPF.getLogger("org.evosuite.symbolic.search.DistanceEstimator");

	private static double normalize(double x) {
		return x / (x + 1.0);
	}

	/**
	 * <p>
	 * getDistance
	 * </p>
	 * 
	 * @param constraints
	 *            a {@link java.util.Collection} object.
	 * @return normalized distance in [0,1]
	 */
	public static double getDistance(Collection<Constraint<?>> constraints) {
		double result = 0;

		try {
			for (Constraint<?> c : constraints) {
				if (isStrConstraint(c)) {
					//long strD = getStrDist(c);
					// result += (double) strD / size;
					try {
						double strD = getStringDist(c);
						result += normalize(strD);
						log.debug("C: " + c + " strDist " + strD);
					} catch (Throwable t) {
						log.debug("C: " + c + " strDist " + t);
						result += 1.0;
					}
				} else if (isLongConstraint(c)) {
					long intD = getIntegerDist(c);
					// result += (double) intD / size;
					result += normalize(intD);
					log.debug("C: " + c + " intDist " + intD);
				} else if (isRealConstraint(c)) {
					double realD = getRealDist(c);
					// result += realD / size;
					result += normalize(realD);
					log.debug("C: " + c + " realDist " + realD);
				} else {
					log.warn("DistanceEstimator.getDistance(): "
					        + "got an unknown constraint: " + c);
					return Double.MAX_VALUE;
				}
			}
			log.debug("Resulting distance: " + result);
			return Math.abs(result);
		} catch (Exception e) {
			//			log.warn(e.toString());
			e.printStackTrace();
			return Double.MAX_VALUE;
		}
	}

	private static boolean isRealConstraint(Constraint<?> c) {
		Expression<?> exprLeft = c.getLeftOperand();
		Expression<?> exprRight = c.getRightOperand();

		boolean leftSide = exprLeft instanceof RealVariable
		        || exprLeft instanceof RealConstant || exprLeft instanceof RealExpression;

		boolean rightSide = exprRight instanceof RealVariable
		        || exprRight instanceof RealConstant
		        || exprRight instanceof RealExpression;

		return leftSide && rightSide;
	}

	private static boolean isLongConstraint(Constraint<?> c) {
		Expression<?> exprLeft = c.getLeftOperand();
		Expression<?> exprRight = c.getRightOperand();

		boolean leftSide = exprLeft instanceof IntegerVariable
		        || exprLeft instanceof IntegerConstant
		        || exprLeft instanceof IntegerExpression
		        //					||		exprLeft instanceof IntegerUnaryExpression
		        //					||		exprLeft instanceof IntegerBinaryExpression
		        || exprLeft instanceof StringUnaryExpression
		        || exprLeft instanceof StringBinaryExpression;

		boolean rightSide = exprRight instanceof IntegerVariable
		        || exprRight instanceof IntegerConstant
		        || exprRight instanceof IntegerExpression
		        //					||		exprRight instanceof IntegerUnaryExpression
		        //					||		exprRight instanceof IntegerBinaryExpression
		        || exprRight instanceof StringUnaryExpression
		        || exprRight instanceof StringBinaryExpression;

		return leftSide && rightSide;
	}

	private static boolean isStrConstraint(Constraint<?> c) {
		Expression<?> exprLeft = c.getLeftOperand();
		Comparator cmpr = c.getComparator();
		Expression<?> exprRight = c.getRightOperand();

		if (exprLeft instanceof StringComparison && exprRight instanceof IntegerConstant) {
			if (((IntegerConstant) exprRight).getConcreteValue() == 0
			        && (cmpr == Comparator.EQ || cmpr == Comparator.NE)) {
				return true;
			}
		}
		return false;
	}

	public static double getStringDist(Constraint<?> target) {
		Expression<?> exprLeft = target.getLeftOperand();
		Comparator cmpr = target.getComparator();
		StringComparison scTarget = (StringComparison) exprLeft;
		log.debug("Calculating distance of constraint " + target);

		double distance = getStringDistance(scTarget);
		if (cmpr == Comparator.NE) {
			return distance;
		} else {
			//if we don't want to satisfy return 0 
			//	if not satisfied Long.MAX_VALUE else
			return distance > 0 ? 0.0 : Double.MAX_VALUE;
		}
	}

	public static double getStringDistance(StringComparison comparison) {
		try {
			String first = comparison.getLeftOperand().execute();
			String second = (String) comparison.getRightOperand().execute();

			switch (comparison.getOperator()) {
			case EQUALSIGNORECASE:
				return DistanceEstimator.StrEqualsIgnoreCase(first, second);
			case EQUALS:
				return DistanceEstimator.StrEquals(first, second);
			case ENDSWITH:
				return DistanceEstimator.StrEndsWith(first, second);
			case CONTAINS:
				return DistanceEstimator.StrContains(first, second);
			case PATTERNMATCHES:
				return DistanceEstimator.RegexMatches(second, first);
			default:
				log.warn("StringComparison: unimplemented operator!"
				        + comparison.getOperator());
				return Double.MAX_VALUE;
			}
		} catch (Exception e) {
			return Double.MAX_VALUE;
		}
	}

	private static double getRealDist(Constraint<?> target) {
		double left = (Double) (target.getLeftOperand().execute());
		double right = (Double) (target.getRightOperand().execute());

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
			log.warn("getIntegerDist: unimplemented comparator");
			return Double.MAX_VALUE;
		}
	}

	/**
	 * <p>
	 * getIntegerDist
	 * </p>
	 * 
	 * @param target
	 *            a {@link org.evosuite.symbolic.expr.Constraint} object.
	 * @return a long.
	 */
	public static long getIntegerDist(Constraint<?> target) {

		long left = ExpressionHelper.getLongResult(target.getLeftOperand());
		long right = ExpressionHelper.getLongResult(target.getRightOperand());
		//long left = (Long) target.getLeftOperand().execute();
		//long right = (Long) target.getRightOperand().execute();

		Comparator cmpr = target.getComparator();
		log.debug("Calculating distance for " + left + " " + cmpr + " " + right);

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
			log.warn("getIntegerDist: unimplemented comparator");
			return Long.MAX_VALUE;
		}

	}

	/**
	 * <p>
	 * getStrDist
	 * </p>
	 * 
	 * @param target
	 *            a {@link org.evosuite.symbolic.expr.Constraint} object.
	 * @return a long.
	 */
	public static long getStrDist(Constraint<?> target) {
		Expression<?> exprLeft = target.getLeftOperand();
		Comparator cmpr = target.getComparator();
		StringComparison scTarget = (StringComparison) exprLeft;
		log.debug("Calculating distance of constraint " + target);

		if (cmpr == Comparator.NE) {
			return scTarget.execute();
		} else {
			//if we don't want to satisfy return 0 
			//	if not satisfied Long.MAX_VALUE else
			return scTarget.execute() > 0 ? 0 : Long.MAX_VALUE;
		}
	}

	/**
	 * <p>
	 * min
	 * </p>
	 * 
	 * @param a
	 *            a int.
	 * @param b
	 *            a int.
	 * @param c
	 *            a int.
	 * @return a int.
	 */
	public static int min(int a, int b, int c) {
		if (a < b)
			return Math.min(a, c);
		else
			return Math.min(b, c);
	}

	/**
	 * <p>
	 * editDistance
	 * </p>
	 * 
	 * @param s
	 *            a {@link java.lang.String} object.
	 * @param t
	 *            a {@link java.lang.String} object.
	 * @return a int.
	 */
	public static double editDistance(String s, String t) {
		int n = s.length(); // length of s
		int m = t.length(); // length of t

		if (n == 0) {
			return m;
		} else if (m == 0) {
			return n;
		}

		double p[] = new double[n + 1]; //'previous' cost array, horizontally
		double d[] = new double[n + 1]; // cost array, horizontally
		double _d[]; //placeholder to assist in swapping p and d

		// indexes into strings s and t
		int i; // iterates through s
		int j; // iterates through t

		char t_j; // jth character of t

		double cost; // cost

		for (i = 0; i <= n; i++) {
			p[i] = i;
		}

		for (j = 1; j <= m; j++) {
			t_j = t.charAt(j - 1);
			d[0] = j;

			for (i = 1; i <= n; i++) {
				//				cost = s.charAt(i - 1) == t_j ? 0 : 1;
				cost = normalize(Math.abs(s.charAt(i - 1) - t_j));
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

	/**
	 * <p>
	 * StrEquals
	 * </p>
	 * 
	 * @param first
	 *            a {@link java.lang.String} object.
	 * @param second
	 *            a {@link java.lang.Object} object.
	 * @return a int.
	 */
	public static double StrEquals(String first, Object second) {
		if (first.equals(second))
			return 0; // Identical
		else {
			return editDistance(first, second.toString());
		}
	}

	/**
	 * <p>
	 * StrEqualsIgnoreCase
	 * </p>
	 * 
	 * @param first
	 *            a {@link java.lang.String} object.
	 * @param second
	 *            a {@link java.lang.String} object.
	 * @return a int.
	 */
	public static double StrEqualsIgnoreCase(String first, String second) {
		return StrEquals(first.toLowerCase(), second.toLowerCase());
	}

	/**
	 * <p>
	 * StrStartsWith
	 * </p>
	 * 
	 * @param value
	 *            a {@link java.lang.String} object.
	 * @param prefix
	 *            a {@link java.lang.String} object.
	 * @param start
	 *            a int.
	 * @return a int.
	 */
	public static double StrStartsWith(String value, String prefix, int start) {
		int len = Math.min(prefix.length(), value.length());
		int end = (start + len > value.length()) ? value.length() : start + len;
		return StrEquals(value.substring(start, end), prefix);
	}

	/**
	 * <p>
	 * StrEndsWith
	 * </p>
	 * 
	 * @param value
	 *            a {@link java.lang.String} object.
	 * @param suffix
	 *            a {@link java.lang.String} object.
	 * @return a int.
	 */
	public static double StrEndsWith(String value, String suffix) {
		int len = Math.min(suffix.length(), value.length());
		String val1 = value.substring(value.length() - len);
		return StrEquals(val1, suffix);
	}

	/**
	 * <p>
	 * StrIsEmpty
	 * </p>
	 * 
	 * @param value
	 *            a {@link java.lang.String} object.
	 * @return a int.
	 */
	public static double StrIsEmpty(String value) {
		int len = value.length();
		if (len == 0) {
			return 0;
		} else {
			return len;
		}
	}

	/**
	 * <p>
	 * StrRegionMatches
	 * </p>
	 * 
	 * @param value
	 *            a {@link java.lang.String} object.
	 * @param thisStart
	 *            a int.
	 * @param string
	 *            a {@link java.lang.String} object.
	 * @param start
	 *            a int.
	 * @param length
	 *            a int.
	 * @param ignoreCase
	 *            a boolean.
	 * @return a int.
	 */
	public static double StrRegionMatches(String value, int thisStart, String string,
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

		return StrEquals(s1.substring(thisStart, length + thisStart),
		                 s2.substring(start, length + start));
	}

	/**
	 * <p>
	 * StrContains
	 * </p>
	 * 
	 * @param val
	 *            a {@link java.lang.String} object.
	 * @param subStr
	 *            a {@link java.lang.CharSequence} object.
	 * @return a int.
	 */
	public static double StrContains(String val, CharSequence subStr) {
		int val_length = val.length();
		int subStr_length = subStr.length();
		double min_dist = Double.MAX_VALUE;
		String sub = subStr.toString();

		if (subStr_length > val_length) {
			return editDistance(val, sub);
		} else {
			int diff = val_length - subStr_length;
			for (int i = 0; i < diff + 1; i++) {
				double res = StrEquals(val.substring(i, subStr_length + i), sub);
				if (res < min_dist) {
					min_dist = res;
				}
			}
		}
		return min_dist;
	}

	public static double RegexMatches(String val, String regex) {
		return RegexDistance.getDistance(val, regex);
	}

}
