package org.evosuite.symbolic.vm;

import org.evosuite.symbolic.expr.bv.IntegerValue;

/**
 * 
 * @author galeotti
 *
 */
public interface IntegerOperand extends Operand {

	public IntegerValue getIntegerExpression();
}
