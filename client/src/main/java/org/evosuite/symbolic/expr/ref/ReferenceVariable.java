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
 * Represents a symbolic reference (for example, a pointer that we have declared
 * as symbolic) at the test case level. We assume these references are not
 * created during the SUT.
 *
 * @author galeotti
 */
public class ReferenceVariable extends ReferenceExpression implements Variable<Object> {


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
        super(objectType, instanceId, 1, true);
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
