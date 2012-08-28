package org.evosuite.symbolic.vm.regex;

import java.util.Iterator;

import org.evosuite.symbolic.expr.Operator;
import org.evosuite.symbolic.expr.StringComparison;
import org.evosuite.symbolic.expr.StringExpression;
import org.evosuite.symbolic.expr.StringToIntCast;
import org.evosuite.symbolic.vm.Function;
import org.evosuite.symbolic.vm.NonNullReference;
import org.evosuite.symbolic.vm.Operand;
import org.evosuite.symbolic.vm.Reference;
import org.evosuite.symbolic.vm.SymbolicEnvironment;
import org.evosuite.symbolic.vm.SymbolicHeap;

/**
 * 
 * @author galeotti
 * 
 */
public final class Pattern_Matches extends Function {

	private static final String MATCHES = "matches";

	public Pattern_Matches(SymbolicEnvironment env) {
		super(env, Types.JAVA_UTIL_REGEX_PATTERN, MATCHES,
				Types.STR_CHARSEQ_TO_BOOLEAN);
	}

	@Override
	public void INVOKESTATIC() {

		symb_input = null;

		symb_regex = null;

	}

	private StringExpression symb_regex;
	private StringExpression symb_input;

	@Override
	public void CALL_RESULT(boolean res) {
		if (symb_input != null && symb_input.containsSymbolicVariable()) {

			int concrete_value = res ? 1 : 0;

			StringComparison strComp = new StringComparison(symb_regex,
					Operator.PATTERNMATCHES, symb_input, (long) concrete_value);


			replaceTopBv32(strComp);
		}
	}

	@Override
	public void CALLER_STACK_PARAM(int nr, int calleeLocalsIndex, Object value) {

		if (nr == 0) {
			Iterator<Operand> it = this.env.topFrame().operandStack.iterator();
			ref(it.next());
			Reference regex_ref = ref(it.next());
			if (regex_ref instanceof NonNullReference) {
				String string = (String) value;
				NonNullReference regex_str_ref = (NonNullReference) regex_ref;
				symb_regex = env.heap.getField(Types.JAVA_LANG_STRING,
						SymbolicHeap.$STRING_VALUE, string, regex_str_ref,
						string);
			} else {
				symb_regex = null;
			}

		} else if (nr == 1) {

			Iterator<Operand> it = this.env.topFrame().operandStack.iterator();
			Reference input_ref = ref(it.next());
			if (input_ref instanceof NonNullReference) {

				NonNullReference input_str_ref = (NonNullReference) input_ref;
				if (value instanceof String) {

					String string = (String) value;
					symb_input = env.heap.getField(Types.JAVA_LANG_STRING,
							SymbolicHeap.$STRING_VALUE, string, input_str_ref,
							string);
				} else if (value instanceof StringBuilder) {

					StringBuilder stringBuffer = (StringBuilder) value;
					env.heap.getField(Types.JAVA_LANG_STRING_BUILDER,
							SymbolicHeap.$STRING_BUILDER_CONTENTS,
							stringBuffer, input_str_ref,
							stringBuffer.toString());

				} else {
					symb_input = null;
				}
			} else {
				symb_input = null;
			}

		}
	}
}
