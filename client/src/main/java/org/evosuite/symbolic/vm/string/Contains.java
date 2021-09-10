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
package org.evosuite.symbolic.vm.string;

import org.evosuite.symbolic.expr.Operator;
import org.evosuite.symbolic.expr.bv.StringBinaryComparison;
import org.evosuite.symbolic.expr.ref.ReferenceConstant;
import org.evosuite.symbolic.expr.str.StringValue;
import org.evosuite.symbolic.vm.SymbolicEnvironment;
import org.evosuite.symbolic.vm.SymbolicFunction;
import org.evosuite.symbolic.vm.heap.SymbolicHeap;

public final class Contains extends SymbolicFunction {

    private static final String CONTAINS = "contains";

    public Contains(SymbolicEnvironment env) {
        super(env, Types.JAVA_LANG_STRING, CONTAINS,
                Types.CHARSEQ_TO_BOOL_DESCRIPTOR);
    }

    @Override
    public Object executeFunction() {
        String conc_left = (String) this.getConcReceiver();
        ReferenceConstant symb_left = this.getSymbReceiver();

        StringValue left_expr = env.heap.getField(Types.JAVA_LANG_STRING,
                SymbolicHeap.$STRING_VALUE, conc_left, symb_left, conc_left);

        CharSequence conc_right = (CharSequence) this.getConcArgument(0);
        ReferenceConstant symb_right = (ReferenceConstant) this
                .getSymbArgument(0);

        if (conc_right instanceof String) {
            String conc_right_str = (String) conc_right;
            StringValue right_expr = env.heap.getField(Types.JAVA_LANG_STRING,
                    SymbolicHeap.$STRING_VALUE, conc_right_str, symb_right,
                    conc_right_str);

            boolean res = this.getConcBooleanRetVal();
            if (right_expr != null) {
                if (left_expr.containsSymbolicVariable()
                        || right_expr.containsSymbolicVariable()) {

                    int concrete_value = res ? 1 : 0;

                    StringBinaryComparison strComp = new StringBinaryComparison(left_expr,
                            Operator.CONTAINS, right_expr,
                            (long) concrete_value);

                    return strComp;
                }
            }
        }

        return this.getSymbIntegerRetVal();
    }

}