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

import org.evosuite.symbolic.expr.ref.ReferenceConstant;
import org.evosuite.symbolic.expr.ref.ReferenceExpression;
import org.evosuite.symbolic.expr.str.StringValue;
import org.evosuite.symbolic.expr.token.NewTokenizerExpr;
import org.evosuite.symbolic.vm.SymbolicEnvironment;
import org.evosuite.symbolic.vm.SymbolicFunction;
import org.evosuite.symbolic.vm.SymbolicHeap;
import org.evosuite.symbolic.vm.string.Types;

public final class StringTokenizer_Init extends SymbolicFunction {

	private static final String INIT = "<init>";

	public StringTokenizer_Init(SymbolicEnvironment env) {
		super(env, Types.JAVA_UTIL_STRING_TOKENIZER, INIT,
				Types.STR_STR_TO_VOID_DESCRIPTOR);
	}

	@Override
	public Object executeFunction() {

		// symbolic receiver (new object)
		ReferenceConstant symb_str_tokenizer = (ReferenceConstant) this
				.getSymbReceiver();

		// string argument
		String conc_str = (String) this.getConcArgument(0);
		ReferenceExpression symb_str = this.getSymbArgument(0);

		// delim argument
		String conc_delim = (String) this.getConcArgument(1);
		ReferenceExpression symb_delim = this.getSymbArgument(1);

		if (symb_str instanceof ReferenceConstant
				&& symb_delim instanceof ReferenceConstant) {
			ReferenceConstant non_null_symb_string = (ReferenceConstant) symb_str;
			assert conc_str != null;

			StringValue strExpr = env.heap.getField(Types.JAVA_LANG_STRING,
					SymbolicHeap.$STRING_VALUE, conc_str, non_null_symb_string,
					conc_str);

			ReferenceConstant non_null_symb_delim = (ReferenceConstant) symb_delim;
			assert conc_delim != null;

			StringValue delimExpr = env.heap.getField(Types.JAVA_LANG_STRING,
					SymbolicHeap.$STRING_VALUE, conc_delim,
					non_null_symb_delim, conc_delim);

			NewTokenizerExpr newTokenizerExpr = new NewTokenizerExpr(strExpr,
					delimExpr);

			// update symbolic heap
			env.heap.putField(Types.JAVA_UTIL_STRING_TOKENIZER,
					SymbolicHeap.$STRING_TOKENIZER_VALUE, null,
					symb_str_tokenizer, newTokenizerExpr);
		}

		// constructor returns void
		return null;
	}
}
