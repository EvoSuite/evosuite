/**
 * 
 */
package de.unisb.cs.st.evosuite.symbolic.smt.smtlib;

import org.smtlib.IExpr;
import org.smtlib.IExpr.IDecimal;
import org.smtlib.SMT;

/**
 * @author Gordon Fraser
 * 
 */
public class SMTExpr {

	private static SMT smt = new SMT();

	private static IExpr.IFactory efactory = smt.smtConfig.exprFactory;

	public static IExpr.ISymbol getSymbol(String name) {
		return smt.smtConfig.exprFactory.symbol(name);
	}

	public static IExpr getNumeral(long value) {
		return smt.smtConfig.exprFactory.numeral(value);
	}

	public static IDecimal getDecimal(String value) {
		return smt.smtConfig.exprFactory.decimal(value);
	}

	public static IExpr Not(IExpr expr) {
		return smt.smtConfig.exprFactory.fcn(efactory.symbol("not"), expr);
	}

	public static IExpr BvNot(IExpr expr) {
		return smt.smtConfig.exprFactory.fcn(efactory.symbol("bvnot"), expr);
	}

	public static IExpr BvNeg(IExpr expr) {
		return smt.smtConfig.exprFactory.fcn(efactory.symbol("bvneg"), expr);
	}

	public static IExpr BvAnd(IExpr expr1, IExpr expr2) {
		return smt.smtConfig.exprFactory.fcn(efactory.symbol("bvand"), expr1, expr2);
	}

	public static IExpr BvNand(IExpr expr1, IExpr expr2) {
		return smt.smtConfig.exprFactory.fcn(efactory.symbol("bvnand"), expr1, expr2);
	}

	public static IExpr BvOr(IExpr expr1, IExpr expr2) {
		return smt.smtConfig.exprFactory.fcn(efactory.symbol("bvor"), expr1, expr2);
	}

	public static IExpr BvNor(IExpr expr1, IExpr expr2) {
		return smt.smtConfig.exprFactory.fcn(efactory.symbol("bvnor"), expr1, expr2);
	}

	public static IExpr BvXor(IExpr expr1, IExpr expr2) {
		return smt.smtConfig.exprFactory.fcn(efactory.symbol("bvxor"), expr1, expr2);
	}

	public static IExpr BvXnor(IExpr expr1, IExpr expr2) {
		return smt.smtConfig.exprFactory.fcn(efactory.symbol("bvxnor"), expr1, expr2);
	}

	public static IExpr BvShl(IExpr expr1, IExpr expr2) {
		return smt.smtConfig.exprFactory.fcn(efactory.symbol("bvshl"), expr1, expr2);
	}

	public static IExpr BvShr(IExpr expr1, IExpr expr2) {
		return smt.smtConfig.exprFactory.fcn(efactory.symbol("bvshr"), expr1, expr2);
	}

	public static IExpr And(IExpr expr1, IExpr expr2) {
		return smt.smtConfig.exprFactory.fcn(efactory.symbol("and"), expr1, expr2);
	}

	public static IExpr Or(IExpr expr1, IExpr expr2) {
		return smt.smtConfig.exprFactory.fcn(efactory.symbol("or"), expr1, expr2);
	}

	public static IExpr Xor(IExpr expr1, IExpr expr2) {
		return smt.smtConfig.exprFactory.fcn(efactory.symbol("xor"), expr1, expr2);
	}

	public static IExpr Plus(IExpr expr1, IExpr expr2) {
		return smt.smtConfig.exprFactory.fcn(efactory.symbol("+"), expr1, expr2);
	}

	public static IExpr Minus(IExpr expr1, IExpr expr2) {
		return smt.smtConfig.exprFactory.fcn(efactory.symbol("-"), expr1, expr2);
	}

	public static IExpr BvPlus(IExpr expr1, IExpr expr2) {
		return smt.smtConfig.exprFactory.fcn(efactory.symbol("bvadd"), expr1, expr2);
	}

	public static IExpr BvMinus(IExpr expr1, IExpr expr2) {
		return smt.smtConfig.exprFactory.fcn(efactory.symbol("bvsub"), expr1, expr2);
	}

	public static IExpr Neg(IExpr expr) {
		return smt.smtConfig.exprFactory.fcn(efactory.symbol("-"), expr);
	}

	public static IExpr Mul(IExpr expr1, IExpr expr2) {
		return smt.smtConfig.exprFactory.fcn(efactory.symbol("*"), expr1, expr2);
	}

	public static IExpr Div(IExpr expr1, IExpr expr2) {
		return smt.smtConfig.exprFactory.fcn(efactory.symbol("div"), expr1, expr2);
	}

	public static IExpr BvMul(IExpr expr1, IExpr expr2) {
		return smt.smtConfig.exprFactory.fcn(efactory.symbol("bvmul"), expr1, expr2);
	}

	public static IExpr BvDiv(IExpr expr1, IExpr expr2) {
		return smt.smtConfig.exprFactory.fcn(efactory.symbol("bvudiv"), expr1, expr2);
	}

	public static IExpr Mod(IExpr expr1, IExpr expr2) {
		return smt.smtConfig.exprFactory.fcn(efactory.symbol("mod"), expr1, expr2);
	}

	public static IExpr Abs(IExpr expr1, IExpr expr2) {
		return smt.smtConfig.exprFactory.fcn(efactory.symbol("abs"), expr1, expr2);
	}

	public static IExpr Lt(IExpr expr1, IExpr expr2) {
		return smt.smtConfig.exprFactory.fcn(efactory.symbol("<"), expr1, expr2);
	}

	public static IExpr Lte(IExpr expr1, IExpr expr2) {
		return smt.smtConfig.exprFactory.fcn(efactory.symbol("<="), expr1, expr2);
	}

	public static IExpr Gt(IExpr expr1, IExpr expr2) {
		return smt.smtConfig.exprFactory.fcn(efactory.symbol(">"), expr1, expr2);
	}

	public static IExpr Gte(IExpr expr1, IExpr expr2) {
		return smt.smtConfig.exprFactory.fcn(efactory.symbol(">="), expr1, expr2);
	}

	public static IExpr Eq(IExpr expr1, IExpr expr2) {
		return smt.smtConfig.exprFactory.fcn(efactory.symbol("="), expr1, expr2);
	}

	public static IExpr BvLt(IExpr expr1, IExpr expr2) {
		return smt.smtConfig.exprFactory.fcn(efactory.symbol("bvlt"), expr1, expr2);
	}

	public static IExpr BvLte(IExpr expr1, IExpr expr2) {
		return smt.smtConfig.exprFactory.fcn(efactory.symbol("bvle"), expr1, expr2);
	}

	public static IExpr BvGt(IExpr expr1, IExpr expr2) {
		return smt.smtConfig.exprFactory.fcn(efactory.symbol("bvgt"), expr1, expr2);
	}

	public static IExpr BvGte(IExpr expr1, IExpr expr2) {
		return smt.smtConfig.exprFactory.fcn(efactory.symbol("bvge"), expr1, expr2);
	}

}
