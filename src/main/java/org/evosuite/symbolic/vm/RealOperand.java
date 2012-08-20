package org.evosuite.symbolic.vm;

import org.evosuite.symbolic.expr.RealExpression;

/**
 * 
 * @author galeotti
 *
 */
public interface RealOperand extends Operand {

	public RealExpression getRealExpression();
}
