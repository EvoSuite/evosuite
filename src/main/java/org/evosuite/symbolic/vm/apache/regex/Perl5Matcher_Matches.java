package org.evosuite.symbolic.vm.apache.regex;

import org.apache.oro.text.regex.Pattern;
import org.evosuite.symbolic.expr.Operator;
import org.evosuite.symbolic.expr.bv.StringBinaryComparison;
import org.evosuite.symbolic.expr.str.StringConstant;
import org.evosuite.symbolic.expr.str.StringValue;
import org.evosuite.symbolic.vm.ExpressionFactory;
import org.evosuite.symbolic.vm.NonNullReference;
import org.evosuite.symbolic.vm.SymbolicEnvironment;
import org.evosuite.symbolic.vm.SymbolicFunction;
import org.evosuite.symbolic.vm.SymbolicHeap;

public final class Perl5Matcher_Matches extends SymbolicFunction {

	private static final String MATCHES = "matches";

	public Perl5Matcher_Matches(SymbolicEnvironment env) {
		super(env, Types.ORG_APACHE_ORO_TEXT_REGEX_PERL5MATCHER, MATCHES,
				Types.STR_STR_TO_BOOLEAN);
	}

	@Override
	public Object executeFunction() {
		// Perl5Matcher conc_matcher = (Perl5Matcher) this.getConcReceiver();
		// NonNullReference symb_matcher = (NonNullReference) this
		// .getSymbReceiver();
		boolean res = this.getConcBooleanRetVal();

		NonNullReference symb_string_ref = (NonNullReference) this
				.getSymbArgument(0);
		// Reference symb_pattern_ref = this.getSymbArgument(1);

		String conc_string = (String) this.getConcArgument(0);
		Pattern conc_pattern = (Pattern) this.getConcArgument(1);

		StringValue symb_string_value = env.heap.getField(
				org.evosuite.symbolic.vm.regex.Types.JAVA_LANG_STRING,
				SymbolicHeap.$STRING_VALUE, conc_string, symb_string_ref,
				conc_string);

		if (symb_string_value != null
				&& symb_string_value.containsSymbolicVariable()) {
			int concrete_value = res ? 1 : 0;
			String pattern_str = conc_pattern.getPattern();
			StringConstant symb_pattern_value = ExpressionFactory
					.buildNewStringConstant(pattern_str);

			StringBinaryComparison strComp = new StringBinaryComparison(
					symb_pattern_value, Operator.APACHE_ORO_PATTERN_MATCHES,
					symb_string_value, (long) concrete_value);

			return strComp;
		} else {
			return this.getSymbIntegerRetVal();
		}
	}
}
