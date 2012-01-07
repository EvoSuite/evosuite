/**
 * 
 */
package de.unisb.cs.st.evosuite.symbolic.expr;

import gov.nasa.jpf.JPF;

import java.util.ArrayList;
import java.util.logging.Logger;

import de.unisb.cs.st.evosuite.symbolic.search.DistanceEstimator;

/**
 * @author krusev
 *
 */
public class StringMultipleComparison extends StringComparison implements
BinaryExpression<String>{

	private static final long serialVersionUID = -3844726361666119758L;

	static Logger log = JPF.getLogger("de.unisb.cs.st.evosuite.symbolic.expr.StringMultipleComparison");
	
	protected ArrayList<Expression<?>> other_v;

	public StringMultipleComparison(Expression<String> _left, Operator _op,
	        Expression<?> _right, ArrayList<Expression<?>> _other, Long con) {
		super(_left, _op, _right, con);
		this.other_v = _other;
	}

	/**
	 * @return the other
	 */
	public ArrayList<Expression<?>> getOther() {
		return other_v;
	}

	@Override
	public Long getConcreteValue() {
		return conVal;
	}

	@Override
	public Operator getOperator() {
		return op;
	}

	@Override
	public Expression<String> getLeftOperand() {
		return left;
	}

	@Override
	public Expression<?> getRightOperand() {
		return right;
	}

	@Override
	public String toString() {
		String str_other_v = "";
		for (int i = 0; i < this.other_v.size(); i++) {
			str_other_v += " " + this.other_v.get(i).toString();
		}
		
		return "(" + left + op.toString() + (right==null ? "" : right) + str_other_v + ")";
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj instanceof StringMultipleExpression) {
			StringMultipleExpression other = (StringMultipleExpression) obj;
			
			boolean other_v_eq = true;
			
			if (other.other_v.size() == this.other_v.size()) {
				for (int i = 0; i < other.other_v.size(); i++) {
					if ( !( other.other_v.get(i).equals(this.other_v.get(i)) ) ) {
						other_v_eq = false;
					}
				}
			} else {
				other_v_eq = false;
			}
			
			return this.op.equals(other.op) && this.getSize() == other.getSize()
			        && this.left.equals(other.left) && this.right.equals(other.right)
			        && other_v_eq;
		}

		return false;
	}

	protected int size = 0;

	@Override
	public int getSize() {
		//TODO fix this
		return -1;
//		if (size == 0) {
//			size = 1 + getLeftOperand().getSize() + getRightOperand().getSize();
//		}
//		return size;
	}
	
	

	@Override
	public Long execute() {
		String first = (String)left.execute();
		String second = (String)right.execute();
		
		
		switch (op) {
		case STARTSWITH:
			long start = (Long) other_v.get(0).execute();

			return (long)DistanceEstimator.StrStartsWith(first, second, (int) start);
		case REGIONMATCHES:
			long frstStart = (Long) other_v.get(0).execute();			
			long secStart = (Long) other_v.get(1).execute();
			long length = (Long) other_v.get(2).execute();
			long ignoreCase = (Long) other_v.get(3).execute();

			return (long)DistanceEstimator.StrRegionMatches(first, (int) frstStart, 
					second, (int) secStart, (int) length, ignoreCase != 0);
		default:
			log.warning("StringMultipleComparison: unimplemented operator!");
			return null;
		}		
	}

}
