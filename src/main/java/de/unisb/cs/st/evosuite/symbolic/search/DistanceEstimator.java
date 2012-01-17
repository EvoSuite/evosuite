package de.unisb.cs.st.evosuite.symbolic.search;
import gov.nasa.jpf.JPF;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.logging.Logger;

import de.unisb.cs.st.evosuite.symbolic.expr.BinaryExpression;
import de.unisb.cs.st.evosuite.symbolic.expr.Cast;
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
import de.unisb.cs.st.evosuite.symbolic.expr.StringMultipleComparison;
import de.unisb.cs.st.evosuite.symbolic.expr.StringUnaryExpression;
import de.unisb.cs.st.evosuite.symbolic.expr.UnaryExpression;
import de.unisb.cs.st.evosuite.symbolic.expr.Variable;

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
		
		try {
			for (Constraint<?> c : constraints) {
				if (isStrConstraint(c)) {
					double strD = (double)getStrDist(c);
					result += strD/(1.0 + strD);//Long.MAX_VALUE;
				} else if (isLongConstraint(c)) {
					double intD = (double)getIntegerDist(c);
					result += intD/(1.0 + intD);//Long.MAX_VALUE;
				} else if (isRealConstraint(c)) {
//					log.warning("DistanceEstimator.getDistance(): " +
//							"got a real constraint: " + c);
					
					double realD = getRealDist(c);
//					log.warning("we are here " + realD);
					result += realD/(1.0 + realD);//Double.MAX_VALUE;
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
		double left = ExpressionHelper.getDoubleResult(target.getLeftOperand());
		double right = ExpressionHelper.getDoubleResult(target.getRightOperand());

		
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

	//TODO delete
	public static long getStringDist(Constraint<?> target) {
		
		Expression<?> exprLeft = target.getLeftOperand();
		Comparator cmpr = target.getComparator();
		Expression<?> exprRight = target.getRightOperand();
		
		int diffConst = 1;
		
		//check if we have a string comparison
		if (	exprLeft instanceof StringComparison 
			 &&	exprRight instanceof IntegerConstant ) {
			StringComparison scTarget = (StringComparison) exprLeft;
			
			if (((IntegerConstant)exprRight).getConcreteValue() == 0 ) {
				
				//check whether we want to satisfy the condition
				if (cmpr == Comparator.NE) {
					return scTarget.execute();
				} else {
					//if we don't want to satisfy return 0 if not satisfied 1 else
					return scTarget.execute() > 0 ? 0 : diffConst;
				}
			} else {
				log.warning("getStringDist: StringComparison compared to non zero");
				return Long.MAX_VALUE;
			}
		} else { 
			//Since we have only String vars here the only other possibility is we have 
			//		some int returning function
			long left = ExpressionHelper.getLongResult(exprLeft);
			long right = ExpressionHelper.getLongResult(exprRight);
			
			
			//TODO fix diffConst as in intDist and test
			switch (cmpr) {
			case EQ:
				return Math.abs(left-right);
			case NE:
							
				return (left-right) != 0 ? 0 : diffConst;
			case LT:
				
				return left-right < 0 ? 0 : diffConst;
			case LE:
				
				return left-right <= 0 ? 0 : diffConst;
			case GT:
				
				return left-right > 0 ? 0 : diffConst;
			case GE:
				
				return left-right >= 0 ? 0 : diffConst;
				
			default:
				log.warning("getStringDist: unimplemented comparator");
				return Long.MAX_VALUE;
			}
		}
	}


	/**
	 * @param constraints
	 * @return true if all but the last constraint (which is the target) are reachable
	 */
	public static boolean areReachableStr(Collection<Constraint<?>> constraints) {
		
		for (Constraint<?> c : constraints) {
			if (getStringDist(c) > 0){
				return false;
			}		
		}
		return true;
	}

	//TODO delete
	public static boolean exprContainsVar(Expression<?> expr,
			Variable<?> var) {
		boolean res = false;
		if (expr.equals(var)) {
			res = true;
		} else if (expr instanceof StringMultipleComparison){
			StringMultipleComparison smc = (StringMultipleComparison) expr;
			res = res || exprContainsVar(smc.getLeftOperand(), var);
			res = res || exprContainsVar(smc.getRightOperand(), var);
			ArrayList<Expression<?>> ar_l_ex = smc.getOther();
			Iterator<Expression<?>> itr = ar_l_ex.iterator();
		    while (itr.hasNext()) {
		    	Expression<?> element = itr.next();
		    	res = res || exprContainsVar(element, var);
		    }
		} else if (expr instanceof StringComparison){
			StringComparison sc = (StringComparison) expr;
			res = res || exprContainsVar(sc.getLeftOperand(), var);
			res = res || exprContainsVar(sc.getRightOperand(), var);
		} else if (expr instanceof BinaryExpression<?>) {
			BinaryExpression<?> bin = (BinaryExpression<?>) expr;
			res = res || exprContainsVar(bin.getLeftOperand(), var);
			res = res || exprContainsVar(bin.getRightOperand(), var);
		} else if (expr instanceof UnaryExpression<?>) {
			UnaryExpression<?> un = (UnaryExpression<?>) expr;
			res = res || exprContainsVar(un.getOperand(), var);
		} else if (expr instanceof Cast<?>) {
			Cast<?> cst = (Cast<?>) expr;
			res = res || exprContainsVar(cst.getConcreteObject(), var);
		} else if (expr instanceof Constraint<?>) {
			// ignore

		}
	    return res;
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
			return length - string.length() + start;//TODO test	
		}

		if (thisStart < 0 || value.length() - thisStart < length) {
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
