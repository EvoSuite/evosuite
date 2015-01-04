package org.evosuite.symbolic.vm.wrappers;

import org.evosuite.symbolic.expr.Operator;
import org.evosuite.symbolic.expr.bv.IntegerUnaryExpression;
import org.evosuite.symbolic.expr.bv.IntegerValue;
import org.evosuite.symbolic.vm.SymbolicEnvironment;
import org.evosuite.symbolic.vm.SymbolicFunction;

public final class Character_isLetter extends SymbolicFunction {

	private final static String IS_LETTER = "isLetter";

	public Character_isLetter(SymbolicEnvironment env) {
		super(env, Types.JAVA_LANG_CHARACTER, IS_LETTER, Types.C_TO_Z);
	}

	@Override
	public Object executeFunction() {
		IntegerValue charValueExpr = this.getSymbIntegerArgument(0);
		boolean res = this.getConcBooleanRetVal();

		if (charValueExpr.containsSymbolicVariable()) {

			long conV = res ? 1 : 0;

			IntegerUnaryExpression is_letter_expr = new IntegerUnaryExpression(
					charValueExpr, Operator.ISLETTER, conV);
			return is_letter_expr;
		} else {

			return this.getSymbIntegerRetVal();
		}
	}

}
