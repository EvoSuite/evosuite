package org.evosuite.symbolic.expr;

import org.evosuite.Properties;
import org.evosuite.symbolic.ConstraintTooLongException;
import org.evosuite.symbolic.DSEStats;
import org.evosuite.symbolic.expr.bv.IntegerConstant;
import org.evosuite.symbolic.expr.bv.StringBinaryComparison;
import org.evosuite.symbolic.expr.bv.StringComparison;
import org.evosuite.symbolic.expr.bv.StringMultipleComparison;
import org.evosuite.symbolic.search.RegexDistance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class StringConstraint extends Constraint<String> {

	static Logger log = LoggerFactory.getLogger(StringConstraint.class);

	public StringConstraint(StringComparison left, Comparator comp,
			IntegerConstant right) {
		super();
		this.left = left;
		this.cmp = comp;
		this.right = right;
		if (getSize() > Properties.DSE_CONSTRAINT_LENGTH) {
			DSEStats.reportConstraintTooLong(getSize());
			throw new ConstraintTooLongException(getSize());
		}
	}

	private final StringComparison left;
	private final Comparator cmp;
	private final IntegerConstant right;

	/**
	 * 
	 */
	private static final long serialVersionUID = -3187023627540040535L;

	@Override
	public Comparator getComparator() {
		return cmp;
	}

	@Override
	public Expression<?> getLeftOperand() {
		return left;
	}

	@Override
	public Expression<?> getRightOperand() {
		return right;
	}

	@Override
	public String toString() {
		return left + cmp.toString() + right;
	}

	@Override
	public Constraint<String> negate() {
		return new StringConstraint(left, cmp.not(), right);
	}
	
	public double getStringDist() {
		Expression<?> exprLeft = this.getLeftOperand();
		Comparator cmpr = this.getComparator();
		double distance = 0.0;

		if (exprLeft instanceof StringBinaryComparison) {
			StringBinaryComparison scTarget = (StringBinaryComparison) exprLeft;
			distance = getStringDistance(scTarget);
			log.debug("Calculating distance of constraint " + this);
		} else if (exprLeft instanceof StringMultipleComparison) {
			StringMultipleComparison scTarget = (StringMultipleComparison) exprLeft;
			distance = getStringDistance(scTarget);
			log.debug("Calculating distance of constraint " + this);
		} else {
			assert (false) : "Invalid string comparison";
		}
		assert (this.right.concreteValue.intValue()==0);
		if (cmpr == Comparator.NE) { 
			return distance; 
		} else {
			// if we don't want to satisfy return 0
			// if not satisfied Long.MAX_VALUE else
			return distance > 0 ? 0.0 : Double.MAX_VALUE;
		}
	}
	
	private static double getStringDistance(StringBinaryComparison comparison) {
		try {
			String first = comparison.getLeftOperand().execute();
			String second = (String) comparison.getRightOperand().execute();

			switch (comparison.getOperator()) {
			case EQUALSIGNORECASE:
				return StrEqualsIgnoreCase(first, second);
			case EQUALS:
				log.debug("Edit distance between " + first + " and " + second + " is: "
				        + StrEquals(first, second));
				return StrEquals(first, second);
			case ENDSWITH:
				return StrEndsWith(first, second);
			case CONTAINS:
				return StrContains(first, second);
			case PATTERNMATCHES:
				return RegexMatches(second, first);
			case APACHE_ORO_PATTERN_MATCHES:
				return RegexMatches(second, first);				
				
			default:
				log.warn("StringComparison: unimplemented operator!"
				        + comparison.getOperator());
				return Double.MAX_VALUE;
			}
		} catch (Exception e) {
			return Double.MAX_VALUE;
		}
	}

	private static double StrContains(String val, CharSequence subStr) {
		int val_length = val.length();
		int subStr_length = subStr.length();
		double min_dist = Double.MAX_VALUE;
		String sub = subStr.toString();

		if (subStr_length > val_length) {
			return avmDistance(val, sub);
			// return editDistance(val, sub);
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

	private static double StrEndsWith(String value, String suffix) {
		int len = Math.min(suffix.length(), value.length());
		String val1 = value.substring(value.length() - len);
		return StrEquals(val1, suffix);
	}
	
	private static double avmDistance(String s, String t) {
		double distance = Math.abs(s.length() - t.length());
		int max = Math.min(s.length(), t.length());
		for(int i = 0; i < max; i++) {
			distance += normalize(Math.abs(s.charAt(i) - t.charAt(i)));
		}
		return distance;
	}
	
	private static double editDistance(String s, String t) {
		int n = s.length(); // length of s
		int m = t.length(); // length of t

		if (n == 0) {
			return m;
		} else if (m == 0) {
			return n;
		}

		double p[] = new double[n + 1]; // 'previous' cost array, horizontally
		double d[] = new double[n + 1]; // cost array, horizontally
		double _d[]; // placeholder to assist in swapping p and d

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
				// cost = s.charAt(i - 1) == t_j ? 0 : 1;
				cost = normalize(Math.abs(s.charAt(i - 1) - t_j));
				// minimum of cell to the left+1, to the top+1, diagonally left
				// and up +cost
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
	
	private static double getStringDistance(StringMultipleComparison comparison) {
		try {
			String first = comparison.getLeftOperand().execute();
			String second = (String) comparison.getRightOperand().execute();

			switch (comparison.getOperator()) {
			case STARTSWITH:
				long start = (Long) comparison.getOther().get(0).execute();
				return StrStartsWith(first, second, (int) start);
			case REGIONMATCHES:
				long frstStart = (Long) comparison.getOther().get(0).execute();
				long secStart = (Long) comparison.getOther().get(1).execute();
				long length = (Long) comparison.getOther().get(2).execute();
				long ignoreCase = (Long) comparison.getOther().get(3).execute();

				return StrRegionMatches(first, (int) frstStart, second,
				                                          (int) secStart, (int) length,
				                                          ignoreCase != 0);
			default:
				log.warn("StringComparison: unimplemented operator!"
				        + comparison.getOperator());
				return Double.MAX_VALUE;
			}
		} catch (Exception e) {
			return Double.MAX_VALUE;
		}
	}
	
	private static double StrRegionMatches(String value, int thisStart, String string,
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
	
	private static double StrEqualsIgnoreCase(String first, String second) {
		return StrEquals(first.toLowerCase(), second.toLowerCase());
	}
	
	private static double StrEquals(String first, Object second) {
		if (first.equals(second))
			return 0; // Identical
		else {
			return avmDistance(first, second.toString());
			// return editDistance(first, second.toString());
		}
	}
	
	private static double StrStartsWith(String value, String prefix, int start) {
		int len = Math.min(prefix.length(), value.length());
		int end = (start + len > value.length()) ? value.length() : start + len;
		return StrEquals(value.substring(start, end), prefix);
	}
	
	private static double RegexMatches(String val, String regex) {
		return RegexDistance.getDistance(val, regex);
	}


}
