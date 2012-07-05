/**
 * Copyright (C) 2011,2012 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Public License for more details.
 *
 * You should have received a copy of the GNU Public License along with
 * EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
/**
 * 
 */
package org.evosuite.symbolic.smt.cvc3;

import java.util.LinkedList;
import java.util.List;

import cvc3.Expr;
import cvc3.ValidityChecker;

/**
 * <p>CVC3Expr class.</p>
 *
 * @author fraser
 */
public class CVC3Expr {

	private final ValidityChecker vc;

	/**
	 * <p>Constructor for CVC3Expr.</p>
	 *
	 * @param vc a {@link cvc3.ValidityChecker} object.
	 */
	public CVC3Expr(ValidityChecker vc) {
		this.vc = vc;
	}

	/**
	 * <p>getSymbol</p>
	 *
	 * @param name a {@link java.lang.String} object.
	 * @return a {@link cvc3.Expr} object.
	 */
	public Expr getSymbol(String name) {
		return vc.varExpr(name, vc.intType());
	}

	/**
	 * <p>getNumeral</p>
	 *
	 * @param num a int.
	 * @return a {@link cvc3.Expr} object.
	 */
	public Expr getNumeral(int num) {
		return vc.ratExpr(num);
	}

	/**
	 * <p>getNumeral</p>
	 *
	 * @param num a long.
	 * @return a {@link cvc3.Expr} object.
	 */
	public Expr getNumeral(long num) {
		if (num > Integer.MAX_VALUE || num < Integer.MIN_VALUE)
			return vc.ratExpr(Long.toString(num));
		else
			return getNumeral((int) num);
	}

	/**
	 * <p>Not</p>
	 *
	 * @param expr a {@link cvc3.Expr} object.
	 * @return a {@link cvc3.Expr} object.
	 */
	public Expr Not(Expr expr) {
		return vc.notExpr(expr);
	}

	/**
	 * <p>And</p>
	 *
	 * @param expr1 a {@link cvc3.Expr} object.
	 * @param expr2 a {@link cvc3.Expr} object.
	 * @return a {@link cvc3.Expr} object.
	 */
	public Expr And(Expr expr1, Expr expr2) {
		return vc.andExpr(expr1, expr2);
	}

	/**
	 * <p>Or</p>
	 *
	 * @param expr1 a {@link cvc3.Expr} object.
	 * @param expr2 a {@link cvc3.Expr} object.
	 * @return a {@link cvc3.Expr} object.
	 */
	public Expr Or(Expr expr1, Expr expr2) {
		return vc.orExpr(expr1, expr2);
	}

	/**
	 * <p>Xor</p>
	 *
	 * @param expr1 a {@link cvc3.Expr} object.
	 * @param expr2 a {@link cvc3.Expr} object.
	 * @return a {@link cvc3.Expr} object.
	 */
	public Expr Xor(Expr expr1, Expr expr2) {
		return vc.orExpr(vc.andExpr(vc.notExpr(expr1), expr2),
		                 vc.andExpr(expr1, vc.notExpr(expr2)));
	}

	/**
	 * <p>Neg</p>
	 *
	 * @param expr a {@link cvc3.Expr} object.
	 * @return a {@link cvc3.Expr} object.
	 */
	public Expr Neg(Expr expr) {
		return vc.uminusExpr(expr);
	}

	/**
	 * <p>Plus</p>
	 *
	 * @param expr1 a {@link cvc3.Expr} object.
	 * @param expr2 a {@link cvc3.Expr} object.
	 * @return a {@link cvc3.Expr} object.
	 */
	public Expr Plus(Expr expr1, Expr expr2) {
		return vc.plusExpr(expr1, expr2);
	}

	/**
	 * <p>Minus</p>
	 *
	 * @param expr1 a {@link cvc3.Expr} object.
	 * @param expr2 a {@link cvc3.Expr} object.
	 * @return a {@link cvc3.Expr} object.
	 */
	public Expr Minus(Expr expr1, Expr expr2) {
		return vc.minusExpr(expr1, expr2);
	}

	/**
	 * <p>Mul</p>
	 *
	 * @param expr1 a {@link cvc3.Expr} object.
	 * @param expr2 a {@link cvc3.Expr} object.
	 * @return a {@link cvc3.Expr} object.
	 */
	public Expr Mul(Expr expr1, Expr expr2) {
		return vc.multExpr(expr1, expr2);
	}

	/**
	 * <p>Div</p>
	 *
	 * @param expr1 a {@link cvc3.Expr} object.
	 * @param expr2 a {@link cvc3.Expr} object.
	 * @return a {@link cvc3.Expr} object.
	 */
	public Expr Div(Expr expr1, Expr expr2) {
		return vc.divideExpr(expr1, expr2);
	}

	/**
	 * <p>Pow</p>
	 *
	 * @param expr1 a {@link cvc3.Expr} object.
	 * @param expr2 a {@link cvc3.Expr} object.
	 * @return a {@link cvc3.Expr} object.
	 */
	public Expr Pow(Expr expr1, Expr expr2) {
		return vc.powExpr(expr1, expr2);
	}

	/**
	 * <p>Lt</p>
	 *
	 * @param expr1 a {@link cvc3.Expr} object.
	 * @param expr2 a {@link cvc3.Expr} object.
	 * @return a {@link cvc3.Expr} object.
	 */
	public Expr Lt(Expr expr1, Expr expr2) {
		return vc.ltExpr(expr1, expr2);
	}

	/**
	 * <p>Lte</p>
	 *
	 * @param expr1 a {@link cvc3.Expr} object.
	 * @param expr2 a {@link cvc3.Expr} object.
	 * @return a {@link cvc3.Expr} object.
	 */
	public Expr Lte(Expr expr1, Expr expr2) {
		return vc.leExpr(expr1, expr2);
	}

	/**
	 * <p>Gt</p>
	 *
	 * @param expr1 a {@link cvc3.Expr} object.
	 * @param expr2 a {@link cvc3.Expr} object.
	 * @return a {@link cvc3.Expr} object.
	 */
	public Expr Gt(Expr expr1, Expr expr2) {
		return vc.gtExpr(expr1, expr2);
	}

	/**
	 * <p>Gte</p>
	 *
	 * @param expr1 a {@link cvc3.Expr} object.
	 * @param expr2 a {@link cvc3.Expr} object.
	 * @return a {@link cvc3.Expr} object.
	 */
	public Expr Gte(Expr expr1, Expr expr2) {
		return vc.geExpr(expr1, expr2);
	}

	/**
	 * <p>Implies</p>
	 *
	 * @param expr1 a {@link cvc3.Expr} object.
	 * @param expr2 a {@link cvc3.Expr} object.
	 * @return a {@link cvc3.Expr} object.
	 */
	public Expr Implies(Expr expr1, Expr expr2) {
		return vc.impliesExpr(expr1, expr2);
	}

	/**
	 * <p>Eq</p>
	 *
	 * @param expr1 a {@link cvc3.Expr} object.
	 * @param expr2 a {@link cvc3.Expr} object.
	 * @return a {@link cvc3.Expr} object.
	 */
	public Expr Eq(Expr expr1, Expr expr2) {
		return vc.eqExpr(expr1, expr2);
	}

	/**
	 * <p>Neq</p>
	 *
	 * @param expr1 a {@link cvc3.Expr} object.
	 * @param expr2 a {@link cvc3.Expr} object.
	 * @return a {@link cvc3.Expr} object.
	 */
	public Expr Neq(Expr expr1, Expr expr2) {
		return vc.notExpr(vc.eqExpr(expr1, expr2));
	}

	/**
	 * <p>Ite</p>
	 *
	 * @param expr1 a {@link cvc3.Expr} object.
	 * @param expr2 a {@link cvc3.Expr} object.
	 * @param expr3 a {@link cvc3.Expr} object.
	 * @return a {@link cvc3.Expr} object.
	 */
	public Expr Ite(Expr expr1, Expr expr2, Expr expr3) {
		return vc.iteExpr(expr1, expr2, expr3);
	}

	//-----------------------------------------------------
	// Bitvector expressions

	/**
	 * <p>BvNeg</p>
	 *
	 * @param expr a {@link cvc3.Expr} object.
	 * @return a {@link cvc3.Expr} object.
	 */
	public Expr BvNeg(Expr expr) {
		return vc.newBVNegExpr(expr);
	}

	/**
	 * <p>BvPlus</p>
	 *
	 * @param numbits a int.
	 * @param expr1 a {@link cvc3.Expr} object.
	 * @param expr2 a {@link cvc3.Expr} object.
	 * @return a {@link cvc3.Expr} object.
	 */
	public Expr BvPlus(int numbits, Expr expr1, Expr expr2) {
		List<Expr> exprs = new LinkedList<Expr>();
		exprs.add(expr1);
		exprs.add(expr2);
		return vc.newBVPlusExpr(numbits, exprs);
	}

	/**
	 * <p>BvMinus</p>
	 *
	 * @param expr1 a {@link cvc3.Expr} object.
	 * @param expr2 a {@link cvc3.Expr} object.
	 * @return a {@link cvc3.Expr} object.
	 */
	public Expr BvMinus(Expr expr1, Expr expr2) {
		return vc.newBVSubExpr(expr1, expr2);
	}

	/**
	 * <p>BvMul</p>
	 *
	 * @param numbits a int.
	 * @param expr1 a {@link cvc3.Expr} object.
	 * @param expr2 a {@link cvc3.Expr} object.
	 * @return a {@link cvc3.Expr} object.
	 */
	public Expr BvMul(int numbits, Expr expr1, Expr expr2) {
		return vc.newBVMultExpr(numbits, expr1, expr2);
	}

	/**
	 * <p>BvSDiv</p>
	 *
	 * @param expr1 a {@link cvc3.Expr} object.
	 * @param expr2 a {@link cvc3.Expr} object.
	 * @return a {@link cvc3.Expr} object.
	 */
	public Expr BvSDiv(Expr expr1, Expr expr2) {
		return vc.newBVSDivExpr(expr1, expr2);
	}

	/**
	 * <p>BvUDiv</p>
	 *
	 * @param expr1 a {@link cvc3.Expr} object.
	 * @param expr2 a {@link cvc3.Expr} object.
	 * @return a {@link cvc3.Expr} object.
	 */
	public Expr BvUDiv(Expr expr1, Expr expr2) {
		return vc.newBVUDivExpr(expr1, expr2);
	}

	/**
	 * <p>BvOr</p>
	 *
	 * @param expr1 a {@link cvc3.Expr} object.
	 * @param expr2 a {@link cvc3.Expr} object.
	 * @return a {@link cvc3.Expr} object.
	 */
	public Expr BvOr(Expr expr1, Expr expr2) {
		return vc.newBVOrExpr(expr1, expr2);
	}

	/**
	 * <p>BvAnd</p>
	 *
	 * @param expr1 a {@link cvc3.Expr} object.
	 * @param expr2 a {@link cvc3.Expr} object.
	 * @return a {@link cvc3.Expr} object.
	 */
	public Expr BvAnd(Expr expr1, Expr expr2) {
		return vc.newBVAndExpr(expr1, expr2);
	}

	/**
	 * <p>BvXor</p>
	 *
	 * @param expr1 a {@link cvc3.Expr} object.
	 * @param expr2 a {@link cvc3.Expr} object.
	 * @return a {@link cvc3.Expr} object.
	 */
	public Expr BvXor(Expr expr1, Expr expr2) {
		return vc.newBVXorExpr(expr1, expr2);
	}

	/**
	 * <p>BvNor</p>
	 *
	 * @param expr1 a {@link cvc3.Expr} object.
	 * @param expr2 a {@link cvc3.Expr} object.
	 * @return a {@link cvc3.Expr} object.
	 */
	public Expr BvNor(Expr expr1, Expr expr2) {
		return vc.newBVNorExpr(expr1, expr2);
	}

	/**
	 * <p>BvXnor</p>
	 *
	 * @param expr1 a {@link cvc3.Expr} object.
	 * @param expr2 a {@link cvc3.Expr} object.
	 * @return a {@link cvc3.Expr} object.
	 */
	public Expr BvXnor(Expr expr1, Expr expr2) {
		return vc.newBVXnorExpr(expr1, expr2);
	}

	/**
	 * <p>BvNand</p>
	 *
	 * @param expr1 a {@link cvc3.Expr} object.
	 * @param expr2 a {@link cvc3.Expr} object.
	 * @return a {@link cvc3.Expr} object.
	 */
	public Expr BvNand(Expr expr1, Expr expr2) {
		return vc.newBVNandExpr(expr1, expr2);
	}

	/*
		public Expr BvShl(Expr expr1, Expr expr2) {
			return vc.newBVSHL(expr1, expr2);
		}

		public Expr BvShr(Expr expr1, Expr expr2) {
			return vc.newBVLSHR(expr1, expr2);
		}
	*/
	/**
	 * <p>BvLt</p>
	 *
	 * @param expr1 a {@link cvc3.Expr} object.
	 * @param expr2 a {@link cvc3.Expr} object.
	 * @return a {@link cvc3.Expr} object.
	 */
	public Expr BvLt(Expr expr1, Expr expr2) {
		return vc.newBVLTExpr(expr1, expr2);
	}

	/**
	 * <p>BvLte</p>
	 *
	 * @param expr1 a {@link cvc3.Expr} object.
	 * @param expr2 a {@link cvc3.Expr} object.
	 * @return a {@link cvc3.Expr} object.
	 */
	public Expr BvLte(Expr expr1, Expr expr2) {
		return vc.newBVLEExpr(expr1, expr2);
	}

	/**
	 * <p>BvGt</p>
	 *
	 * @param expr1 a {@link cvc3.Expr} object.
	 * @param expr2 a {@link cvc3.Expr} object.
	 * @return a {@link cvc3.Expr} object.
	 */
	public Expr BvGt(Expr expr1, Expr expr2) {
		return vc.notExpr(vc.newBVLEExpr(expr1, expr2));
	}

	/**
	 * <p>BvGte</p>
	 *
	 * @param expr1 a {@link cvc3.Expr} object.
	 * @param expr2 a {@link cvc3.Expr} object.
	 * @return a {@link cvc3.Expr} object.
	 */
	public Expr BvGte(Expr expr1, Expr expr2) {
		return vc.notExpr(vc.newBVLTExpr(expr1, expr2));
	}
}
