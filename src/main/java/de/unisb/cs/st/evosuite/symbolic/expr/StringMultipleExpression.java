/**
 * 
 */
package de.unisb.cs.st.evosuite.symbolic.expr;

import java.util.ArrayList;

/**
 * @author krusev
 *
 */
public class StringMultipleExpression extends StringExpression implements
BinaryExpression<String>{

	private static final long serialVersionUID = 7172041118401792672L;

	protected String concretValue;

	protected Operator op;

	protected Expression<String> left;
	protected Expression<?> right;
	protected ArrayList<Expression<?>> other_v;

	public StringMultipleExpression(Expression<String> _left, Operator _op,
	        Expression<?> _right, ArrayList<Expression<?>> _other, String con) {
		this.concretValue = con;
		this.left = _left;
		this.right = _right;
		this.op = _op;
		this.other_v = _other;
	}

	/**
	 * @return the other
	 */
	public ArrayList<Expression<?>> getOther() {
		return other_v;
	}

	@Override
	public String getConcreteValue() {
		return concretValue;
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
		
		return "(" + left + op.toString() + right + str_other_v + ")";
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

}
