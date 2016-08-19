package org.evosuite.symbolic.expr.ref;

import java.util.Collections;
import java.util.Set;

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
public final class ReferenceVariable extends ReferenceExpression implements Variable<Object> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5785895234153444210L;

	/**
	 * The name of this variable. The name cannot be modified once created.
	 */
	private final String name;

	/**
	 * Creates a new reference variable using the type of the reference, an
	 * instance id, the name of the variable and the concrete object reference.
	 * The resulting variable is initialized.
	 * 
	 * @param objectType
	 * @param instanceId
	 * @param name
	 * @param concreteValue
	 */
	public ReferenceVariable(Type objectType, int instanceId, String name, Object concreteValue) {
		super(objectType, instanceId,1,true);
		this.name = name;
		this.initializeReference(concreteValue);
	}

	/**
	 * Returns a the set {this}
	 * 
	 * @return
	 */
	@Override
	public Set<Variable<?>> getVariables() {
		return Collections.singleton(this);
	}

	/**
	 * Returns the name of the variable
	 * 
	 * @return
	 */
	@Override
	public String getName() {
		return name;
	}

	/**
	 * Returns the concrete object
	 * 
	 * @return
	 */
	@Override
	public Object getMinValue() {
		return this.getConcreteValue();
	}

	/**
	 * Returns the concrete object
	 * 
	 * @return
	 */
	@Override
	public Object getMaxValue() {
		return this.getConcreteValue();
	}

	@Override
	public <K, V> K accept(ExpressionVisitor<K, V> v, V arg) {
		return v.visit(this, arg);
	}

}
