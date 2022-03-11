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
package org.evosuite.symbolic.vm.math;

import org.evosuite.symbolic.expr.Operator;
import org.evosuite.symbolic.expr.bv.IntegerValue;
import org.evosuite.symbolic.expr.bv.RealUnaryToIntegerExpression;
import org.evosuite.symbolic.expr.fp.RealValue;
import org.evosuite.symbolic.vm.SymbolicEnvironment;
import org.evosuite.symbolic.vm.SymbolicFunction;

public abstract class GetExponent {

    private static final String GET_EXPONENT = "getExponent";

    public static class GetExponent_F extends SymbolicFunction {

        public GetExponent_F(SymbolicEnvironment env) {
            super(env, Types.JAVA_LANG_MATH, GET_EXPONENT, Types.F2I_DESCRIPTOR);
        }

        @Override
        public Object executeFunction() {

            int res = this.getConcIntRetVal();
            RealValue realExpression = this.getSymbRealArgument(0);

            IntegerValue getExponentExpr;
            if (realExpression.containsSymbolicVariable()) {
                Operator op = Operator.GETEXPONENT;
                getExponentExpr = new RealUnaryToIntegerExpression(
                        realExpression, op, (long) res);
            } else {
                getExponentExpr = this.getSymbIntegerRetVal();
            }
            return getExponentExpr;
        }

    }

    public static class GetExponent_D extends SymbolicFunction {

        public GetExponent_D(SymbolicEnvironment env) {
            super(env, Types.JAVA_LANG_MATH, GET_EXPONENT, Types.D2I_DESCRIPTOR);
        }

        @Override
        public Object executeFunction() {

            int res = this.getConcIntRetVal();
            RealValue realExpression = this.getSymbRealArgument(0);

            IntegerValue getExponentExpr;
            if (realExpression.containsSymbolicVariable()) {
                Operator op = Operator.GETEXPONENT;
                getExponentExpr = new RealUnaryToIntegerExpression(
                        realExpression, op, (long) res);
            } else {
                getExponentExpr = this.getSymbIntegerRetVal();
            }
            return getExponentExpr;
        }

    }

}
