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
public class StringBuilderExpression extends StringExpression {


	private static final long serialVersionUID = 5944852577642617563L;

	protected boolean undef_func;
	protected Expression<String> expr;

	public StringBuilderExpression(Expression<String> _expr) {
		this.expr = _expr;
		this.undef_func = false;
		if (getSize() > Properties.DSE_CONSTRAINT_LENGTH)
			throw new ConstraintTooLongException();
	}

	public Expression<String> getExpr() {
		return expr;
	}

	public void setExpr(Expression<String> _expr) {
		expr = _expr;
	}
	
	public void append(Expression<String> _expr) {
		if (expr == null)
			expr = _expr;
		else {
			if (_expr instanceof StringConstant) {
				if (expr instanceof StringConstant) {
					expr = new StringConstant(
							((String)expr.getConcreteValue()) 
						+	((String)_expr.getConcreteValue()));
					return;
				} else if (expr instanceof StringBinaryExpression) {
					StringBinaryExpression sBin = (StringBinaryExpression)expr;
					if (sBin.getRightOperand() instanceof StringConstant) {
						StringConstant strConst = new StringConstant(
										sBin.getRightOperand().getConcreteValue()
									+	((String)_expr.getConcreteValue()));
						expr = new StringBinaryExpression(sBin.getLeftOperand(),
											Operator.APPEND, strConst, 
											sBin.getConcreteValue()
												+((String)_expr.getConcreteValue()));
						return;
					}
				}
					
			}
			
			expr = new StringBinaryExpression(expr,
    			Operator.APPEND, 
    			_expr, 
    			((String)expr.getConcreteValue()) + ((String)_expr.getConcreteValue()));
		}
	}
	
	public boolean has_undef_func() {
		return undef_func;
	}

	public void set_undef_func() {
		undef_func = true;
	}

	@Override
	public String toString() {
		return "StringBuilder(" + expr.toString() + ")";
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj instanceof StringBuilderExpression) {
			StringBuilderExpression other = (StringBuilderExpression) obj;
			return this.expr.equals(other.expr)
						&& this.getSize() == other.getSize(); 
		}

		return false;
	}

	protected int size = 0;

	@Override
	public int getSize() {
		int expr_size = 0;
		if (expr!=null)
			expr_size = expr.getSize();
		if (size == 0) {
			size = 1 + expr_size;
		}
		return size;
	}
	
	@Override
	public String getConcreteValue() {
		
		return (String) expr.getConcreteValue();
	}

	@Override
	public String execute() {
		return (String) expr.execute();
	}

}
