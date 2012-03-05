/**
 * 
 */
package de.unisb.cs.st.evosuite.symbolic.expr;

import de.unisb.cs.st.evosuite.Properties;
import de.unisb.cs.st.evosuite.symbolic.ConstraintTooLongException;

/**
 * @author krusev
 *
 */
public class StringToIntCast extends IntegerExpression implements Cast<String> {

	private static final long serialVersionUID = 2214987345674527740L;

	protected Long concValue;
	
	protected Expression<String> expr;

	public StringToIntCast(Expression<String> _expr, Long _concValue) {
		this.expr = _expr;
		this.concValue = _concValue;
		if (getSize() > Properties.DSE_CONSTRAINT_LENGTH)
			throw new ConstraintTooLongException();
	}

	@Override
	public Object getConcreteValue() {
		return concValue;
	}

	@Override
	public Expression<String> getConcreteObject() {
		return expr;
	}

	@Override
	public String toString() {
		return "((INT)"+expr+")";
	}

	@Override
	public boolean equals(Object obj) {
		if(obj==this)
		{
			return true;
		}
		if(obj instanceof StringToIntCast)
		{
			StringToIntCast other=(StringToIntCast) obj;
			return this.expr.equals(other.expr) 
				&& this.concValue == other.concValue;
		}

		return false;
	}

	protected int size=0;
	@Override
	public int getSize() {
		if(size == 0)
		{
			size=1 + expr.getSize();
		}
		return size;
	}
	
	@Override
	public Long execute() {
		return Long.parseLong(((String)expr.execute()));
	}



}
