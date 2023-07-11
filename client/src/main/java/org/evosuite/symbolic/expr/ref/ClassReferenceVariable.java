/*
 * Copyright (C) 2010-2018 Gordon Fraser, Andrea Arcuri and EvoSuite
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

import org.evosuite.symbolic.expr.ExpressionVisitor;
import org.objectweb.asm.Type;

/**
 * Represents a non array reference variable.
 *
 * @author Ignacio Lebrero
 */
public class ClassReferenceVariable extends ReferenceVariable {

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
	public ClassReferenceVariable(Type objectType, int instanceId, String name, Object concreteValue) {
		super(objectType, instanceId, name, concreteValue);
	}

	@Override
	public <K, V> K accept(ExpressionVisitor<K, V> v, V arg) {
		return v.visit(this, arg);
	}

}