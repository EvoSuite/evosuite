package org.evosuite.symbolic.vm.regex;

import java.util.regex.Matcher;

import org.evosuite.symbolic.expr.Operator;
import org.evosuite.symbolic.expr.bv.StringBinaryComparison;
import org.evosuite.symbolic.expr.str.StringConstant;
import org.evosuite.symbolic.expr.str.StringValue;
import org.evosuite.symbolic.vm.ExpressionFactory;
import org.evosuite.symbolic.vm.NonNullReference;
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
		NonNullReference symb_matcher = (NonNullReference) this
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
