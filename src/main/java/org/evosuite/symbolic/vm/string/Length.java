package org.evosuite.symbolic.vm.string;

import org.evosuite.symbolic.expr.Operator;
import org.evosuite.symbolic.expr.bv.StringUnaryToIntegerExpression;
import org.evosuite.symbolic.expr.str.StringValue;
import org.evosuite.symbolic.vm.NonNullReference;
import org.evosuite.symbolic.vm.SymbolicEnvironment;
import org.evosuite.symbolic.vm.SymbolicFunction;
import org.evosuite.symbolic.vm.SymbolicHeap;

public final class Length extends SymbolicFunction {

	private static final String LENGTH = "length";

	public Length(SymbolicEnvironment env) {
		super(env, Types.JAVA_LANG_STRING, LENGTH, Types.TO_INT_DESCRIPTOR);
	}

	@Override
	public Object executeFunction() {
		NonNullReference symb_str = this.getSymbReceiver();
		String conc_str = (String) this.getConcReceiver();
		int res = this.getConcIntRetVal();

		StringValue string_expr = env.heap.getField(Types.JAVA_LANG_STRING,
				SymbolicHeap.$STRING_VALUE, conc_str, symb_str, conc_str);

		if (string_expr.containsSymbolicVariable()) {
			StringUnaryToIntegerExpression strUnExpr = new StringUnaryToIntegerExpression(
					string_expr, Operator.LENGTH, (long) res);
			return strUnExpr;
		}

		return this.getSymbIntegerRetVal();
	}
}
