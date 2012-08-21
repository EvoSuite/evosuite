package org.evosuite.symbolic.vm.regex;

import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.evosuite.symbolic.expr.StringExpression;
import org.evosuite.symbolic.vm.Function;
import org.evosuite.symbolic.vm.NonNullReference;
import org.evosuite.symbolic.vm.Operand;
import org.evosuite.symbolic.vm.Reference;
import org.evosuite.symbolic.vm.StringReference;
import org.evosuite.symbolic.vm.SymbolicEnvironment;
import org.evosuite.symbolic.vm.SymbolicHeap;

public final class Pattern_Matcher extends Function {

	private static final String MATCHER = "matcher";

	public Pattern_Matcher(SymbolicEnvironment env) {
		super(env, Types.JAVA_UTIL_REGEX_PATTERN, MATCHER,
				Types.CHARSEQ_TO_MATCHER);
	}

	Pattern conc_pattern;
	StringExpression symb_input;

	@Override
	public void INVOKEVIRTUAL(Object receiver) {
		if (receiver == null)
			return;

		conc_pattern = (Pattern) receiver;

		Iterator<Operand> it = env.topFrame().operandStack.iterator();
		Reference charSeq_ref = ref(it.next());
		if (charSeq_ref instanceof StringReference) {
			StringReference charSeq_str_ref = (StringReference) charSeq_ref;
			symb_input = charSeq_str_ref.getStringExpression();
		} else {
			symb_input = null;
		}
	}

	@Override
	public void CALL_RESULT(Object res) {
		if (res == null)
			return;

		Matcher conc_matcher = (Matcher) res;
		NonNullReference symb_matcher = (NonNullReference) env.topFrame().operandStack
				.peekRef();

		if (symb_input != null) {
			env.heap.putField(Types.JAVA_UTIL_REGEX_MATCHER,
					SymbolicHeap.$MATCHER_INPUT, conc_matcher, symb_matcher,
					symb_input);
		}

	}

}
