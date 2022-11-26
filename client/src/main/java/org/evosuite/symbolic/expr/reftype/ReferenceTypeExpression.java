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
package org.evosuite.symbolic.expr.reftype;

import org.objectweb.asm.Type;
import org.evosuite.symbolic.expr.AbstractExpression;

/**
 * This is the super class of all symbolic reference types (classes).
 *
 * @author Ignacio Lebrero
 */
public abstract class ReferenceTypeExpression extends AbstractExpression<Type> implements ReferenceTypeValue {

    private static final long serialVersionUID = 4684495307141703121L;

    /**
     * referenceTypeId does not change during the lifetime of the reference
     */
    private final int referenceTypeId;

    /**
     * This is the result of applying System.identityHashCode to the concrete class.
     */
    private int concIdentityHashCode;

    public ReferenceTypeExpression(Type concreteClass, int size, boolean containsSymbolicVariable, int referenceTypeId) {
        super(concreteClass, size, containsSymbolicVariable);

        this.referenceTypeId = referenceTypeId;
        this.concIdentityHashCode = System.identityHashCode(concreteClass);
    }

    public int getConcIdentityHashCode() {
        return this.concIdentityHashCode;
    }

    public int getReferenceTypeId() {
        return referenceTypeId;
    }
}
