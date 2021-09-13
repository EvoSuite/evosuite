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

public final class ArraySelect {

    public static final class RealArraySelect extends AbstractExpression<Double> implements RealValue {

        private static final long serialVersionUID = -1638142164886370510L;

        private final ArrayValue.RealArrayValue symbolicArray;
        private final IntegerValue symbolicIndex;
        private final RealValue symbolicSelectedValue;

        /**
         * @param arrayExpr
         * @param indexExpr
         * @param selectedValueExpr
         */
        public RealArraySelect(ArrayValue.RealArrayValue arrayExpr, IntegerValue indexExpr, RealValue selectedValueExpr) {
            super(
                    selectedValueExpr.getConcreteValue(),
                    1 + arrayExpr.getSize() + indexExpr.getSize() + selectedValueExpr.getSize(),
                    arrayExpr.containsSymbolicVariable()
            );

            this.symbolicArray = arrayExpr;
            this.symbolicIndex = indexExpr;
            this.symbolicSelectedValue = selectedValueExpr;
        }

        @Override
        public String toString() {
            return symbolicArray + "[" + symbolicIndex + "]";
        }

        @Override
        public Set<Variable<?>> getVariables() {
            Set<Variable<?>> variables = new HashSet<>();
            variables.addAll(this.symbolicArray.getVariables());
            variables.addAll(this.symbolicIndex.getVariables());
            variables.addAll(this.symbolicSelectedValue.getVariables());
            return variables;
        }

        @Override
        public Set<Object> getConstants() {
            Set<Object> result = new HashSet<>();
            result.addAll(this.symbolicArray.getConstants());
            result.addAll(this.symbolicIndex.getConstants());
            result.addAll(this.symbolicSelectedValue.getConstants());
            return result;
        }

        @Override
        public <K, V> K accept(ExpressionVisitor<K, V> v, V arg) {
            return v.visit(this, arg);
        }

        public IntegerValue getSymbolicIndex() {
            return symbolicIndex;
        }

        public RealValue getSymbolicSelectedValue() {
            return symbolicSelectedValue;
        }

        public ArrayValue.RealArrayValue getSymbolicArray() {
            return symbolicArray;
        }
    }

    public static final class IntegerArraySelect extends AbstractExpression<Long> implements IntegerValue {

        private static final long serialVersionUID = -1638142164886370510L;

        private final ArrayValue.IntegerArrayValue symbolicArray;
        private final IntegerValue symbolicIndex;
        private final IntegerValue symbolicSelectedValue;

        /**
         * @param arrayExpr
         * @param indexExpr
         * @param symbolicSelectedValue
         */
        public IntegerArraySelect(ArrayValue.IntegerArrayValue arrayExpr, IntegerValue indexExpr, IntegerValue symbolicSelectedValue) {
            super(
                    symbolicSelectedValue.getConcreteValue(),
                    1 + arrayExpr.getSize() + indexExpr.getSize(),
                    arrayExpr.containsSymbolicVariable()
            );

            this.symbolicIndex = indexExpr;
            this.symbolicSelectedValue = symbolicSelectedValue;
            this.symbolicArray = arrayExpr;
        }

        @Override
        public String toString() {
            return symbolicArray + "[" + symbolicIndex + "]";
        }

        @Override
        public Set<Variable<?>> getVariables() {
            Set<Variable<?>> variables = new HashSet<>();
            variables.addAll(this.symbolicArray.getVariables());
            variables.addAll(this.symbolicIndex.getVariables());
            return variables;
        }

        @Override
        public Set<Object> getConstants() {
            Set<Object> result = new HashSet<>();
            result.addAll(this.symbolicArray.getConstants());
            result.addAll(this.symbolicIndex.getConstants());
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

        public IntegerValue getSymbolicSelectedValue() {
            return symbolicSelectedValue;
        }
    }

    public static final class StringArraySelect extends AbstractExpression<String> implements StringValue {

        private static final long serialVersionUID = -1638142164886370510L;

        private final ArrayValue.StringArrayValue symbolicArray;
        private final IntegerValue symbolicIndex;
        private final StringValue symbolicSelectedValue;

        /**
         * @param arrayExpr
         * @param indexExpr
         * @param selectedValueExpr
         */
        public StringArraySelect(ArrayValue.StringArrayValue arrayExpr, IntegerValue indexExpr, StringValue selectedValueExpr) {
            super(
                    selectedValueExpr.getConcreteValue(),
                    1 + arrayExpr.getSize() + indexExpr.getSize() + selectedValueExpr.getSize(),
                    arrayExpr.containsSymbolicVariable()
            );

            this.symbolicArray = arrayExpr;
            this.symbolicIndex = indexExpr;
            this.symbolicSelectedValue = selectedValueExpr;
        }

        @Override
        public String toString() {
            return symbolicArray + "[" + symbolicIndex + "]";
        }

        @Override
        public Set<Variable<?>> getVariables() {
            Set<Variable<?>> variables = new HashSet<>();
            variables.addAll(this.symbolicArray.getVariables());
            variables.addAll(this.symbolicIndex.getVariables());
            variables.addAll(this.symbolicSelectedValue.getVariables());
            return variables;
        }

        @Override
        public Set<Object> getConstants() {
            Set<Object> result = new HashSet<>();
            result.addAll(this.symbolicArray.getConstants());
            result.addAll(this.symbolicIndex.getConstants());
            result.addAll(this.symbolicSelectedValue.getConstants());
            return result;
        }

        @Override
        public <K, V> K accept(ExpressionVisitor<K, V> v, V arg) {
            return v.visit(this, arg);
        }

        public IntegerValue getSymbolicIndex() {
            return symbolicIndex;
        }

        public StringValue getSymbolicSelectedValue() {
            return symbolicSelectedValue;
        }

        public ArrayValue.StringArrayValue getSymbolicArray() {
            return symbolicArray;
        }
    }
}
