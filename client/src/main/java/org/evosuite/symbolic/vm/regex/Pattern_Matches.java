package org.evosuite.symbolic.vm.regex;

import org.evosuite.symbolic.expr.Operator;
import org.evosuite.symbolic.expr.bv.StringBinaryComparison;
import org.evosuite.symbolic.expr.str.StringValue;
import org.evosuite.symbolic.vm.NonNullReference;
import org.evosuite.symbolic.vm.Reference;
import org.evosuite.symbolic.vm.SymbolicEnvironment;
import org.evosuite.symbolic.vm.SymbolicFunction;
import org.evosuite.symbolic.vm.SymbolicHeap;

/**
 * 
 * @author galeotti
 * 
 */
public final class Pattern_Matches extends SymbolicFunction {

	private static final String MATCHES = "matches";

	public Pattern_Matches(SymbolicEnvironment env) {
		super(env, Types.JAVA_UTIL_REGEX_PATTERN, MATCHES,
				Types.STR_CHARSEQ_TO_BOOLEAN);
	}

	@Override
	public Object executeFunction() {

		// argument 0
		String regex_str = (String) this.getConcArgument(0);
		NonNullReference regex_ref = (NonNullReference) this.getSymbArgument(0);

		// argument 1
		CharSequence input_char_seq = (CharSequence) this.getConcArgument(1);
		Reference input_ref = this.getSymbArgument(1);

		// return value
		boolean res = this.getConcBooleanRetVal();

		// symbolic execution
		StringValue symb_regex = env.heap.getField(Types.JAVA_LANG_STRING,
				SymbolicHeap.$STRING_VALUE, regex_str, regex_ref, regex_str);

		StringValue symb_input = getSymbInput(input_char_seq, input_ref);

		if (symb_input != null && symb_input.containsSymbolicVariable()) {

			int concrete_value = res ? 1 : 0;

			StringBinaryComparison strComp = new StringBinaryComparison(symb_regex,
					Operator.PATTERNMATCHES, symb_input, (long) concrete_value);

			return strComp;
		} else {
			return this.getSymbIntegerRetVal();
		}

	}

	private StringValue getSymbInput(CharSequence input_char_seq,
			Reference input_ref) {
		StringValue symb_input;
		if (input_ref instanceof NonNullReference) {
			NonNullReference input_str_ref = (NonNullReference) input_ref;
			assert input_char_seq != null;

			if (input_char_seq instanceof String) {

				String string = (String) input_char_seq;
				symb_input = env.heap.getField(Types.JAVA_LANG_STRING,
						SymbolicHeap.$STRING_VALUE, string, input_str_ref,
						string);

			} else if (input_char_seq instanceof StringBuilder) {

				StringBuilder stringBuffer = (StringBuilder) input_char_seq;
				symb_input = env.heap.getField(Types.JAVA_LANG_STRING_BUILDER,
						SymbolicHeap.$STRING_BUILDER_CONTENTS, stringBuffer,
						input_str_ref, stringBuffer.toString());
			} else {
				symb_input = null;
			}
		} else {
			symb_input = null;
		}
		return symb_input;
	}
}
