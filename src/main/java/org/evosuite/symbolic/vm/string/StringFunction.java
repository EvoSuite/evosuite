package org.evosuite.symbolic.vm.string;

import org.evosuite.symbolic.expr.StringExpression;
import org.evosuite.symbolic.vm.Function;
import org.evosuite.symbolic.vm.SymbolicEnvironment;


public abstract class StringFunction extends Function {

	private static final String JAVA_LANG_STRING = String.class.getName()
			.replace(".", "/");

	public StringFunction(SymbolicEnvironment env, String name, String desc) {
		super(env, JAVA_LANG_STRING, name, desc);
	}

	
	@Override
	public final void INVOKEVIRTUAL(Object receiver) {
		/**
		 * We do nothing if receiver is null since we assume HeapVM deals with
		 * the null pointer exception effect in the symbolic state.
		 */
		if (receiver == null) {
			return;
		}

		INVOKEVIRTUAL_String((String) receiver);
	}

	protected StringExpression stringReceiverExpr;

	/**
	 * This method should not consume the symbolic arguments in the stack
	 * operand. The disposal of these arguments will be done by RETURN,
	 * CALL_RESULT or HANDLER_BEGIN
	 * 
	 * @param receiver
	 * 
	 */
	protected void INVOKEVIRTUAL_String(String receiver) {
		/* STUB */
	}
}
