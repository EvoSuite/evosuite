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

import org.evosuite.symbolic.expr.Expression;
import org.evosuite.symbolic.expr.Operator;
import org.evosuite.symbolic.expr.bv.IntegerConstant;
import org.evosuite.symbolic.expr.bv.IntegerValue;
import org.evosuite.symbolic.expr.bv.StringMultipleComparison;
import org.evosuite.symbolic.expr.ref.ReferenceConstant;
import org.evosuite.symbolic.expr.str.StringValue;
import org.evosuite.symbolic.vm.SymbolicEnvironment;
import org.evosuite.symbolic.vm.SymbolicFunction;
import org.evosuite.symbolic.vm.heap.SymbolicHeap;

import java.util.ArrayList;

public final class RegionMatches5 extends SymbolicFunction {

    private static final String REGION_MATCHES = "regionMatches";

    public RegionMatches5(SymbolicEnvironment env) {
        super(env, Types.JAVA_LANG_STRING, REGION_MATCHES,
                Types.INT_STR_INT_INT_TO_BOOL_DESCRIPTOR);
    }

    @Override
    public Object executeFunction() {

        ReferenceConstant symb_receiver = this
                .getSymbReceiver();
        String conc_receiver = (String) this.getConcReceiver();
        StringValue stringReceiverExpr = env.heap.getField(
                Types.JAVA_LANG_STRING, SymbolicHeap.$STRING_VALUE,
                conc_receiver, symb_receiver, conc_receiver);

        IntegerValue ignoreCaseExpr = new IntegerConstant(0);
        IntegerValue toffsetExpr = this.getSymbIntegerArgument(0);

        ReferenceConstant symb_other = (ReferenceConstant) this
                .getSymbArgument(1);
        String conc_other = (String) this.getConcArgument(1);
        StringValue otherExpr = env.heap.getField(Types.JAVA_LANG_STRING,
                SymbolicHeap.$STRING_VALUE, conc_other, symb_other, conc_other);
        IntegerValue ooffsetExpr = this.getSymbIntegerArgument(2);
        IntegerValue lenExpr = this.getSymbIntegerArgument(3);

        boolean res = this.getConcBooleanRetVal();
        if (stringReceiverExpr.containsSymbolicVariable()
                || ignoreCaseExpr.containsSymbolicVariable()
                || toffsetExpr.containsSymbolicVariable()
                || otherExpr.containsSymbolicVariable()
                || ooffsetExpr.containsSymbolicVariable()
                || lenExpr.containsSymbolicVariable()) {

            ArrayList<Expression<?>> other = new ArrayList<>();
            other.add(toffsetExpr);
            other.add(ooffsetExpr);
            other.add(lenExpr);
            other.add(ignoreCaseExpr);
            int conV = res ? 1 : 0;

            StringMultipleComparison strComp = new StringMultipleComparison(
                    stringReceiverExpr, Operator.REGIONMATCHES, otherExpr,
                    other, (long) conV);

            return strComp;
        }

        return this.getSymbIntegerRetVal();
    }

}
