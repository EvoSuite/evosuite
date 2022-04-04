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
package org.evosuite.symbolic.vm.string.tokenizer;

import org.evosuite.symbolic.expr.ref.ReferenceConstant;
import org.evosuite.symbolic.expr.token.HasMoreTokensExpr;
import org.evosuite.symbolic.expr.token.TokenizerExpr;
import org.evosuite.symbolic.vm.SymbolicEnvironment;
import org.evosuite.symbolic.vm.SymbolicFunction;
import org.evosuite.symbolic.vm.heap.SymbolicHeap;
import org.evosuite.symbolic.vm.string.Types;

import java.util.StringTokenizer;

public final class HasMoreTokens extends SymbolicFunction {

    private static final String HAS_MORE_TOKENS = "hasMoreTokens";

    public HasMoreTokens(SymbolicEnvironment env) {
        super(env, Types.JAVA_UTIL_STRING_TOKENIZER, HAS_MORE_TOKENS,
                Types.TO_BOOLEAN_DESCRIPTOR);
    }

    @Override
    public Object executeFunction() {

        StringTokenizer conc_tokenizer = (StringTokenizer) this
                .getConcReceiver();
        ReferenceConstant symb_tokenizer = this.getSymbReceiver();

        boolean res = this.getConcBooleanRetVal();

        TokenizerExpr tokenizerExpr = (TokenizerExpr) env.heap.getField(
                Types.JAVA_UTIL_STRING_TOKENIZER,
                SymbolicHeap.$STRING_TOKENIZER_VALUE, conc_tokenizer,
                symb_tokenizer);

        if (tokenizerExpr != null && tokenizerExpr.containsSymbolicVariable()) {
            HasMoreTokensExpr hasMoreTokenExpr = new HasMoreTokensExpr(
                    tokenizerExpr, res ? 1L : 0L);

            return hasMoreTokenExpr;

        }

        return this.getSymbIntegerRetVal();
    }
}
