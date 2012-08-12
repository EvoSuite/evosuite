package edu.uta.cse.dsc.vm2.string.builder;

import org.evosuite.symbolic.expr.StringBuilderExpression;
import org.evosuite.symbolic.expr.StringExpression;

import edu.uta.cse.dsc.vm2.NonNullReference;
import edu.uta.cse.dsc.vm2.Reference;
import edu.uta.cse.dsc.vm2.SymbolicEnvironment;
import edu.uta.cse.dsc.vm2.string.StringFunction;

public final class SB_ToString extends StringBuilderVirtualFunction {

	private static final String FUNCTION_NAME = "toString";

	public SB_ToString(SymbolicEnvironment env) {
		super(env, FUNCTION_NAME, StringFunction.TO_STR_DESCRIPTOR);
	}

	@Override
	protected void INVOKEVIRTUAL(StringBuilder receiver) {
		Reference ref = this.env.topFrame().operandStack.peekRef();
		// the reference can not be null at this point
		NonNullReference strBuilderRef = (NonNullReference) ref;

		// get from symbolic heap (it could be null if no symbolic expression
		// was saved)
		this.stringBuilderExpr = (StringBuilderExpression) this.env
				.getHeap(SB_Init.STRING_BUILDER_CONTENTS,
						strBuilderRef);

	}

	@Override
	public void CALL_RESULT(Object res) {
		if (this.stringBuilderExpr != null) {
			StringExpression stringExpr = (StringExpression) this.stringBuilderExpr
					.getExpr();
			this.replaceStrRefTop(stringExpr);
		}

	}
}
