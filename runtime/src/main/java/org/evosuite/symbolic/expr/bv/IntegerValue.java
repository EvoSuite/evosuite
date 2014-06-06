package org.evosuite.symbolic.expr.bv;

import org.evosuite.symbolic.expr.Expression;
import org.evosuite.symbolic.expr.SymbolicValue;


public interface IntegerValue extends Expression<Long>, SymbolicValue {

	public Long execute();
}
