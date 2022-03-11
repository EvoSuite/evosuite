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
import org.evosuite.symbolic.expr.ref.ReferenceConstant;
import org.objectweb.asm.Type;

/**
 * Represents an instant of an array.
 *
 * @author Ignacio Lebrero
 */
public abstract class ArrayConstant extends ReferenceConstant implements ArrayValue {

    public ArrayConstant(Type arrayType, int instanceId) {
        super(arrayType, instanceId);
    }

    public static final class IntegerArrayConstant extends ArrayConstant implements ArrayValue.IntegerArrayValue {

        public IntegerArrayConstant(Type arrayType, int instanceId) {
            super(arrayType, instanceId);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean equals(Object obj) {
            if (obj instanceof ArrayConstant.IntegerArrayConstant) {
                ArrayConstant.IntegerArrayConstant v = (ArrayConstant.IntegerArrayConstant) obj;
                return this.concreteValue.equals(v.concreteValue);
            }
            return false;
        }


        @Override
        public <K, V> K accept(ExpressionVisitor<K, V> v, V arg) {
            return v.visit(this, arg);
        }
    }

    public static final class RealArrayConstant extends ArrayConstant implements ArrayValue.RealArrayValue {

        public RealArrayConstant(Type arrayType, int instanceId) {
            super(arrayType, instanceId);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean equals(Object obj) {
            if (obj instanceof ArrayConstant.RealArrayConstant) {
                ArrayConstant.RealArrayConstant v = (ArrayConstant.RealArrayConstant) obj;
                return this.concreteValue.equals(v.concreteValue);
            }
            return false;
        }

        @Override
        public <K, V> K accept(ExpressionVisitor<K, V> v, V arg) {
            return v.visit(this, arg);
        }
    }

    public static final class StringArrayConstant extends ArrayConstant implements ArrayValue.StringArrayValue {

        public StringArrayConstant(Type arrayType, int instanceId) {
            super(arrayType, instanceId);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean equals(Object obj) {
            if (obj instanceof ArrayConstant.StringArrayConstant) {
                ArrayConstant.StringArrayConstant v = (ArrayConstant.StringArrayConstant) obj;
                return this.concreteValue.equals(v.concreteValue);
            }
            return false;
        }

        @Override
        public <K, V> K accept(ExpressionVisitor<K, V> v, V arg) {
            return v.visit(this, arg);
        }
    }

    public static final class ReferenceArrayConstant extends ArrayConstant implements ArrayValue.ReferenceArrayValue {

        public ReferenceArrayConstant(Type arrayType, int instanceId) {
            super(arrayType, instanceId);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean equals(Object obj) {
            if (obj instanceof ArrayConstant.ReferenceArrayConstant) {
                ArrayConstant.ReferenceArrayConstant v = (ArrayConstant.ReferenceArrayConstant) obj;
                return this.concreteValue.equals(v.concreteValue);
            }
            return false;
        }

        @Override
        public <K, V> K accept(ExpressionVisitor<K, V> v, V arg) {
            return v.visit(this, arg);
        }
    }
}

