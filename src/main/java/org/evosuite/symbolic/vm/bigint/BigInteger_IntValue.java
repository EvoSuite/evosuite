package org.evosuite.symbolic.vm.bigint;

import java.math.BigInteger;

import org.evosuite.symbolic.expr.bv.IntegerValue;
import org.evosuite.symbolic.vm.NonNullReference;
import org.evosuite.symbolic.vm.SymbolicFunction;
import org.evosuite.symbolic.vm.SymbolicEnvironment;
import org.evosuite.symbolic.vm.SymbolicHeap;

public final class BigInteger_IntValue extends SymbolicFunction {

	private static final String INT_VALUE = "intValue";

	public BigInteger_IntValue(SymbolicEnvironment env) {
		super(env, Types.JAVA_MATH_BIG_INTEGER, INT_VALUE, Types.TO_INT);
	}


	@Override
	public Object executeFunction() {
		BigInteger conc_big_integer = (BigInteger) this.getConcReceiver();
		NonNullReference symb_big_integer = this.getSymbReceiver();
		int res =  this.getConcIntRetVal();
		
		IntegerValue integer_expr = this.env.heap.getField(
				Types.JAVA_MATH_BIG_INTEGER,
				SymbolicHeap.$BIG_INTEGER_CONTENTS, conc_big_integer,
				symb_big_integer, res);
		
		return integer_expr;
	}

}
