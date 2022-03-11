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

import org.evosuite.symbolic.expr.AbstractExpression;
import org.evosuite.symbolic.expr.ExpressionVisitor;
import org.evosuite.symbolic.expr.Variable;
import org.evosuite.symbolic.expr.bv.IntegerValue;
import org.evosuite.symbolic.expr.fp.RealValue;
import org.evosuite.symbolic.expr.str.StringValue;

import java.util.HashSet;
import java.util.Set;

/**
 * Array store expressions.
 *
 * @author Ignacio Lebrero
 */
public final class ArrayStore {

    public static final class IntegerArrayStore extends AbstractExpression<Object> implements ArrayValue.IntegerArrayValue {

        private final ArrayValue.IntegerArrayValue symbolicArray;
        private final IntegerValue symbolicIndex;
        private final IntegerValue SymbolicValue;

        /**
         * @param arrayExpr
         * @param indexExpr
         */
        public IntegerArrayStore(ArrayValue.IntegerArrayValue arrayExpr, IntegerValue indexExpr, IntegerValue valueExpression, Object resultArray) {
            super(
                    resultArray,
                    1 + arrayExpr.getSize() + indexExpr.getSize() + valueExpression.getSize(),
                    arrayExpr.containsSymbolicVariable() || valueExpression.containsSymbolicVariable()
            );

            this.symbolicArray = arrayExpr;
            this.symbolicIndex = indexExpr;
            this.SymbolicValue = valueExpression;
        }

        @Override
        public String toString() {
            return symbolicArray + "[" + symbolicIndex + "] = " + SymbolicValue;
        }

        @Override
        public Set<Variable<?>> getVariables() {
            Set<Variable<?>> variables = new HashSet<Variable<?>>();
            variables.addAll(this.symbolicArray.getVariables());
            variables.addAll(this.symbolicIndex.getVariables());
            variables.addAll(this.SymbolicValue.getVariables());
            return variables;
        }

        @Override
        public Set<Object> getConstants() {
            Set<Object> result = new HashSet<>();
            result.addAll(this.symbolicArray.getConstants());
            result.addAll(this.symbolicIndex.getConstants());
            result.addAll(this.SymbolicValue.getConstants());
            return result;
        }

        @Override
        public <K, V> K accept(ExpressionVisitor<K, V> v, V arg) {
            return v.visit(this, arg);
        }

        public ArrayValue.IntegerArrayValue getSymbolicArray() {
            return symbolicArray;
        }

        public IntegerValue getSymbolicIndex() {
            return symbolicIndex;
        }

        public IntegerValue getSymbolicValue() {
            return SymbolicValue;
        }
    }

    public static final class RealArrayStore extends AbstractExpression<Object> implements ArrayValue.RealArrayValue {
        private final RealValue symbolicValue;
        private final IntegerValue symbolicIndex;
        private final ArrayValue.RealArrayValue symbolicArray;

        /**
         * @param arrayExpr
         * @param indexExpr
         * @param valueExpression
         */
        public RealArrayStore(ArrayValue.RealArrayValue arrayExpr, IntegerValue indexExpr, RealValue valueExpression, Object concreteResultArray) {
            super(
                    concreteResultArray,
                    1 + arrayExpr.getSize() + indexExpr.getSize() + valueExpression.getSize(),
                    arrayExpr.containsSymbolicVariable() || valueExpression.containsSymbolicVariable()
            );

            this.symbolicArray = arrayExpr;
            this.symbolicIndex = indexExpr;
            this.symbolicValue = valueExpression;
        }

        @Override
        public String toString() {
            return symbolicArray + "[" + symbolicIndex + "] = " + symbolicValue;
        }

        @Override
        public Set<Variable<?>> getVariables() {
            Set<Variable<?>> variables = new HashSet<Variable<?>>();
            variables.addAll(this.symbolicArray.getVariables());
            variables.addAll(this.symbolicIndex.getVariables());
            variables.addAll(this.symbolicValue.getVariables());
            return variables;
        }

        @Override
        public Set<Object> getConstants() {
            Set<Object> result = new HashSet<>();
            result.addAll(this.symbolicArray.getConstants());
            result.addAll(this.symbolicIndex.getConstants());
            result.addAll(this.symbolicValue.getConstants());
            return result;
        }

        @Override
        public <K, V> K accept(ExpressionVisitor<K, V> v, V arg) {
            return v.visit(this, arg);
        }

        public ArrayValue.RealArrayValue getSymbolicArray() {
            return symbolicArray;
        }

        public IntegerValue getSymbolicIndex() {
            return symbolicIndex;
        }

        public RealValue getSymbolicValue() {
            return symbolicValue;
        }
    }

    public static final class StringArrayStore extends AbstractExpression<Object> implements ArrayValue.StringArrayValue {
        private final StringValue symbolicValue;
        private final IntegerValue symbolicIndex;
        private final ArrayValue.StringArrayValue symbolicArray;

        /**
         * @param arrayExpr
         * @param indexExpr
         * @param valueExpression
         */
        public StringArrayStore(ArrayValue.StringArrayValue arrayExpr, IntegerValue indexExpr, StringValue valueExpression, Object concreteResultArray) {
            super(
                    concreteResultArray,
                    1 + arrayExpr.getSize() + indexExpr.getSize() + valueExpression.getSize(),
                    arrayExpr.containsSymbolicVariable() || valueExpression.containsSymbolicVariable()
            );

            this.symbolicArray = arrayExpr;
            this.symbolicIndex = indexExpr;
            this.symbolicValue = valueExpression;
        }

        @Override
        public String toString() {
            return symbolicArray + "[" + symbolicIndex + "] = " + symbolicValue;
        }

        @Override
        public Set<Variable<?>> getVariables() {
            Set<Variable<?>> variables = new HashSet<Variable<?>>();
            variables.addAll(this.symbolicArray.getVariables());
            variables.addAll(this.symbolicIndex.getVariables());
            variables.addAll(this.symbolicValue.getVariables());
            return variables;
        }

        @Override
        public Set<Object> getConstants() {
            Set<Object> result = new HashSet<>();
            result.addAll(this.symbolicArray.getConstants());
            result.addAll(this.symbolicIndex.getConstants());
            result.addAll(this.symbolicValue.getConstants());
            return result;
        }

        @Override
        public <K, V> K accept(ExpressionVisitor<K, V> v, V arg) {
            return v.visit(this, arg);
        }

        public ArrayValue.StringArrayValue getSymbolicArray() {
            return symbolicArray;
        }

        public IntegerValue getSymbolicIndex() {
            return symbolicIndex;
        }

        public StringValue getSymbolicValue() {
            return symbolicValue;
        }
    }
}