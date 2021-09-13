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
package org.evosuite.symbolic.vm.wrappers;

import org.evosuite.symbolic.expr.Operator;
import org.evosuite.symbolic.expr.bv.IntegerUnaryExpression;
import org.evosuite.symbolic.expr.bv.IntegerValue;
import org.evosuite.symbolic.vm.SymbolicEnvironment;
import org.evosuite.symbolic.vm.SymbolicFunction;

public final class Character_isDigit extends SymbolicFunction {

    private final static String IS_DIGIT = "isDigit";

    public Character_isDigit(SymbolicEnvironment env) {
        super(env, Types.JAVA_LANG_CHARACTER, IS_DIGIT, Types.C_TO_Z);
    }

    @Override
    public Object executeFunction() {
        IntegerValue charValueExpr = this.getSymbIntegerArgument(0);
        boolean res = this.getConcBooleanRetVal();

        if (charValueExpr.containsSymbolicVariable()) {

            long conV = res ? 1 : 0;

            return new IntegerUnaryExpression(
                    charValueExpr, Operator.ISDIGIT, conV);

        } else {
            return this.getSymbIntegerRetVal();
        }
    }

}
