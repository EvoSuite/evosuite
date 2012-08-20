package org.evosuite.symbolic.vm;

import org.evosuite.symbolic.expr.IntegerExpression;

/**
 * 
 * @author galeotti
 *
 */
public interface IntegerOperand extends Operand {

	public IntegerExpression getIntegerExpression();
}
