package org.evosuite.symbolic.expr.bv;

import org.evosuite.symbolic.expr.Expression;

public interface IntegerValue extends Expression<Long>, SymbolicValue {

	public Long execute();
}
