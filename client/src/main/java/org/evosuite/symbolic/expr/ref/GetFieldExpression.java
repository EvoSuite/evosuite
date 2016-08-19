package org.evosuite.symbolic.expr.ref;

import java.util.Set;

import org.evosuite.symbolic.expr.ExpressionVisitor;
import org.evosuite.symbolic.expr.Variable;
import org.objectweb.asm.Type;

public final class GetFieldExpression extends ReferenceExpression {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4517401722564017247L;

	private final ReferenceExpression receiverExpr;

	private final String fieldName;

	/**
	 * Creates a symbolic expression of the form "expr.F" where expr is the
	 * <code>ReferenceExpression</code>, F is the string <code>fieldName</code>
	 * and the concrete value of the symbolic expression "expr.F" is
	 * <code>concreteValue</code>
	 * 
	 * @param receiverExpr
	 *            the symbolic expression of the receiver object
	 * @param fieldName
	 *            the field name
	 * @param concreteValue
	 *            the concrete object for the symbolic expression expr.F
	 */
	public GetFieldExpression(Type objectType, int instanceId, ReferenceExpression receiverExpr, String fieldName,
			Object concreteValue) {
		super(objectType, instanceId, 1 + receiverExpr.getSize(), receiverExpr.containsSymbolicVariable());
		this.receiverExpr = receiverExpr;
		this.fieldName = fieldName;
		this.initializeReference(concreteValue);
	}

	/**
	 * Returns the set of all the variables in the receiver expression (the expr
	 * in expr.F)
	 * 
	 * @return
	 */
	@Override
	public Set<Variable<?>> getVariables() {
		return this.receiverExpr.getVariables();
	}

	@Override
	public <K, V> K accept(ExpressionVisitor<K, V> v, V arg) {
		return v.visit(this, arg);
	}

	/**
	 * Returns the receiver expression (the expr in expr.F)
	 * 
	 * @return
	 */
	public ReferenceExpression getReceiverExpr() {
		return receiverExpr;
	}

	/**
	 * Returns the field name (the F in expr.F)
	 * 
	 * @return
	 */
	public String getFieldName() {
		return fieldName;
	}

}
