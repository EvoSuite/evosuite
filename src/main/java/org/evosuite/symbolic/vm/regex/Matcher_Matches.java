package org.evosuite.symbolic.vm.regex;

import java.util.regex.Matcher;
import java.util.regex.PatternSyntaxException;

import org.evosuite.symbolic.expr.Operator;
import org.evosuite.symbolic.expr.bv.StringComparison;
import org.evosuite.symbolic.expr.str.StringConstant;
import org.evosuite.symbolic.expr.str.StringValue;
import org.evosuite.symbolic.vm.ExpressionFactory;
import org.evosuite.symbolic.vm.Function;
import org.evosuite.symbolic.vm.NonNullReference;
import org.evosuite.symbolic.vm.SymbolicEnvironment;
import org.evosuite.symbolic.vm.SymbolicHeap;

public final class Matcher_Matches extends Function {

	private static final String MATCHES = "matches";

	public Matcher_Matches(SymbolicEnvironment env) {
		super(env, Types.JAVA_UTIL_REGEX_MATCHER, MATCHES, Types.TO_BOOLEAN);
	}

	private Matcher conc_matcher;
	private NonNullReference symb_matcher;

	@Override
	public void INVOKEVIRTUAL(Object receiver) {
		if (receiver == null)
			return;

		conc_matcher = (Matcher) receiver;

		symb_matcher = (NonNullReference) env.topFrame().operandStack.peekRef();
	}

	@Override
	public void CALL_RESULT(boolean res) {
		String conc_regex = conc_matcher.pattern().pattern();

		StringValue symb_input = (StringValue) env.heap.getField(
				Types.JAVA_UTIL_REGEX_MATCHER, SymbolicHeap.$MATCHER_INPUT,
				conc_matcher, symb_matcher);
		if (symb_input != null && symb_input.containsSymbolicVariable()) {

			int concrete_value = res ? 1 : 0;
			StringConstant symb_regex = ExpressionFactory
					.buildNewStringConstant(conc_regex);
			StringComparison strComp = new StringComparison(symb_regex,
					Operator.PATTERNMATCHES, symb_input, (long) concrete_value);

			replaceTopBv32(strComp);
		}

	}
}
