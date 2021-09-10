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

import org.evosuite.symbolic.expr.ExpressionVisitor;

/**
 * General expression for anonymous jvm-created lambda classes (usually after invokedynamic is used).
 * <p>
 * TODO: Lambdas may be closures so a lot of fields may be attached to them.
 *
 * @author Ignacio Lebrero
 */
public class LambdaSyntheticType extends LiteralClassType {

    private final boolean callsNonInstrumentedCode;

    public LambdaSyntheticType(Class concreteValue, boolean callsNonInstrumentedCode) {
        super(concreteValue);

        this.callsNonInstrumentedCode = callsNonInstrumentedCode;
    }

    public boolean callsNonInstrumentedCode() {
        return callsNonInstrumentedCode;
    }

    @Override
    public <K, V> K accept(ExpressionVisitor<K, V> v, V arg) {
        return v.visit(this, arg);
    }
}