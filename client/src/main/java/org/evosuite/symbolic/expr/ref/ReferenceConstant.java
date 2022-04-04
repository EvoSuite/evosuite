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
import org.evosuite.symbolic.expr.Variable;
import org.objectweb.asm.Type;

import java.util.Collections;
import java.util.Set;

/**
 * This class represents a reference that is not symbolic (e.g. a new Object()
 * somewhere during the execution of the code). After the NEW operation, the
 * concrete reference cannot be accessed until the <init> method finishes.
 * Therefore, we have to initialize the <code>ReferenceConstant</code> after the
 * <init> method ends.
 *
 * @author galeotti
 */
public class ReferenceConstant extends ReferenceExpression {


    private static final long serialVersionUID = 4288259851884045452L;

    public ReferenceConstant(Type objectType, int instanceId) {
        super(objectType, instanceId, 1, false);
    }

    @Override
    public Set<Variable<?>> getVariables() {
        return Collections.emptySet();
    }

    @Override
    public <K, V> K accept(ExpressionVisitor<K, V> v, V arg) {
        return v.visit(this, arg);
    }
}
