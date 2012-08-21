package org.evosuite.symbolic.vm.regex;

import static org.objectweb.asm.Type.BOOLEAN_TYPE;

import java.util.Iterator;
import java.util.regex.Pattern;

import org.evosuite.symbolic.expr.Operator;
import org.evosuite.symbolic.expr.StringComparison;
import org.evosuite.symbolic.expr.StringExpression;
import org.evosuite.symbolic.expr.StringToIntCast;
import org.evosuite.symbolic.vm.Function;
import org.evosuite.symbolic.vm.Operand;
import org.evosuite.symbolic.vm.Reference;
import org.evosuite.symbolic.vm.StringReference;
import org.evosuite.symbolic.vm.SymbolicEnvironment;
import org.objectweb.asm.Type;

/**
 * 
 * @author galeotti
 * 
 */
public final class Pattern_Matches extends Function {

	private static final String JAVA_UTIL_PATTERN = Pattern.class.getName()
			.replace(".", "/");

	private static final String MATCHES = "matches";

	private static final Type STR_TYPE = Type.getType(String.class);

	private static final Type CHARSEQ_TYPE = Type.getType(CharSequence.class);

	private static final String STR_CHARSEQ_TO_BOOLEAN = Type
			.getMethodDescriptor(BOOLEAN_TYPE, STR_TYPE, CHARSEQ_TYPE);

	public Pattern_Matches(SymbolicEnvironment env) {
		super(env, JAVA_UTIL_PATTERN, MATCHES, STR_CHARSEQ_TO_BOOLEAN);
	}

	@Override
	public void INVOKESTATIC() {
		Iterator<Operand> it = this.env.topFrame().operandStack.iterator();
		Reference input_ref = ref(it.next());
		if (input_ref instanceof StringReference) {
			StringReference input_str_ref = (StringReference) input_ref;
			symb_input = input_str_ref.getStringExpression();
		} else {
			symb_input = null;
		}
		Reference regex_ref = ref(it.next());
		if (regex_ref instanceof StringReference) {
			StringReference regex_str_ref = (StringReference) regex_ref;
			symb_regex = regex_str_ref.getStringExpression();
		} else {
			symb_regex = null;
		}
	}

	private StringExpression symb_regex;
	private StringExpression symb_input;

	@Override
	public void CALL_RESULT(boolean res) {
		if (symb_input != null && symb_input.containsSymbolicVariable()) {

			int concrete_value = res ? 1 : 0;

			StringComparison strComp = new StringComparison(symb_regex,
					Operator.PATTERNMATCHES, symb_input, (long) concrete_value);
			StringToIntCast castExpr = new StringToIntCast(strComp,
					(long) concrete_value);

			replaceTopBv32(castExpr);
		}
	}

	@Override
	public void CALLER_STACK_PARAM(int nr, int calleeLocalsIndex, Object value) {
		/* STUB */
	}

}
