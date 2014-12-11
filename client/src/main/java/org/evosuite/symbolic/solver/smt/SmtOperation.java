package org.evosuite.symbolic.solver.smt;

import java.util.Arrays;

public final class SmtOperation extends SmtExpr {

	public enum Operator {
		MUL("*"), //
		MINUS("-"), //
		ADD("+"), //
		MOD("mod"), //
		INT2BV32("(_ int2bv 32)"), //
		BVOR("bvor"), //
		BV2Nat("bv2nat"), //
		BVAND("bvand"), //
		BVXOR("bvxor"), //
		BV2INT("bv2int"), //
		BVSHL("bvshl"), //
		BVASHR("bvashr"), //
		BVLSHR("bvlshr"), //
		GT(">"), //
		ITE("ite"), // 
		LT("<"), // 
		GE(">="), // 
		REAL2INT("to_int"), // 
		INT2REAL("to_real"), //
		DIV("/"), //
		STR_SUBSTR("str.substr"), // 
		STR_REPLACE("str.replace"), //
		STR_INDEXOF("str.indexof"), //
		EQ("="), //
		STR_CONCAT("str.++"), //
		INT_TO_STR("int.to.str"), //
		STR_SUFFIXOF("str.suffixof"), //
		STR_CONTAINS("str.contains"), //
		STR_AT("str.at"), //
		CHAR_TO_INT("char_to_int"), //
		STR_PREFIXOF("str.prefixof"), //
		INT_TO_CHAR("int_to_char"), //
		STR_LEN("str.len"), //
		LE("<="), //
		NOT("not"), //
		STR_TO_INT("str.to.int");

		private final String rep;

		private Operator(String rep) {
			this.rep = rep;
		}

		@Override
		public String toString() {
			return rep;
		}
	}

	private final Operator operator;
	private final SmtExpr[] arguments;

	private final boolean hasSymbolicValues;

	/**
	 * Unary operation
	 * 
	 * @param op
	 * @param arg
	 */
	public SmtOperation(Operator op, SmtExpr... arg) {
		this.operator = op;
		this.arguments = arg;
		this.hasSymbolicValues = hasSymbolicValue(arg);
	}

	@Override
	public <K, V> K accept(SmtExprVisitor<K, V> v, V arg) {
		return v.visit(this, arg);
	}

	public SmtExpr[] getArguments() {
		return arguments;
	}

	public Operator getOperator() {
		return operator;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(arguments);
		result = prime * result
				+ ((operator == null) ? 0 : operator.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SmtOperation other = (SmtOperation) obj;
		if (!Arrays.equals(arguments, other.arguments))
			return false;
		if (operator != other.operator)
			return false;
		return true;
	}

	@Override
	public boolean isSymbolic() {
		return hasSymbolicValues;
	}

	private static boolean hasSymbolicValue(SmtExpr[] arguments) {
		for (SmtExpr smtExpr : arguments) {
			if (smtExpr.isSymbolic()) {
				return true;
			}
		}
		return false;
	}

}
