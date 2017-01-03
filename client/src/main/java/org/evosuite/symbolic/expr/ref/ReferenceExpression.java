/**
 * Copyright (C) 2010-2016 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3.0 of the License, or
 * (at your option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.symbolic.expr.ref;

import org.evosuite.symbolic.expr.AbstractExpression;
import org.evosuite.symbolic.expr.SymbolicValue;
import org.objectweb.asm.Type;

/**
 * This is the super class of all symbolic references. A reference expression
 * can be initialized using a concrete reference (can be null). Once it is
 * initialized, we can get the concrete identity hash code and the concrete
 * object
 * 
 * @author galeotti
 *
 */
public abstract class ReferenceExpression extends AbstractExpression<Object> implements SymbolicValue {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7925438069540850557L;

	/**
	 * InstanceId does not change during the lifetime of the reference
	 */
	private final int instanceId;

	/**
	 * The type of this reference does not change during the lifetime
	 */
	private final Type objectType;

	/**
	 * This is the result of applying System.identityHashCode to the concrete
	 * value stored in this reference
	 */
	private int concIdentityHashCode;

	/**
	 * This flag shows if the current Reference was initialized (i.e. a concrete
	 * value was assigned to this symbolic reference)
	 */
	private boolean isInitialized = false;

	/**
	 * Creates a new Reference expression of type <code>objectType</code> and
	 * with instance identifier <code>instanceId</code>
	 * 
	 * @param objectType
	 *            the type of the new reference
	 * @param instanceId
	 *            the instance identifier for this new reference
	 */
	public ReferenceExpression(Type objectType, int instanceId, int expressionSize, boolean containsSymbolicVariable) {
		super(null, expressionSize, containsSymbolicVariable);

		this.objectType = objectType;
		this.instanceId = instanceId;

		// the reference is not initialized
		this.isInitialized = false; // it is not initialized
		this.concIdentityHashCode = -1; // no hash code
	}

	/**
	 * Initializes the current reference using the concrete object
	 * 
	 * @param conc_object
	 */
	public void initializeReference(Object conc_object) {
		if (this.isInitialized) {
			throw new IllegalStateException("Reference already initialized!");
		}

		this.concreteValue = conc_object;
		this.concIdentityHashCode = System.identityHashCode(conc_object);
		this.isInitialized = true;
	}

	/**
	 * Prints the reference Id
	 */
	@Override
	public String toString() {
		return this.getClassName() + "$" + this.instanceId;
	}

	/**
	 * Returns true iff the the reference is initialised
	 * 
	 * @return
	 */
	public boolean isInitialized() {
		return this.isInitialized;
	}

	/**
	 * Returns the identity hash
	 * 
	 * @return
	 */
	public int getConcIdentityHashCode() {
		if (!isInitialized())
			throw new IllegalStateException("Object has to be initialized==true for this method to be invoked");
		return this.concIdentityHashCode;
	}

	/**
	 * Returns the type of the class
	 * 
	 * @return
	 */
	public String getClassName() {
		return this.objectType.getClassName();
	}

	/**
	 * Returns true iff the reference is of type <code>String</code>
	 * 
	 * @return
	 */
	public boolean isString() {
		Type stringType = Type.getType(String.class);
		return this.objectType.equals(stringType);
	}

}
