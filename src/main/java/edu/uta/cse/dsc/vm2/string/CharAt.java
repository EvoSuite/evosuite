package edu.uta.cse.dsc.vm2.string;

import java.util.Iterator;

import org.evosuite.symbolic.expr.IntegerExpression;
import org.evosuite.symbolic.expr.Operator;
import org.evosuite.symbolic.expr.StringBinaryExpression;
import org.evosuite.symbolic.expr.StringToIntCast;

import edu.uta.cse.dsc.vm2.Operand;
import edu.uta.cse.dsc.vm2.SymbolicEnvironment;

public final class CharAt extends StringFunction {

	private static final String CHAR_AT = "charAt";

	private IntegerExpression indexExpr;

	public CharAt(SymbolicEnvironment env) {
		super(env, CHAR_AT, Types.INT_TO_CHAR_DESCRIPTOR);
	}

	@Override
	protected void INVOKEVIRTUAL_String(String receiver) {
		Iterator<Operand> it = env.topFrame().operandStack.iterator();
		this.indexExpr = bv32(it.next());
		this.stringReceiverExpr = operandToStringExpression(it.next());
	}

	@Override
	public void CALL_RESULT(int res) {

		if (stringReceiverExpr.containsSymbolicVariable()
				|| indexExpr.containsSymbolicVariable()) {

			String intToString = Long.toString(res);
			StringBinaryExpression strBExpr = new StringBinaryExpression(
					stringReceiverExpr, Operator.CHARAT, indexExpr, intToString);
			StringToIntCast castExpr = new StringToIntCast(strBExpr, (long) res);
			replaceTopBv32(castExpr);

		} else {
			// do nothing
		}
	}
}
