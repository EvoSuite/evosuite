package edu.uta.cse.dsc.vm2;

import org.evosuite.symbolic.expr.IntegerExpression;

public interface IntegerOperand extends Operand {

	public IntegerExpression getIntegerExpression();
}
