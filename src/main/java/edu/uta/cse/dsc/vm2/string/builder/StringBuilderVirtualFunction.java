package edu.uta.cse.dsc.vm2.string.builder;

import org.evosuite.symbolic.expr.StringBuilderExpression;

import edu.uta.cse.dsc.vm2.SymbolicEnvironment;
import edu.uta.cse.dsc.vm2.string.StringFunctionCallVM;
import edu.uta.cse.dsc.vm2.string.VirtualFunction;

public abstract class StringBuilderVirtualFunction extends VirtualFunction {

	public StringBuilderVirtualFunction(SymbolicEnvironment env, String name,
			String desc) {
		super(env, StringFunctionCallVM.JAVA_LANG_STRING_BUILDER, name, desc);
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

		INVOKEVIRTUAL((StringBuilder) receiver);
	}

	protected StringBuilderExpression stringBuilderReceiverExpr;

	/**
	 * This method should not consume the symbolic arguments in the stack
	 * operand. The disposal of these arguments will be done by RETURN,
	 * CALL_RESULT or HANDLER_BEGIN
	 * 
	 * @param receiver
	 * 
	 */
	protected abstract void INVOKEVIRTUAL(StringBuilder receiver);

}
