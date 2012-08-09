package edu.uta.cse.dsc.vm2.string;

import java.util.Iterator;

import org.evosuite.symbolic.expr.Operator;
import org.evosuite.symbolic.expr.StringBinaryExpression;
import org.evosuite.symbolic.expr.StringExpression;
import org.evosuite.symbolic.expr.StringToIntCast;

import edu.uta.cse.dsc.vm2.Operand;
import edu.uta.cse.dsc.vm2.ReferenceOperand;
import edu.uta.cse.dsc.vm2.StringReferenceOperand;
import edu.uta.cse.dsc.vm2.SymbolicEnvironment;

public final class EndsWith extends StringFunction {

	private static final String FUNCTION_NAME = "endsWith";
	private StringExpression strExpr;

	public EndsWith(SymbolicEnvironment env) {
		super(env, FUNCTION_NAME, StringFunction.STR_TO_BOOL_DESCRIPTOR);
	}

	@Override
	protected void INVOKEVIRTUAL(String receiver) {
		Iterator<Operand> it = env.topFrame().operandStack.iterator();
		ReferenceOperand strRef = ref(it.next());
		if (isNullRef(strRef)) {
			throwException(new NullPointerException());
			return;
		}
		this.strExpr = ((StringReferenceOperand) strRef).getStringExpression();
		this.stringReceiverExpr = stringRef(it.next());

	}

	@Override
	public void CALL_RESULT(boolean res) {
		if (stringReceiverExpr.containsSymbolicVariable()
				|| strExpr.containsSymbolicVariable()) {
			int conV = res ? 1 : 0;
			StringBinaryExpression strBExpr = new StringBinaryExpression(
					stringReceiverExpr, Operator.ENDSWITH, strExpr,
					Integer.toString(conV));
			StringToIntCast castExpr = new StringToIntCast(strBExpr,
					(long) conV);
			this.replaceBv32Top(castExpr);
		} else {
			// do nothing (concrete value only)
		}

	}
}
