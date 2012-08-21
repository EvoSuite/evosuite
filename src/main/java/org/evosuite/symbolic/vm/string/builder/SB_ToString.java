package org.evosuite.symbolic.vm.string.builder;

import org.evosuite.symbolic.expr.StringExpression;
import org.evosuite.symbolic.vm.NonNullReference;
import org.evosuite.symbolic.vm.SymbolicEnvironment;
import org.evosuite.symbolic.vm.string.Types;


public final class SB_ToString extends StringBuilderFunction {

	private static final String FUNCTION_NAME = "toString";

	public SB_ToString(SymbolicEnvironment env) {
		super(env, FUNCTION_NAME, Types.TO_STR_DESCRIPTOR);
	}

	private StringBuilder conc_str_builder;

	@Override
	protected void INVOKEVIRTUAL_StringBuilder(StringBuilder conc_receiver) {
		if (conc_receiver == null)
			return;
		// the reference can not be null at this point
		symb_receiver = (NonNullReference) this.env.topFrame().operandStack
				.peekRef();

		conc_str_builder = conc_receiver;

	}

	@Override
	public void CALL_RESULT(Object res) {

		// get from symbolic heap (it could be null if no symbolic expression
		// was saved)
		this.stringBuilderExpr = this.getStringBuilderExpression(conc_str_builder,
				this.symb_receiver);

		if (this.stringBuilderExpr.containsSymbolicVariable()) {
			StringExpression stringExpr = (StringExpression) this.stringBuilderExpr
					.getExpr();
			this.replaceStrRefTop(stringExpr);
		}

	}
}
