/**
 * Copyright (C) 2010-2016 Gordon Fraser, Andrea Arcuri and EvoSuite
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

import java.util.StringTokenizer;

import org.evosuite.symbolic.expr.ref.ReferenceConstant;
import org.evosuite.symbolic.expr.token.NextTokenizerExpr;
import org.evosuite.symbolic.expr.token.StringNextTokenExpr;
import org.evosuite.symbolic.expr.token.TokenizerExpr;
import org.evosuite.symbolic.vm.SymbolicEnvironment;
import org.evosuite.symbolic.vm.SymbolicFunction;
import org.evosuite.symbolic.vm.SymbolicHeap;
import org.evosuite.symbolic.vm.string.Types;

public final class NextToken extends SymbolicFunction {

	private static final String NEXT_TOKEN = "nextToken";

	public NextToken(SymbolicEnvironment env) {
		super(env, Types.JAVA_UTIL_STRING_TOKENIZER, NEXT_TOKEN,
				Types.TO_STR_DESCRIPTOR);
	}

	@Override
	public Object executeFunction() {

		ReferenceConstant symb_receiver = this.getSymbReceiver();
		StringTokenizer conc_receiver = (StringTokenizer) this
				.getConcReceiver();

		TokenizerExpr tokenizerExpr = (TokenizerExpr) env.heap.getField(
				Types.JAVA_UTIL_STRING_TOKENIZER,
				SymbolicHeap.$STRING_TOKENIZER_VALUE, conc_receiver,
				symb_receiver);

		if (tokenizerExpr != null && tokenizerExpr.containsSymbolicVariable()) {

			ReferenceConstant symb_ret_val = (ReferenceConstant) this
					.getSymbRetVal();
			String conc_ret_val = (String) this.getConcRetVal();

			// create new NEXT_TOKEN string expression
			StringNextTokenExpr string_next_token_expr = new StringNextTokenExpr(
					tokenizerExpr, conc_ret_val);
			env.heap.putField(Types.JAVA_LANG_STRING,
					SymbolicHeap.$STRING_VALUE, conc_ret_val, symb_ret_val,
					string_next_token_expr);

			// update StringTokenizer's symbolic state
			NextTokenizerExpr nextTokenizerExpr = new NextTokenizerExpr(
					tokenizerExpr);

			env.heap.putField(Types.JAVA_UTIL_STRING_TOKENIZER,
					SymbolicHeap.$STRING_TOKENIZER_VALUE, conc_receiver,
					symb_receiver, nextTokenizerExpr);

		}
		return this.getSymbRetVal();
	}
}
