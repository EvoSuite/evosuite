package de.unisb.cs.st.evosuite.symbolic.search;
import gov.nasa.jpf.JPF;

import java.util.Collection;
import java.util.logging.Logger;

import de.unisb.cs.st.evosuite.symbolic.expr.Comparator;
import de.unisb.cs.st.evosuite.symbolic.expr.Constraint;
import de.unisb.cs.st.evosuite.symbolic.expr.Expression;
import de.unisb.cs.st.evosuite.symbolic.expr.StringComparison;

/**
 * @author krusev
 *
 */
public abstract class DistanceEstimator {
	
	static Logger log = JPF.getLogger("de.unisb.cs.st.evosuite.symbolic.search.DistanceEstimator");

	/**
	 * @param sc
	 * @return
	 */
	public static double getStringDistance(StringComparison sc) {

		long result = sc.execute();
//		log.warning("comparison: " + sc + " distance: " + result);		
		return result;
	}

	/**
	 * @param constraints
	 * @return true if all but the last constraint (which is the target) are reachable
	 */
	public static boolean areReachable(Collection<Constraint<?>> constraints) {
		boolean result = true;

		for (Constraint<?> c : constraints) {
			Expression<?> expr = c.getLeftOperand();
			if(expr instanceof StringComparison) {
				StringComparison sc = (StringComparison) expr;
				Comparator op = c.getComparator();
				long dis = sc.execute();
				if (op.equals(Comparator.NE)) {
					//we want to satisfy
					result = (dis >= 0);
				}
				if (op.equals(Comparator.EQ)) {
					//we DON'T want to satisfy
					result = (dis < 0);
				}
			}
		}

		return result;
	}
	
	
	public static int min(int a, int b, int c) {
		if (a < b)
			return Math.min(a, c);
		else
			return Math.min(b, c);
	}

	
	public static int editDistance(String s, String t) {
		int d[][]; // matrix
		int n, m; // length of s and t
		int i, j; // iterates through s and t
		char s_i, t_j; // i-th and j-th character of s and t

		//If one of the strings is empty return the length of the other one
		n = s.length();
		m = t.length();
		if (n == 0) {
			return m;
		}
		if (m == 0) {
			return n;
		}
		
		//Initialize the computation matrix
		d = new int[n + 1][m + 1];
		for (i = 0; i <= n; i++) {
			d[i][0] = i;
		}
		for (j = 0; j <= m; j++) {
			d[0][j] = j;
		}

		//Compute the distance
		for (i = 1; i <= n; i++) {
			s_i = s.charAt(i - 1);
			for (j = 1; j <= m; j++) {
				t_j = t.charAt(j - 1);
				if (s_i == t_j) {
					d[i][j] = d[i-1][j-1];
				} else {
					d[i][j] = min(	d[i - 1][j] + 1, 		// deletion
									d[i][j - 1] + 1,  		// insertion
									d[i - 1][j - 1] + 1);	// substitution
				}
			}
		}
		
		//If the length is different return same distance but negated
		//return (m!=n) ? -d[n][m] : d[n][m];
		return d[n][m];
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
			log.warning("are we here1");
			return length - string.length() + start;//TODO test	
		}

		if (thisStart < 0 || value.length() - thisStart < length) {
			log.warning("are we here2");
			return length - value.length() + thisStart;//TODO
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

	
	//TODO ask Gordon what's with the K and why editDistance() is giving 0 when equal 
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
