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
package org.evosuite.symbolic.vm.regex;

import java.util.regex.Matcher;

import org.evosuite.symbolic.expr.Operator;
import org.evosuite.symbolic.expr.bv.StringBinaryComparison;
import org.evosuite.symbolic.expr.ref.ReferenceConstant;
import org.evosuite.symbolic.expr.str.StringConstant;
import org.evosuite.symbolic.expr.str.StringValue;
import org.evosuite.symbolic.vm.ExpressionFactory;
import org.evosuite.symbolic.vm.SymbolicEnvironment;
import org.evosuite.symbolic.vm.SymbolicFunction;
import org.evosuite.symbolic.vm.SymbolicHeap;

public final class Matcher_Matches extends SymbolicFunction {

	private static final String MATCHES = "matches";

	public Matcher_Matches(SymbolicEnvironment env) {
		super(env, Types.JAVA_UTIL_REGEX_MATCHER, MATCHES, Types.TO_BOOLEAN);
	}

	@Override
	public Object executeFunction() {
		Matcher conc_matcher = (Matcher) this.getConcReceiver();
		ReferenceConstant symb_matcher = (ReferenceConstant) this
				.getSymbReceiver();
		boolean res = this.getConcBooleanRetVal();

		String conc_regex = conc_matcher.pattern().pattern();
		StringValue symb_input = (StringValue) env.heap.getField(
				Types.JAVA_UTIL_REGEX_MATCHER, SymbolicHeap.$MATCHER_INPUT,
				conc_matcher, symb_matcher);

		if (symb_input != null && symb_input.containsSymbolicVariable()) {
			int concrete_value = res ? 1 : 0;
			StringConstant symb_regex = ExpressionFactory
					.buildNewStringConstant(conc_regex);
			StringBinaryComparison strComp = new StringBinaryComparison(symb_regex,
					Operator.PATTERNMATCHES, symb_input, (long) concrete_value);

			return strComp;
		} else {
			return this.getSymbIntegerRetVal();
		}
	}
}
