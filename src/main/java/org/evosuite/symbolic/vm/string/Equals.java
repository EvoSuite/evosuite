package org.evosuite.symbolic.vm.string;

import java.util.Iterator;

import org.evosuite.symbolic.expr.Operator;
import org.evosuite.symbolic.expr.bv.StringComparison;
import org.evosuite.symbolic.expr.bv.StringToIntegerCast;
import org.evosuite.symbolic.expr.str.StringValue;
import org.evosuite.symbolic.vm.NonNullReference;
import org.evosuite.symbolic.vm.NullReference;
import org.evosuite.symbolic.vm.Operand;
import org.evosuite.symbolic.vm.Reference;
import org.evosuite.symbolic.vm.SymbolicEnvironment;
import org.evosuite.symbolic.vm.SymbolicHeap;

public final class Equals extends StringFunction {

	private static final String EQUALS = "equals";
	private StringValue strExpr;

	public Equals(SymbolicEnvironment env) {
		super(env, EQUALS, Types.OBJECT_TO_BOOL_DESCRIPTOR);
	}

	@Override
	protected void INVOKEVIRTUAL_String(String receiver) {
		Iterator<Operand> it = env.topFrame().operandStack.iterator();
		ref(it.next()); // discard argument
		this.stringReceiverExpr = getStringExpression(it.next()); // get
																	// receiver

	}

	@Override
	public void CALL_RESULT(boolean res) {
		if (this.strExpr != null
				&& (stringReceiverExpr.containsSymbolicVariable() || strExpr
						.containsSymbolicVariable())) {
			int conV = res ? 1 : 0;
			StringComparison strBExpr = new StringComparison(
					stringReceiverExpr, Operator.EQUALS, strExpr, (long) conV);
			this.replaceTopBv32(strBExpr);
		} else {
			// do nothing (concrete value only)
		}

	}

	@Override
	public void CALLER_STACK_PARAM(int nr, int calleeLocalsIndex, Object value) {

		Reference ref = this.env.topFrame().operandStack.peekRef();
		if (ref instanceof NullReference) {
			this.strExpr = null;
		} else {
			NonNullReference symb_receiver = (NonNullReference) ref;
			if (symb_receiver.isString()) {
				String conc_receiver = (String) value;
				this.strExpr = env.heap.getField(Types.JAVA_LANG_STRING,
						SymbolicHeap.$STRING_VALUE, conc_receiver,
						symb_receiver, conc_receiver);
			} else {
				this.strExpr = null; // equals(!String) returns false anyway
			}
		}
	}
}
