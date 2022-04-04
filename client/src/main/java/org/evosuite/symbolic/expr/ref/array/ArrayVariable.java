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
package org.evosuite.symbolic.expr.ref.array;

import org.evosuite.symbolic.expr.ExpressionVisitor;
import org.evosuite.symbolic.expr.ref.ReferenceVariable;
import org.objectweb.asm.Type;

/**
 * Represents an instance of a symbolic array.
 *
 * @author Ignacio Lebrero
 */
public abstract class ArrayVariable extends ReferenceVariable implements ArrayValue {

    public ArrayVariable(Type arrayType, int instanceId, String arrayName, Object concreteArray) {
        super(arrayType, instanceId, arrayName, concreteArray);
    }

    public void setConcreteValue(Object concreteValue) {
        this.concreteValue = concreteValue;
    }

    public static final class IntegerArrayVariable extends ArrayVariable implements ArrayValue.IntegerArrayValue {

        public IntegerArrayVariable(Type arrayType, int instanceId, String arrayName, Object concreteArray) {
            super(arrayType, instanceId, arrayName, concreteArray);
        }

        @Override
        public <K, V> K accept(ExpressionVisitor<K, V> v, V arg) {
            return v.visit(this, arg);
        }
    }

    public static final class RealArrayVariable extends ArrayVariable implements ArrayValue.RealArrayValue {

        public RealArrayVariable(Type arrayType, int instanceId, String arrayName, Object concreteArray) {
            super(arrayType, instanceId, arrayName, concreteArray);
        }

        @Override
        public <K, V> K accept(ExpressionVisitor<K, V> v, V arg) {
            return v.visit(this, arg);
        }

    }

    public static final class StringArrayVariable extends ArrayVariable implements ArrayValue.StringArrayValue {

        public StringArrayVariable(Type arrayType, int instanceId, String arrayName, Object concreteArray) {
            super(arrayType, instanceId, arrayName, concreteArray);
        }

        @Override
        public <K, V> K accept(ExpressionVisitor<K, V> v, V arg) {
            return v.visit(this, arg);
        }
    }

    public static final class ReferenceArrayVariable extends ArrayVariable implements ArrayValue.ReferenceArrayValue {

        public ReferenceArrayVariable(Type arrayType, int instanceId, String arrayName, Object concreteArray) {
            super(arrayType, instanceId, arrayName, concreteArray);
        }

        @Override
        public <K, V> K accept(ExpressionVisitor<K, V> v, V arg) {
            return v.visit(this, arg);
        }

    }

}


