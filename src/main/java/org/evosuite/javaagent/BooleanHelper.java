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
/**
 * 
 */
package org.evosuite.javaagent;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.evosuite.Properties;
import org.objectweb.asm.Opcodes;

/**
 * <p>
 * BooleanHelper class.
 * </p>
 * 
 * @author Gordon Fraser
 */
public class BooleanHelper {

	/** Constant <code>K=Integer.MAX_VALUE - 2</code> */
	public static final int K = Integer.MAX_VALUE - 2;
	//public static final int K = 1000;

	private static final int TRUE = K;

	private static final int FALSE = -K;

	/**
	 * <p>
	 * clearStack
	 * </p>
	 */
	public static void clearStack() {
		lastDistance.clear();
	}

	/**
	 * Helper function that is called instead of Object.equals
	 * 
	 * @param obj1
	 *            a {@link java.lang.Object} object.
	 * @param obj2
	 *            a {@link java.lang.Object} object.
	 * @return a int.
	 */
	public static int objectEquals(Object obj1, Object obj2) {
		return obj1.equals(obj2) ? TRUE : FALSE;
	}

	/**
	 * Helper function that is called instead of Collection.isEmpty
	 * 
	 * @param c
	 *            a {@link java.util.Collection} object.
	 * @return a int.
	 */
	public static int collectionIsEmpty(Collection<?> c) {
		return c.isEmpty() ? TRUE : -c.size();
	}

	/**
	 * Helper function that is called instead of Collection.contains
	 * 
	 * @param c
	 *            a {@link java.util.Collection} object.
	 * @param o1
	 *            a {@link java.lang.Object} object.
	 * @return a int.
	 */
	public static int collectionContains(Collection<?> c, Object o1) {
		int matching = 0;
		double min_distance = Double.MAX_VALUE;
		for (Object o2 : c) {
			if (o2.equals(o1))
				matching++;
			else {
				if (o2 != null && o1 != null) {
					if (o2.getClass().equals(o1.getClass()) && o1 instanceof Number) {
						Number n1 = (Number) o1;
						Number n2 = (Number) o2;
						min_distance = Math.min(min_distance,
						                        Math.abs(n1.doubleValue()
						                                - n2.doubleValue()));
					}
				}
			}
		}
		if (matching > 0)
			return matching;
		else {
			if (min_distance == Double.MAX_VALUE)
				return -c.size() - 1;
			else {
				return -1 * (int) Math.ceil(K * min_distance / (min_distance + 1.0));
			}

		}
	}

	/**
	 * Helper function that is called instead of Collection.containsAll
	 * 
	 * @param c
	 *            a {@link java.util.Collection} object.
	 * @param c2
	 *            a {@link java.util.Collection} object.
	 * @return a int.
	 */
	public static int collectionContainsAll(Collection<?> c, Collection<?> c2) {
		int mismatch = 0;
		for (Object o : c2) {
			if (c.contains(o))
				mismatch++;
		}
		return mismatch > 0 ? -mismatch : c2.size();
	}

	/**
	 * Helper function that is called instead of Map.containsKey
	 * 
	 * @param o1
	 *            a {@link java.lang.Object} object.
	 * @param m
	 *            a {@link java.util.Map} object.
	 * @return a int.
	 */
	public static int mapContainsKey(Map<?, ?> m, Object o1) {
		return collectionContains(m.keySet(), o1);
	}

	/**
	 * Helper function that is called instead of Map.containsValue
	 * 
	 * @param o1
	 *            a {@link java.lang.Object} object.
	 * @param m
	 *            a {@link java.util.Map} object.
	 * @return a int.
	 */
	public static int mapContainsValue(Map<?, ?> m, Object o1) {
		return collectionContains(m.values(), o1);
	}

	/**
	 * Helper function that is called instead of Map.isEmpty
	 * 
	 * @param m
	 *            a {@link java.util.Map} object.
	 * @return a int.
	 */
	public static int mapIsEmpty(Map<?, ?> m) {
		return m.isEmpty() ? TRUE : -m.size();
	}

	static Map<Integer, Integer> lastDistance = new HashMap<Integer, Integer>();

	/**
	 * Keep track of the distance for this predicate
	 * 
	 * @param branchId
	 *            a int.
	 * @param distance
	 *            a int.
	 */
	public static void pushPredicate(int distance, int branchId) {
		//		Branch branch = BranchPool.getBranch(branchId);
		//		//if (branch.getClassName().equals(Properties.TARGET_CLASS))
		//		System.out.println("Keeping branch id: " + branch.getClassName() + " - "
		//		        + branchId + " - " + distance);
		//System.out.println("Keeping branch id: " + branchId + " - " + distance);
		lastDistance.put(branchId, Math.abs(distance));
	}

	/**
	 * Retrieve the distance of a predicate with its given approximation level
	 * 
	 * @param branchId
	 *            a int.
	 * @param approximationLevel
	 *            a int.
	 * @param value
	 *            a int.
	 * @return a int.
	 */
	public static int getDistance(int branchId, int approximationLevel, int value) {
		int distance = Integer.MAX_VALUE;
		//		if (approximationLevel < 0)
		//			approximationLevel = 0;
		if (branchId > 0) {
			if (lastDistance.containsKey(branchId)) {
				distance = lastDistance.get(branchId);
			}
		}
		//		Branch branch = BranchPool.getBranch(branchId);
		//if (branch.getClassName().equals(Properties.TARGET_CLASS))
		//System.out.println("Getting branch id: " + approximationLevel + "/" + branchId
		//        + "/" + value + " - " + distance);

		//		System.out.println("Getting branch id: " + branch.getClassName() + " - "
		//		        + branchId + " - " + distance);
		double val = (1.0 + normalize(distance)) / Math.pow(2.0, approximationLevel);
		//double val = (1.0 + Math.abs(distance)) / Math.pow(2.0, approximationLevel);

		//		int d = (int) Math.ceil(K * val / (val + 1));
		int d = (int) Math.ceil(K * val);
		if (d == 0)
			d = 1;
		if (value <= 0)
			d = -d;
		//System.out.println("Value: " + distance + ", Distance: " + d);
		return d;
	}

	/**
	 * FIXME: the use of this function needs to be clarified
	 * 
	 * @param distance
	 * @return
	 */
	private static double normalize(int distance) {
		//		double k = K;
		double k = Properties.MAX_INT;
		double d = distance;
		return d / (d + 0.5 * k);
		//return distance / (distance + 1.0);
	}

	/**
	 * Replacement function for double comparison
	 * 
	 * @param d1
	 *            a double.
	 * @param d2
	 *            a double.
	 * @return a int.
	 */
	public static int doubleSub(double d1, double d2) {
		if (d1 == d2) {
			return 0;
		} else {
			double diff = d1 - d2;
			double diff2 = Math.signum(diff) * Math.abs(diff) / (1.0 + Math.abs(diff));
			//			int d3 = (int) Math.round(Integer.MAX_VALUE * diff2);
			int d3 = (int) (diff2 < 0 ? Math.floor(Integer.MAX_VALUE * diff2)
			        : Math.ceil(Integer.MAX_VALUE * diff2));
			return d3;
		}
	}

	/**
	 * Replacement function for float comparison
	 * 
	 * @param f1
	 *            a float.
	 * @param f2
	 *            a float.
	 * @return a int.
	 */
	public static int floatSub(float f1, float f2) {
		if (f1 == f2)
			return 0;
		else {
			double diff = f1 - f2;
			double diff2 = Math.signum(diff) * Math.abs(diff) / (1.0F + Math.abs(diff));
			int d3 = (int) Math.ceil(Integer.MAX_VALUE * diff2);
			return d3;
		}
	}

	/**
	 * <p>
	 * intSub
	 * </p>
	 * 
	 * @param a
	 *            a int.
	 * @param b
	 *            a int.
	 * @return a int.
	 */
	public static int intSub(int a, int b) {
		long sub = (long) a - (long) b;
		if (sub < -K)
			return -K;
		else if (sub > K)
			return K;
		return (int) sub;
	}

	/**
	 * Replacement function for long comparison
	 * 
	 * @param l1
	 *            a long.
	 * @param l2
	 *            a long.
	 * @return a int.
	 */
	public static int longSub(long l1, long l2) {
		if (l1 == l2)
			return 0;
		else {
			double diff = l1 - l2;
			double diff2 = Math.signum(diff) * Math.abs(diff) / (1.0 + Math.abs(diff));
			int d3 = (int) Math.ceil(Integer.MAX_VALUE * diff2);
			return d3;
		}
	}

	/**
	 * <p>
	 * fromDouble
	 * </p>
	 * 
	 * @param d
	 *            a double.
	 * @return a int.
	 */
	@Deprecated
	public static int fromDouble(double d) {
		//logger.info("Converting double " + d);
		/*
		if (d > Integer.MAX_VALUE)
			return Integer.MAX_VALUE;
		else if (d < Integer.MIN_VALUE)
			return Integer.MIN_VALUE;
		else 
		*/
		if (d == 0.0)
			return 0;
		else {
			double d2 = Math.signum(d) * Math.abs(d) / (1.0 + Math.abs(d));
			//logger.info(" -> " + d2);
			int d3 = (int) Math.round(Integer.MAX_VALUE * d2);
			//logger.info(" -> " + d3);
			return d3;
		}
	}

	/**
	 * <p>
	 * fromFloat
	 * </p>
	 * 
	 * @param d
	 *            a float.
	 * @return a int.
	 */
	@Deprecated
	public static int fromFloat(float d) {
		//logger.info("Converting float " + d);
		/*
		if (d > Integer.MAX_VALUE)
			return Integer.MAX_VALUE;
		else if (d < Integer.MIN_VALUE)
			return Integer.MIN_VALUE;
		else */
		if (d == 0.0f)
			return 0;
		else {
			float d2 = Math.signum(d) * Math.abs(d) / (1f + Math.abs(d));
			//logger.info(" ->" + d2);
			int d3 = Math.round(Integer.MAX_VALUE * d2);
			//logger.info(" -> " + d3);
			return d3;
		}
	}

	/**
	 * <p>
	 * fromLong
	 * </p>
	 * 
	 * @param d
	 *            a long.
	 * @return a int.
	 */
	@Deprecated
	public static int fromLong(long d) {
		/*
		if (d > Integer.MAX_VALUE)
			return Integer.MAX_VALUE;
		else if (d < Integer.MIN_VALUE)
			return Integer.MIN_VALUE;
			*/
		//else
		//	return (int) d;
		if (d == 0L)
			return 0;
		double d2 = Math.signum(d) * Math.abs(d) / (1L + Math.abs(d));
		int d3 = (int) Math.round(Integer.MAX_VALUE * d2);
		return d3;
	}

	/**
	 * <p>
	 * booleanToInt
	 * </p>
	 * 
	 * @param b
	 *            a boolean.
	 * @return a int.
	 */
	public static int booleanToInt(boolean b) {
		if (b)
			return TRUE;
		else
			return FALSE;
	}

	/**
	 * <p>
	 * intToBoolean
	 * </p>
	 * 
	 * @param x
	 *            a int.
	 * @return a boolean.
	 */
	public static boolean intToBoolean(int x) {
		return x > 0;
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
	 * compareBoolean
	 * </p>
	 * 
	 * @param a
	 *            a int.
	 * @param b
	 *            a int.
	 * @return a int.
	 */
	public static int compareBoolean(int a, int b) {
		if ((a > 0 && b > 0) || (a <= 0 && b <= 0))
			return Math.abs(a - b);
		else
			return -1 * Math.abs(a - b);
	}

	/**
	 * <p>
	 * editDistance_old
	 * </p>
	 * 
	 * @param s
	 *            a {@link java.lang.String} object.
	 * @param t
	 *            a {@link java.lang.String} object.
	 * @return a int.
	 */
	public static int editDistance_old(String s, String t) {
		int d[][]; // matrix
		int n; // length of s
		int m; // length of t
		int i; // iterates through s
		int j; // iterates through t
		char s_i; // ith character of s
		char t_j; // jth character of t
		int cost; // cost

		int k = 127;

		// Step 1

		n = s.length();
		m = t.length();
		if (n == 0) {
			return m;
		}
		if (m == 0) {
			return n;
		}
		d = new int[n + 1][m + 1];

		// Step 2

		for (i = 0; i <= n; i++) {
			d[i][0] = i;
		}

		for (j = 0; j <= m; j++) {
			d[0][j] = j;
		}

		// Step 3

		for (i = 1; i <= n; i++) {

			s_i = s.charAt(i - 1);

			// Step 4

			for (j = 1; j <= m; j++) {

				t_j = t.charAt(j - 1);

				// Step 5

				if (s_i == t_j) {
					cost = 0;
				} else {
					//					cost = 127/4 + 3 * Math.abs(s_i - t_j)/4;
					cost = 127;
				}

				// Step 6

				d[i][j] = min(d[i - 1][j] + k, d[i][j - 1] + k, d[i - 1][j - 1] + cost);

			}

		}

		// Step 7

		return d[n][m];
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
	public static int editDistance(String s, String t) {
		//if (s == null || t == null) {
		//	throw new IllegalArgumentException("Strings must not be null");
		//}

		/*
		    The difference between this impl. and the previous is that, rather 
		     than creating and retaining a matrix of size s.length()+1 by t.length()+1, 
		     we maintain two single-dimensional arrays of length s.length()+1.  The first, d,
		     is the 'current working' distance array that maintains the newest distance cost
		     counts as we iterate through the characters of String s.  Each time we increment
		     the index of String t we are comparing, d is copied to p, the second int[].  Doing so
		     allows us to retain the previous cost counts as required by the algorithm (taking 
		     the minimum of the cost count to the left, up one, and diagonally up and to the left
		     of the current cost count being calculated).  (Note that the arrays aren't really 
		     copied anymore, just switched...this is clearly much better than cloning an array 
		     or doing a System.arraycopy() each time  through the outer loop.)

		     Effectively, the difference between the two implementations is this one does not 
		     cause an out of memory condition when calculating the LD over two very large strings.  		
		 */

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

	/*
	 * Return a positive number if the 2 strings are equal, or a <=0 value representing
	 * how different they are
	 */
	/**
	 * <p>
	 * StringEquals
	 * </p>
	 * 
	 * @param first
	 *            a {@link java.lang.String} object.
	 * @param second
	 *            a {@link java.lang.Object} object.
	 * @return a int.
	 */
	public static int StringEquals(String first, Object second) {
		if (first == null) {
			throw new IllegalArgumentException(
			        "StringEquals is not supposed to work on a null caller");
		}
		// Comparison with null is always false
		if (second == null) {
			return -K;
		}

		if (first.equals(second)) {
			return K; // Identical
		} else {
			//System.out.println("Edit distance between " + first + " and " + second
			//       + " is " + -editDistance(first, second.toString()) + " / "
			//      + getLevenshteinDistance(first, (String) second));
			//return -editDistance(first, second.toString());
			//return -getLevenshteinDistance(first, (String) second);
			return -getDistanceBasedOnLeftAlignment(first, second.toString());
		}
	}

	public static int StringMatches(String str, String regex) {
		int distance = RegexDistance.getDistance(str, regex);

		if (distance > 0)
			return -distance;
		else
			return K;
	}

	public static int StringMatchRegex(String regex, CharSequence input) {
		int distance = RegexDistance.getDistance(input.toString(), regex);

		if (distance > 0)
			return -distance;
		else
			return K;
	}

	public static int StringMatchRegex(Matcher matcher) {
		Pattern pattern = matcher.pattern();
		String regex = pattern.pattern();
		CharSequence input;
		try {
			Field textField = Matcher.class.getDeclaredField("text");
			textField.setAccessible(true);
			input = (CharSequence) textField.get(matcher);
			int distance = RegexDistance.getDistance(input.toString(), regex);
			if (distance > 0)
				return -distance;
			else
				return K;
		} catch (Throwable t) {
			// TODO Auto-generated catch block
			t.printStackTrace();
			return matcher.matches() ? 1 : -1;
		}
	}

	/**
	 * <p>
	 * getDistanceBasedOnLeftAlignment
	 * </p>
	 * 
	 * @param a
	 *            a {@link java.lang.String} object.
	 * @param b
	 *            a {@link java.lang.String} object.
	 * @return a int.
	 */
	public static int getDistanceBasedOnLeftAlignment(String a, String b) {
		if (a == b) {
			assert (false);
			return K;
		} else if (a == null && b != null) {
			return b.length() + 1; // +1 is important to handle the empty string "" 
		} else if (a != null && b == null) {
			return a.length() + 1;
		} else {
			int differences = 0;
			int min = Math.min(a.length(), b.length());
			int max = Math.max(a.length(), b.length());
			differences += (max - min);
			for (int i = 0; i < min; i++) {
				/*
				 * Note: instead of just checking for mismatches, we could use something more sophisticated.
				 * Eg, "a" is closer to "e" than "!". But maybe, considering the type of local search
				 * we do, we don't need to do it
				 */
				if (a.charAt(i) != b.charAt(i)) {
					differences++;
				}
			}
			return differences;
		}
	}

	/**
	 * <p>
	 * StringEqualsIgnoreCase
	 * </p>
	 * 
	 * @param first
	 *            a {@link java.lang.String} object.
	 * @param second
	 *            a {@link java.lang.String} object.
	 * @return a int.
	 */
	public static int StringEqualsIgnoreCase(String first, String second) {
		return StringEquals(first.toLowerCase(), second.toLowerCase());
	}

	/**
	 * <p>
	 * StringStartsWith
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
	public static int StringStartsWith(String value, String prefix, int start) {
		int len = Math.min(prefix.length(), value.length());
		//System.out.println("StartsWith: " + start + ": " + value + " / " + prefix + ": "
		//        + value.substring(start, start + len) + " / " + prefix + " = "
		//        + StringEquals(value.substring(start, start + len), prefix));
		return StringEquals(value.substring(start, start + len), prefix);
	}

	/**
	 * <p>
	 * StringEndsWith
	 * </p>
	 * 
	 * @param value
	 *            a {@link java.lang.String} object.
	 * @param suffix
	 *            a {@link java.lang.String} object.
	 * @return a int.
	 */
	public static int StringEndsWith(String value, String suffix) {
		int len = Math.min(suffix.length(), value.length());
		String val1 = value.substring(value.length() - len);
		return StringEquals(val1, suffix);
	}

	/**
	 * <p>
	 * StringIsEmpty
	 * </p>
	 * 
	 * @param value
	 *            a {@link java.lang.String} object.
	 * @return a int.
	 */
	public static int StringIsEmpty(String value) {
		int len = value.length();
		if (len == 0) {
			return K;
		} else {
			return -len;
		}
	}

	/**
	 * <p>
	 * StringRegionMatches
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
	public static int StringRegionMatches(String value, boolean ignoreCase,
	        int thisStart, String string, int start, int length) {
		if (value == null || string == null)
			throw new NullPointerException();

		if (start < 0 || string.length() - start < length) {
			return -K;
		}

		if (thisStart < 0 || value.length() - thisStart < length) {
			return -K;
		}
		if (length <= 0) {
			return K;
		}

		String s1 = value;
		String s2 = string;
		if (ignoreCase) {
			s1 = s1.toLowerCase();
			s2 = s2.toLowerCase();
		}

		return StringEquals(s1.substring(thisStart, length + thisStart),
		                    s2.substring(start, length + start));
	}

	public static int StringRegionMatches(String value, int thisStart, String string,
	        int start, int length) {
		return StringRegionMatches(value, false, thisStart, string, start, length);
	}

	/**
	 * Replacement function for the Java instanceof instruction, which returns a
	 * distance integer
	 * 
	 * @param o
	 *            a {@link java.lang.Object} object.
	 * @param c
	 *            a {@link java.lang.Class} object.
	 * @return a int.
	 */
	public static int instanceOf(Object o, Class<?> c) {
		if (o == null)
			return FALSE;
		return c.isAssignableFrom(o.getClass()) ? TRUE : FALSE;
	}

	/**
	 * Replacement function for the Java IFNULL instruction, returning a
	 * distance integer
	 * 
	 * @param o
	 * @param opcode
	 * @param opcode
	 *            a int.
	 * @return a int.
	 */
	public static int isNull(Object o, int opcode) {
		if (opcode == Opcodes.IFNULL)
			return o == null ? TRUE : FALSE;
		else
			return o != null ? TRUE : FALSE;
	}

	/**
	 * <p>
	 * IOR
	 * </p>
	 * 
	 * @param a
	 *            a int.
	 * @param b
	 *            a int.
	 * @return a int.
	 */
	public static int IOR(int a, int b) {
		int ret = 0;
		if (a > 0 || b > 0) {
			// True

			ret = a;
			if (b > 0 && b < a)
				ret = b;
		} else {
			// False

			ret = a;
			if (b > a)
				ret = b;
		}

		return ret;
	}

	/**
	 * <p>
	 * IAND
	 * </p>
	 * 
	 * @param a
	 *            a int.
	 * @param b
	 *            a int.
	 * @return a int.
	 */
	public static int IAND(int a, int b) {
		return Math.min(a, b);
	}

	/**
	 * <p>
	 * IXOR
	 * </p>
	 * 
	 * @param a
	 *            a int.
	 * @param b
	 *            a int.
	 * @return a int.
	 */
	public static int IXOR(int a, int b) {
		int ret = 0;
		if (a > 0 && b <= 0) {
			// True
			ret = a;
		} else if (b > 0 && a <= 0) {
			ret = b;
		} else {
			// False
			ret = -Math.abs(a - b);
		}

		return ret;
	}

	/**
	 * <p>
	 * isEqual
	 * </p>
	 * 
	 * @param o1
	 *            a {@link java.lang.Object} object.
	 * @param o2
	 *            a {@link java.lang.Object} object.
	 * @param opcode
	 *            a int.
	 * @return a int.
	 */
	public static int isEqual(Object o1, Object o2, int opcode) {
		if (opcode == Opcodes.IF_ACMPEQ)
			return o1 == o2 ? K : -K;
		else
			return o1 != o2 ? K : -K;
	}

	private static Stack<Object> parametersObject = new Stack<Object>();
	private static Stack<Boolean> parametersBoolean = new Stack<Boolean>();
	private static Stack<Character> parametersChar = new Stack<Character>();
	private static Stack<Byte> parametersByte = new Stack<Byte>();
	private static Stack<Short> parametersShort = new Stack<Short>();
	private static Stack<Integer> parametersInteger = new Stack<Integer>();
	private static Stack<Float> parametersFloat = new Stack<Float>();
	private static Stack<Long> parametersLong = new Stack<Long>();
	private static Stack<Double> parametersDouble = new Stack<Double>();

	/**
	 * <p>
	 * popParameterBooleanFromInt
	 * </p>
	 * 
	 * @return a boolean.
	 */
	public static boolean popParameterBooleanFromInt() {
		int i = parametersInteger.pop();
		boolean result = i > 0;
		return result;
	}

	/**
	 * <p>
	 * popParameterIntFromBoolean
	 * </p>
	 * 
	 * @return a int.
	 */
	public static int popParameterIntFromBoolean() {
		boolean i = parametersBoolean.pop();
		if (i)
			return K;
		else
			return -K;
	}

	/**
	 * <p>
	 * popParameterBoolean
	 * </p>
	 * 
	 * @return a boolean.
	 */
	public static boolean popParameterBoolean() {
		return parametersBoolean.pop();
	}

	/**
	 * <p>
	 * popParameterChar
	 * </p>
	 * 
	 * @return a char.
	 */
	public static char popParameterChar() {
		return parametersChar.pop();
	}

	/**
	 * <p>
	 * popParameterByte
	 * </p>
	 * 
	 * @return a byte.
	 */
	public static byte popParameterByte() {
		return parametersByte.pop();
	}

	/**
	 * <p>
	 * popParameterShort
	 * </p>
	 * 
	 * @return a short.
	 */
	public static short popParameterShort() {
		return parametersShort.pop();
	}

	/**
	 * <p>
	 * popParameterInt
	 * </p>
	 * 
	 * @return a int.
	 */
	public static int popParameterInt() {
		return parametersInteger.pop();
	}

	/**
	 * <p>
	 * popParameterFloat
	 * </p>
	 * 
	 * @return a float.
	 */
	public static float popParameterFloat() {
		return parametersFloat.pop();
	}

	/**
	 * <p>
	 * popParameterLong
	 * </p>
	 * 
	 * @return a long.
	 */
	public static long popParameterLong() {
		return parametersLong.pop();
	}

	/**
	 * <p>
	 * popParameterDouble
	 * </p>
	 * 
	 * @return a double.
	 */
	public static double popParameterDouble() {
		return parametersDouble.pop();
	}

	/**
	 * <p>
	 * popParameterObject
	 * </p>
	 * 
	 * @return a {@link java.lang.Object} object.
	 */
	public static Object popParameterObject() {
		return parametersObject.pop();
	}

	/**
	 * <p>
	 * popParameter
	 * </p>
	 * 
	 * @param o
	 *            a {@link java.lang.Object} object.
	 * @return a {@link java.lang.Object} object.
	 */
	public static Object popParameter(Object o) {
		return parametersObject.pop();
	}

	/**
	 * <p>
	 * pushParameter
	 * </p>
	 * 
	 * @param o
	 *            a boolean.
	 */
	public static void pushParameter(boolean o) {
		parametersBoolean.push(o);
	}

	/**
	 * <p>
	 * pushParameter
	 * </p>
	 * 
	 * @param o
	 *            a char.
	 */
	public static void pushParameter(char o) {
		parametersChar.push(o);
	}

	/**
	 * <p>
	 * pushParameter
	 * </p>
	 * 
	 * @param o
	 *            a byte.
	 */
	public static void pushParameter(byte o) {
		parametersByte.push(o);
	}

	/**
	 * <p>
	 * pushParameter
	 * </p>
	 * 
	 * @param o
	 *            a short.
	 */
	public static void pushParameter(short o) {
		parametersShort.push(o);
	}

	/**
	 * <p>
	 * pushParameter
	 * </p>
	 * 
	 * @param o
	 *            a int.
	 */
	public static void pushParameter(int o) {
		parametersInteger.push(o);
	}

	/**
	 * <p>
	 * pushParameter
	 * </p>
	 * 
	 * @param o
	 *            a float.
	 */
	public static void pushParameter(float o) {
		parametersFloat.push(o);
	}

	/**
	 * <p>
	 * pushParameter
	 * </p>
	 * 
	 * @param o
	 *            a long.
	 */
	public static void pushParameter(long o) {
		parametersLong.push(o);
	}

	/**
	 * <p>
	 * pushParameter
	 * </p>
	 * 
	 * @param o
	 *            a double.
	 */
	public static void pushParameter(double o) {
		parametersDouble.push(o);
	}

	/**
	 * <p>
	 * pushParameter
	 * </p>
	 * 
	 * @param o
	 *            a {@link java.lang.Object} object.
	 */
	public static void pushParameter(Object o) {
		parametersObject.push(o);
	}
}
