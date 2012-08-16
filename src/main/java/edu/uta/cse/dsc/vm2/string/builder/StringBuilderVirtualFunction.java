package edu.uta.cse.dsc.vm2.string.builder;

import static edu.uta.cse.dsc.vm2.string.builder.StringBuilderConstants.JAVA_LANG_STRING_BUILDER;
import static edu.uta.cse.dsc.vm2.string.builder.StringBuilderConstants.STRING_BUILDER_CONTENTS;

import org.evosuite.symbolic.expr.StringBuilderExpression;
import org.evosuite.symbolic.expr.StringExpression;

import edu.uta.cse.dsc.vm2.NonNullReference;
import edu.uta.cse.dsc.vm2.SymbolicEnvironment;
import edu.uta.cse.dsc.vm2.string.VirtualFunction;

public abstract class StringBuilderVirtualFunction extends VirtualFunction {

	public StringBuilderVirtualFunction(SymbolicEnvironment env, String name,
			String desc) {
		super(env, StringBuilderConstants.JAVA_LANG_STRING_BUILDER, name, desc);
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

	protected StringBuilderExpression stringBuilderExpr;
	protected NonNullReference symb_receiver;

	/**
	 * This method should not consume the symbolic arguments in the stack
	 * operand. The disposal of these arguments will be done by RETURN,
	 * CALL_RESULT or HANDLER_BEGIN
	 * 
	 * @param receiver
	 * 
	 */
	protected abstract void INVOKEVIRTUAL(StringBuilder receiver);

	protected StringBuilderExpression getStringBuilderExpression(
			StringBuilder conc_receiver, NonNullReference symb_receiver) {

		StringExpression strExpr = this.env.heap.getField(
				JAVA_LANG_STRING_BUILDER, STRING_BUILDER_CONTENTS,
				conc_receiver, symb_receiver, conc_receiver.toString());

		if (!(strExpr instanceof StringBuilderExpression)) {
			return new StringBuilderExpression(strExpr);
		} else {
			return (StringBuilderExpression) strExpr;
		}
	}

	protected void replaceRefTop(NonNullReference ref) {
		this.env.topFrame().operandStack.popRef();
		this.env.topFrame().operandStack.pushRef(ref);
	}

}
