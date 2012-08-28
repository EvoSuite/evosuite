package org.evosuite.symbolic.vm.string;

import java.util.ArrayList;
import java.util.Iterator;

import org.evosuite.symbolic.expr.Expression;
import org.evosuite.symbolic.expr.IntegerExpression;
import org.evosuite.symbolic.expr.Operator;
import org.evosuite.symbolic.expr.StringExpression;
import org.evosuite.symbolic.expr.StringMultipleComparison;
import org.evosuite.symbolic.expr.StringToIntCast;
import org.evosuite.symbolic.vm.NullReference;
import org.evosuite.symbolic.vm.Operand;
import org.evosuite.symbolic.vm.Reference;
import org.evosuite.symbolic.vm.ReferenceOperand;
import org.evosuite.symbolic.vm.SymbolicEnvironment;

public final class RegionMatches extends StringFunction {

	private static final String REGION_MATCHES = "regionMatches";

	private IntegerExpression lenExpr;
	private IntegerExpression ooffsetExpr;
	private StringExpression otherExpr;
	private IntegerExpression toffsetExpr;
	private IntegerExpression ignoreCaseExpr;

	public RegionMatches(SymbolicEnvironment env) {
		super(env, REGION_MATCHES,
				Types.BOOL_INT_STR_INT_INT_TO_BOOL_DESCRIPTOR);
	}

	@Override
	public void INVOKEVIRTUAL_String(String receiver) {
		Iterator<Operand> it = env.topFrame().operandStack.iterator();
		lenExpr = bv32(it.next());
		ooffsetExpr = bv32(it.next());
		otherExpr = getStringExpression(it.next());
		toffsetExpr = bv32(it.next());
		ignoreCaseExpr = bv32(it.next());
		Operand receiver_operand = it.next();
		Reference receiver_ref = ((ReferenceOperand) receiver_operand)
				.getReference();
		if (receiver_ref instanceof NullReference) {
			return;
		}
		stringReceiverExpr = getStringExpression(receiver_operand);

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
			this.replaceTopBv32(strComp);
		}
	}

}
