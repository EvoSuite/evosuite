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
package org.evosuite.symbolic.vm.string.reader;

import org.evosuite.symbolic.expr.reader.StringReaderExpr;
import org.evosuite.symbolic.expr.ref.ReferenceConstant;
import org.evosuite.symbolic.expr.ref.ReferenceExpression;
import org.evosuite.symbolic.expr.str.StringValue;
import org.evosuite.symbolic.vm.SymbolicEnvironment;
import org.evosuite.symbolic.vm.SymbolicFunction;
import org.evosuite.symbolic.vm.SymbolicHeap;
import org.evosuite.symbolic.vm.string.Types;

public final class StringReader_Init extends SymbolicFunction {

	private static final String INIT = "<init>";

	public StringReader_Init(SymbolicEnvironment env) {
		super(env, Types.JAVA_IO_STRING_READER, INIT,
				Types.STR_TO_VOID_DESCRIPTOR);
	}

	@Override
	public Object executeFunction() {

		// symbolic receiver (new object)
		ReferenceConstant symb_str_reader = (ReferenceConstant) this
				.getSymbReceiver();

		// string argument
		String conc_str = (String) this.getConcArgument(0);
		ReferenceExpression symb_str = this.getSymbArgument(0);

		if (symb_str instanceof ReferenceConstant) {
			ReferenceConstant non_null_symb_string = (ReferenceConstant) symb_str;
			assert conc_str != null;

			StringValue strExpr = env.heap.getField(Types.JAVA_LANG_STRING,
					SymbolicHeap.$STRING_VALUE, conc_str, non_null_symb_string,
					conc_str);

			if (strExpr.containsSymbolicVariable()) {

				int conc_string_reader_value;
				if (conc_str.length() == 0) {
					conc_string_reader_value = -1;
				} else {
					conc_string_reader_value = conc_str.charAt(0);
				}

				StringReaderExpr newStringReaderExpr = new StringReaderExpr(
						(long) conc_string_reader_value, strExpr);

				// update symbolic heap
				env.heap.putField(Types.JAVA_IO_STRING_READER,
						SymbolicHeap.$STRING_READER_VALUE, null,
						symb_str_reader, newStringReaderExpr);

			}
		}

		// constructor returns void
		return null;
	}
}
