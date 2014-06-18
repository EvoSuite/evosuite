package org.evosuite.symbolic.vm;

import org.evosuite.symbolic.expr.fp.RealValue;

/**
 * 
 * @author galeotti
 *
 */
public interface RealOperand extends Operand {

	public RealValue getRealExpression();
}
