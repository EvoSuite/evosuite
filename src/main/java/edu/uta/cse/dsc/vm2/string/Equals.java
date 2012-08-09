package edu.uta.cse.dsc.vm2.string;

import java.util.Iterator;

import org.evosuite.symbolic.expr.Operator;
import org.evosuite.symbolic.expr.StringBinaryExpression;
import org.evosuite.symbolic.expr.StringComparison;
import org.evosuite.symbolic.expr.StringExpression;
import org.evosuite.symbolic.expr.StringToIntCast;

import edu.uta.cse.dsc.vm2.Operand;
import edu.uta.cse.dsc.vm2.ReferenceOperand;
import edu.uta.cse.dsc.vm2.StringReferenceOperand;
import edu.uta.cse.dsc.vm2.SymbolicEnvironment;

public final class Equals extends StringFunction {

	private static final String FUNCTION_NAME = "equals";
	private StringExpression strExpr;

	public Equals(SymbolicEnvironment env) {
		super(env, FUNCTION_NAME, StringFunction.OBJECT_TO_BOOL_DESCRIPTOR);
	}

	@Override
	protected void INVOKEVIRTUAL(String receiver) {
		Iterator<Operand> it = env.topFrame().operandStack.iterator();
		ReferenceOperand refOperand = ref(it.next());
		if (isNullRef(refOperand)) {
			this.strExpr = null;
		} else {
			if (refOperand instanceof StringReferenceOperand) {
				this.strExpr = ((StringReferenceOperand) refOperand)
						.getStringExpression();
			} else {
				this.strExpr = null;
			}
		}
		this.stringReceiverExpr = stringRef(it.next());

	}

	@Override
	public void CALL_RESULT(boolean res) {
		if (this.strExpr != null
				&& (stringReceiverExpr.containsSymbolicVariable() || strExpr
						.containsSymbolicVariable())) {
			int conV = res ? 1 : 0;
			StringComparison strBExpr = new StringComparison(
					stringReceiverExpr, Operator.EQUALS, strExpr, (long) conV);
			StringToIntCast castExpr = new StringToIntCast(strBExpr,
					(long) conV);
			this.replaceBv32Top(castExpr);
		} else {
			// do nothing (concrete value only)
		}

	}
}
