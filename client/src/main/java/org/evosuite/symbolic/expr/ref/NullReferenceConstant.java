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

import static org.evosuite.symbolic.vm.heap.SymbolicHeap.NULL_INSTANCE_ID;

/**
 * This class represents a reference to Null.
 *
 * @author Ignacio Lebrero
 */
public final class NullReferenceConstant extends ReferenceConstant {

    private static final long serialVersionUID = 8675423326479140020L;

	/**
	 * There should be only one instance of this object
	 */
    private static NullReferenceConstant instance;

    public synchronized static NullReferenceConstant getInstance() {
    	if (instance == null) {
    		instance = new NullReferenceConstant();
		}

    	return instance;
	}

	@Override
	public <K, V> K accept(ExpressionVisitor<K, V> v, V arg) {
		return v.visit(this, arg);
	}

    private NullReferenceConstant() {
        super(Type.getType(Object.class), NULL_INSTANCE_ID);
        initializeReference(null);
    }
}