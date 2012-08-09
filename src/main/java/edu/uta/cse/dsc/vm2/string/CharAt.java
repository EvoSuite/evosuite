package edu.uta.cse.dsc.vm2.string;

import java.util.Iterator;

import org.evosuite.symbolic.expr.IntegerExpression;
import org.evosuite.symbolic.expr.Operator;
import org.evosuite.symbolic.expr.StringBinaryExpression;
import org.evosuite.symbolic.expr.StringToIntCast;

import edu.uta.cse.dsc.vm2.Operand;
import edu.uta.cse.dsc.vm2.SymbolicEnvironment;

public final class CharAt extends StringFunction {

	private static final String FUNCTION_NAME = "charAt";

	private IntegerExpression indexExpr;

	public CharAt(SymbolicEnvironment env) {
		super(env, FUNCTION_NAME, StringFunction.INT_TO_CHAR_DESCRIPTOR);
	}

	@Override
	protected void INVOKEVIRTUAL(String receiver) {
		Iterator<Operand> it = env.topFrame().operandStack.iterator();
		this.indexExpr = bv32(it.next());
		this.stringReceiverExpr = stringRef(it.next());

		int indexConcrete = ((Long) this.indexExpr.getConcreteValue())
				.intValue();
		if (indexConcrete < 0) {
			throwException(new StringIndexOutOfBoundsException());
			return;
		}
		if (indexConcrete > receiver.length()) {
			throwException(new StringIndexOutOfBoundsException());
			return;
		}

	}

	@Override
	public void CALL_RESULT(int res) {
		if (stringReceiverExpr.containsSymbolicVariable()
				|| indexExpr.containsSymbolicVariable()) {
			StringBinaryExpression strBExpr = new StringBinaryExpression(
					stringReceiverExpr, Operator.CHARAT, indexExpr,
					Character.toString((char) res));
			StringToIntCast castExpr = new StringToIntCast(strBExpr, (long) res);
			replaceBv32Top(castExpr);
		} else {
			// do nothing
		}
	}
}
