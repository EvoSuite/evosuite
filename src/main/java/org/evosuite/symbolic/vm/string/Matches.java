package org.evosuite.symbolic.vm.string;

import org.evosuite.symbolic.expr.Operator;
import org.evosuite.symbolic.expr.bv.StringBinaryComparison;
import org.evosuite.symbolic.expr.str.StringConstant;
import org.evosuite.symbolic.expr.str.StringValue;
import org.evosuite.symbolic.vm.ExpressionFactory;
import org.evosuite.symbolic.vm.NonNullReference;
import org.evosuite.symbolic.vm.SymbolicEnvironment;
import org.evosuite.symbolic.vm.SymbolicFunction;
import org.evosuite.symbolic.vm.SymbolicHeap;

public final class Matches extends SymbolicFunction {

	private static final String MATCHES = "matches";

	public Matches(SymbolicEnvironment env) {
		super(env, Types.JAVA_LANG_STRING, MATCHES,
				Types.STR_TO_BOOL_DESCRIPTOR);
	}

	@Override
	public Object executeFunction() {

		// receiver
		NonNullReference symb_receiver = this.getSymbReceiver();
		String conc_receiver = (String) this.getConcReceiver();

		// argument
		String conc_argument = (String) this.getConcArgument(0);

		StringValue right_expr = env.heap.getField(Types.JAVA_LANG_STRING,
				SymbolicHeap.$STRING_VALUE, conc_receiver, symb_receiver,
				conc_receiver);

		// return val
		boolean res = this.getConcBooleanRetVal();

		if (right_expr.containsSymbolicVariable()) {
			StringConstant left_expr = ExpressionFactory
					.buildNewStringConstant(conc_argument);
			int conV = res ? 1 : 0;

			StringBinaryComparison strBExpr = new StringBinaryComparison(left_expr,
					Operator.PATTERNMATCHES, right_expr, (long) conV);

			return strBExpr;
		}

		return this.getSymbIntegerRetVal();
	}
}
