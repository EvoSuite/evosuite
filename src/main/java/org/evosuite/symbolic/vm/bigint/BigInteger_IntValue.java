package org.evosuite.symbolic.vm.bigint;

import java.math.BigInteger;

import org.evosuite.symbolic.expr.bv.IntegerValue;
import org.evosuite.symbolic.vm.Function;
import org.evosuite.symbolic.vm.NonNullReference;
import org.evosuite.symbolic.vm.SymbolicEnvironment;
import org.evosuite.symbolic.vm.SymbolicHeap;

public final class BigInteger_IntValue extends Function {

	private static final String INT_VALUE = "intValue";

	public BigInteger_IntValue(SymbolicEnvironment env) {
		super(env, Types.JAVA_MATH_BIG_INTEGER, INT_VALUE, Types.TO_INT);
	}

	private BigInteger conc_big_integer;
	private NonNullReference symb_big_integer;

	@Override
	public void INVOKEVIRTUAL(Object receiver) {
		if (receiver != null) {
			conc_big_integer = (BigInteger) receiver;
			symb_big_integer = (NonNullReference) this.env.topFrame().operandStack
					.peekRef();
		} else {
			conc_big_integer = null;
			symb_big_integer = null;
		}

	}

	@Override
	public void CALL_RESULT(int res) {

		if (conc_big_integer != null) {

			IntegerValue integer_expr = this.env.heap.getField(
					Types.JAVA_MATH_BIG_INTEGER,
					SymbolicHeap.$BIG_INTEGER_CONTENTS, conc_big_integer,
					symb_big_integer, res);

			this.replaceTopBv32(integer_expr);
		}

	}

}
