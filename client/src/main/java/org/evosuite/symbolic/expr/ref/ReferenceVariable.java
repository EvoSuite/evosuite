package org.evosuite.symbolic.expr.ref;

import java.util.Collections;
import java.util.Set;

import org.evosuite.symbolic.expr.AbstractExpression;
import org.evosuite.symbolic.expr.ExpressionVisitor;
import org.evosuite.symbolic.expr.Variable;
import org.objectweb.asm.Type;

/**
 * Represents a symbolic reference (for example, a pointer that we have declared
 * as symbolic) at the test case level. We assume these references are not
 * created during the SUT.
 * 
 * @author galeotti
 *
 */
public final class ReferenceVariable extends AbstractExpression<Object>
		implements ReferenceExpression, Variable<Object> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5785895234153444210L;

	/**
	 * The name of this variable
	 */
	private final String name;

	public ReferenceVariable(Type objectType, int instanceId, String name, Object concreteValue) {
		super(concreteValue, 1, true);
		this.name = name;
	}

	@Override
	public Set<Variable<?>> getVariables() {
		return Collections.singleton(this);
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Object getMinValue() {
		return this.getConcreteValue();
	}

	@Override
	public Object getMaxValue() {
		return this.getConcreteValue();
	}

	@Override
	public Object getWeakConcreteObject() {
		return this.getConcreteValue();
	}

	@Override
	public <K, V> K accept(ExpressionVisitor<K, V> v, V arg) {
		return v.visit(this, arg);
	}
	
}
