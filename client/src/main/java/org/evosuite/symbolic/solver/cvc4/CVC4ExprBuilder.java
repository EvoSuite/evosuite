package org.evosuite.symbolic.solver.cvc4;

import org.evosuite.symbolic.solver.SmtLibExprBuilder;

public abstract class CVC4ExprBuilder extends SmtLibExprBuilder {

	public static String mkBV2Int(String bvExpr) {
		String bv2nat = mkBV2Nat(bvExpr);
		return bv2nat;
	}

	public static String mkStringConstant(String str) {
		return "\"" + str + "\"";
	}
}
