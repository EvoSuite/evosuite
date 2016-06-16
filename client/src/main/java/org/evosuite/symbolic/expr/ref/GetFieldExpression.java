package org.evosuite.symbolic.expr.ref;

import java.util.Set;

import org.evosuite.symbolic.expr.AbstractExpression;
import org.evosuite.symbolic.expr.ExpressionVisitor;
import org.evosuite.symbolic.expr.Variable;

public final class GetFieldExpression extends AbstractExpression<Object> implements ReferenceExpression {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4517401722564017247L;

	private final ReferenceExpression receiverExpr;

	private final String fieldName;

	public GetFieldExpression(Object concreteValue, ReferenceExpression receiverExpr, String fieldName) {
		super(concreteValue, 1 + receiverExpr.getSize(), receiverExpr.containsSymbolicVariable());
		this.receiverExpr = receiverExpr;
		this.fieldName = fieldName;
	}

	@Override
	public Set<Variable<?>> getVariables() {
		return this.receiverExpr.getVariables();
	}

	@Override
	public <K, V> K accept(ExpressionVisitor<K, V> v, V arg) {
		return v.visit(this, arg);
	}

	@Override
	public Object getWeakConcreteObject() {
		return this.getConcreteValue();
	}

	public ReferenceExpression getReceiverExpr() {
		return receiverExpr;
	}

	public String getFieldName() {
		return fieldName;
	}

}
