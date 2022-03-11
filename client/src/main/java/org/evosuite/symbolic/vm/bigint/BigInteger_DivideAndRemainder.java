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
package org.evosuite.symbolic.vm.bigint;

import org.evosuite.symbolic.expr.bv.IntegerValue;
import org.evosuite.symbolic.expr.ref.ReferenceConstant;
import org.evosuite.symbolic.expr.ref.ReferenceExpression;
import org.evosuite.symbolic.vm.ExpressionFactory;
import org.evosuite.symbolic.vm.SymbolicEnvironment;
import org.evosuite.symbolic.vm.SymbolicFunction;
import org.evosuite.symbolic.vm.heap.SymbolicHeap;

import java.lang.reflect.Array;
import java.math.BigInteger;

public final class BigInteger_DivideAndRemainder extends SymbolicFunction {

    private static final String DIVIDE_AND_REMAINDER = "divideAndRemainder";
    private static final int REMAINDER_ARRAY_INDEX = 1;
    private static final int QUOTIENT_ARRAY_INDEX = 0;

    public BigInteger_DivideAndRemainder(SymbolicEnvironment env) {
        super(env, Types.JAVA_MATH_BIG_INTEGER, DIVIDE_AND_REMAINDER,
                Types.BIG_INTEGER_TO_BIG_INTEGER_ARRAY);
    }

    @Override
    public Object executeFunction() {
        BigInteger conc_left_big_integer = (BigInteger) this.getConcReceiver();
        ReferenceConstant symb_left_big_integer = this.getSymbReceiver();

        BigInteger conc_right_big_integer = (BigInteger) this
                .getConcArgument(0);
        ReferenceConstant symb_right_big_integer = (ReferenceConstant) this
                .getSymbArgument(0);

        Object res = this.getConcRetVal();
        ReferenceExpression symb_res = this.getSymbRetVal();

        if (res != null && conc_left_big_integer != null
                && conc_right_big_integer != null) {

            IntegerValue left_big_integer_expr = this.env.heap.getField(
                    Types.JAVA_MATH_BIG_INTEGER,
                    SymbolicHeap.$BIG_INTEGER_CONTENTS, conc_left_big_integer,
                    symb_left_big_integer, conc_left_big_integer.longValue());

            IntegerValue right_big_integer_expr = this.env.heap.getField(
                    Types.JAVA_MATH_BIG_INTEGER,
                    SymbolicHeap.$BIG_INTEGER_CONTENTS, conc_right_big_integer,
                    symb_right_big_integer, conc_right_big_integer.longValue());

            if (left_big_integer_expr.containsSymbolicVariable()
                    || right_big_integer_expr.containsSymbolicVariable()) {

                // quotient
                BigInteger conc_quotient = (BigInteger) Array.get(res,
                        QUOTIENT_ARRAY_INDEX);

                ReferenceConstant symb_quotient = (ReferenceConstant) this.env.heap
                        .getReference(conc_quotient);

                IntegerValue symb_div_value = ExpressionFactory.div(
                        left_big_integer_expr, right_big_integer_expr,
                        conc_quotient.longValue());

                this.env.heap.putField(Types.JAVA_MATH_BIG_INTEGER,
                        SymbolicHeap.$BIG_INTEGER_CONTENTS, conc_quotient,
                        symb_quotient, symb_div_value);

                // remainder
                BigInteger conc_remainder = (BigInteger) Array.get(res,
                        REMAINDER_ARRAY_INDEX);

                ReferenceConstant symb_remainder = (ReferenceConstant) this.env.heap
                        .getReference(conc_remainder);

                IntegerValue symb_rem_value = ExpressionFactory.rem(
                        left_big_integer_expr, right_big_integer_expr,
                        conc_remainder.longValue());

                this.env.heap.putField(Types.JAVA_MATH_BIG_INTEGER,
                        SymbolicHeap.$BIG_INTEGER_CONTENTS, conc_remainder,
                        symb_remainder, symb_rem_value);

            }
        }

        return symb_res;
    }

}
