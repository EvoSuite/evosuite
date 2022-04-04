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
package org.evosuite.symbolic.solver.smt;

public class SmtArrayConstant {

    public static class SmtIntegerArrayConstant extends SmtConstant {
        private final Object array;

        public SmtIntegerArrayConstant(Object arrayValue) {
            this.array = arrayValue;
        }

        @Override
        public <K, V> K accept(SmtExprVisitor<K, V> v, V arg) {
            return v.visit(this, arg);
        }

        public Object getConstantValue() {
            return array;
        }
    }

    public static class SmtRealArrayConstant extends SmtConstant {
        private final Object array;

        public SmtRealArrayConstant(Object arrayValue) {
            this.array = arrayValue;
        }

        @Override
        public <K, V> K accept(SmtExprVisitor<K, V> v, V arg) {
            return v.visit(this, arg);
        }

        public Object getConstantValue() {
            return array;
        }
    }

    public static class SmtStringArrayConstant extends SmtConstant {
        private final Object array;

        public SmtStringArrayConstant(Object arrayValue) {
            this.array = arrayValue;
        }

        @Override
        public <K, V> K accept(SmtExprVisitor<K, V> v, V arg) {
            return v.visit(this, arg);
        }

        public Object getConstantValue() {
            return array;
        }
    }

    public static class SmtReferenceArrayConstant extends SmtConstant {
        private final Object array;

        public SmtReferenceArrayConstant(Object arrayValue) {
            this.array = arrayValue;
        }

        @Override
        public <K, V> K accept(SmtExprVisitor<K, V> v, V arg) {
            return v.visit(this, arg);
        }

        public Object getConstantValue() {
            return array;
        }
    }
}
