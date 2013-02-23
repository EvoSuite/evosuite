package org.evosuite.symbolic.vm.regex;

import java.util.regex.Matcher;

import org.evosuite.symbolic.expr.str.StringValue;
import org.evosuite.symbolic.vm.NonNullReference;
import org.evosuite.symbolic.vm.Reference;
import org.evosuite.symbolic.vm.SymbolicEnvironment;
import org.evosuite.symbolic.vm.SymbolicFunction;
import org.evosuite.symbolic.vm.SymbolicHeap;

public final class Pattern_Matcher extends SymbolicFunction {

	private static final String MATCHER = "matcher";

	public Pattern_Matcher(SymbolicEnvironment env) {
		super(env, Types.JAVA_UTIL_REGEX_PATTERN, MATCHER,
				Types.CHARSEQ_TO_MATCHER);
	}

	@Override
	public Object executeFunction() {

		// receiver
		@SuppressWarnings("unused")
		NonNullReference symb_receiver = this.getSymbReceiver();

		// argument
		CharSequence conc_char_seq = (CharSequence) this.getConcArgument(0);
		Reference symb_char_seq= this.getSymbArgument(0);

		// return value
		Matcher conc_matcher = (Matcher) this.getConcRetVal();
		NonNullReference symb_matcher = (NonNullReference) this.getSymbRetVal();

		if (conc_char_seq != null && conc_char_seq instanceof String) {
			assert symb_char_seq instanceof NonNullReference;
			NonNullReference symb_string = (NonNullReference)symb_char_seq;
			
			String string = (String) conc_char_seq;
			StringValue symb_input = env.heap.getField(Types.JAVA_LANG_STRING,
					SymbolicHeap.$STRING_VALUE, string, symb_string, string);

			env.heap.putField(Types.JAVA_UTIL_REGEX_MATCHER,
					SymbolicHeap.$MATCHER_INPUT, conc_matcher, symb_matcher,
					symb_input);
		}
		return symb_matcher;
	}

}
