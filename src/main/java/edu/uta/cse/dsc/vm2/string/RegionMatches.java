package edu.uta.cse.dsc.vm2.string;

import java.util.ArrayList;
import java.util.Iterator;

import org.evosuite.symbolic.expr.Expression;
import org.evosuite.symbolic.expr.IntegerExpression;
import org.evosuite.symbolic.expr.Operator;
import org.evosuite.symbolic.expr.StringExpression;
import org.evosuite.symbolic.expr.StringMultipleComparison;
import org.evosuite.symbolic.expr.StringToIntCast;

import edu.uta.cse.dsc.vm2.Operand;
import edu.uta.cse.dsc.vm2.SymbolicEnvironment;

public final class RegionMatches extends StringFunction {

	private static final String FUNCTION_NAME = "regionMatches";

	private IntegerExpression lenExpr;
	private IntegerExpression ooffsetExpr;
	private StringExpression otherExpr;
	private IntegerExpression toffsetExpr;
	private IntegerExpression ignoreCaseExpr;

	public RegionMatches(SymbolicEnvironment env) {
		super(env, FUNCTION_NAME,
				StringFunction.BOOL_INT_STR_INT_INT_TO_BOOL_DESCRIPTOR);
	}

	@Override
	protected void INVOKEVIRTUAL(String receiver) {
		/* STUB */
	}

	@Override
	public void INVOKEVIRTUAL() {
		Iterator<Operand> it = env.topFrame().operandStack.iterator();
		lenExpr = bv32(it.next());
		ooffsetExpr = bv32(it.next());
		otherExpr = operandToStringRef(it.next());
		toffsetExpr = bv32(it.next());
		ignoreCaseExpr = bv32(it.next());
		stringReceiverExpr = operandToStringRef(it.next());

	}

	@Override
	public void CALL_RESULT(boolean res) {

		if (stringReceiverExpr.containsSymbolicVariable()
				|| ignoreCaseExpr.containsSymbolicVariable()
				|| toffsetExpr.containsSymbolicVariable()
				|| otherExpr.containsSymbolicVariable()
				|| ooffsetExpr.containsSymbolicVariable()
				|| lenExpr.containsSymbolicVariable()) {

			ArrayList<Expression<?>> other = new ArrayList<Expression<?>>();
			other.add(this.toffsetExpr);
			other.add(ooffsetExpr);
			other.add(lenExpr);
			other.add(ignoreCaseExpr);
			int conV = res ? 1 : 0;

			StringMultipleComparison strComp = new StringMultipleComparison(
					stringReceiverExpr, Operator.REGIONMATCHES, otherExpr,
					other, (long) conV);
			StringToIntCast castExpr = new StringToIntCast(strComp, (long) conV);
			this.replaceBv32Top(castExpr);
		}
	}

}
