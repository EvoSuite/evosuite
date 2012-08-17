package edu.uta.cse.dsc.vm2.string;

import java.util.Iterator;

import org.evosuite.symbolic.expr.Operator;
import org.evosuite.symbolic.expr.StringComparison;
import org.evosuite.symbolic.expr.StringExpression;
import org.evosuite.symbolic.expr.StringToIntCast;

import edu.uta.cse.dsc.vm2.NullReference;
import edu.uta.cse.dsc.vm2.Operand;
import edu.uta.cse.dsc.vm2.Reference;
import edu.uta.cse.dsc.vm2.StringReference;
import edu.uta.cse.dsc.vm2.SymbolicEnvironment;

public final class Equals extends StringFunction {

	private static final String EQUALS = "equals";
	private StringExpression strExpr;

	public Equals(SymbolicEnvironment env) {
		super(env, EQUALS, Types.OBJECT_TO_BOOL_DESCRIPTOR);
	}

	@Override
	protected void INVOKEVIRTUAL_String(String receiver) {
		Iterator<Operand> it = env.topFrame().operandStack.iterator();
	
		Reference ref = ref(it.next());
		if (ref instanceof NullReference) {
			this.strExpr = null;
		} else {
			if (ref instanceof StringReference) {
				this.strExpr = ((StringReference) ref).getStringExpression();
			} else {
				this.strExpr = null; // equals(!String) returns false anyway
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
			this.replaceTopBv32(castExpr);
		} else {
			// do nothing (concrete value only)
		}

	}
}
