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
package org.evosuite.symbolic.vm;

import java.lang.ref.WeakReference;

import org.objectweb.asm.Type;

/**
 * 
 * @author galeotti
 * 
 */
public class NonNullExpression implements ReferenceExpression {

	private final int instanceId;
	private final Type objectType;

	private WeakReference<Object> weakReference;
	private int concIdentityHashCode;
	private boolean isInitialized = false;
	
	public NonNullExpression(Type objectType, int instanceId) {
		this.objectType = objectType;
		this.instanceId = instanceId;

		weakReference = null;
		concIdentityHashCode = -1;
	}

	@Override
	public String toString() {
		return this.getClassName() + "$" + this.instanceId;
	}

	public void initializeReference(Object obj) {
		if (this.isInitialized) {
			throw new IllegalStateException("Reference already initialized!");
		}

		this.weakReference = new WeakReference<Object>(obj);
		this.concIdentityHashCode = System.identityHashCode(obj);
		this.isInitialized = true;
	}

	public boolean isInitialized() {
		return this.weakReference != null;
	}

	public Object getWeakConcreteObject() {
		if (!isInitialized())
			throw new IllegalStateException(
					"Object has to be initialized==true for this method to be invoked");
		return this.weakReference.get();
	}

	public int getConcIdentityHashCode() {
		if (!isInitialized())
			throw new IllegalStateException(
					"Object has to be initialized==true for this method to be invoked");
		return this.concIdentityHashCode;
	}

	public boolean isCollectable() {
		return this.isInitialized() && this.getWeakConcreteObject() == null;
	}

	public String getClassName() {
		return this.objectType.getClassName();
	}

	public boolean isString() {
		Type stringType = Type.getType(String.class);
		return this.objectType.equals(stringType);
	}
}
