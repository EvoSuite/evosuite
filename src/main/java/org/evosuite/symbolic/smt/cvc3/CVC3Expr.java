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
 * @author fraser
 * 
 */
public class CVC3Expr {

	private final ValidityChecker vc;

	public CVC3Expr(ValidityChecker vc) {
		this.vc = vc;
	}

	public Expr getSymbol(String name) {
		return vc.varExpr(name, vc.intType());
	}

	public Expr getNumeral(int num) {
		return vc.ratExpr(num);
	}

	public Expr getNumeral(long num) {
		if (num > Integer.MAX_VALUE || num < Integer.MIN_VALUE)
			return vc.ratExpr(Long.toString(num));
		else
			return getNumeral((int) num);
	}

	public Expr Not(Expr expr) {
		return vc.notExpr(expr);
	}

	public Expr And(Expr expr1, Expr expr2) {
		return vc.andExpr(expr1, expr2);
	}

	public Expr Or(Expr expr1, Expr expr2) {
		return vc.orExpr(expr1, expr2);
	}

	public Expr Xor(Expr expr1, Expr expr2) {
		return vc.orExpr(vc.andExpr(vc.notExpr(expr1), expr2),
		                 vc.andExpr(expr1, vc.notExpr(expr2)));
	}

	public Expr Neg(Expr expr) {
		return vc.uminusExpr(expr);
	}

	public Expr Plus(Expr expr1, Expr expr2) {
		return vc.plusExpr(expr1, expr2);
	}

	public Expr Minus(Expr expr1, Expr expr2) {
		return vc.minusExpr(expr1, expr2);
	}

	public Expr Mul(Expr expr1, Expr expr2) {
		return vc.multExpr(expr1, expr2);
	}

	public Expr Div(Expr expr1, Expr expr2) {
		return vc.divideExpr(expr1, expr2);
	}

	public Expr Pow(Expr expr1, Expr expr2) {
		return vc.powExpr(expr1, expr2);
	}

	public Expr Lt(Expr expr1, Expr expr2) {
		return vc.ltExpr(expr1, expr2);
	}

	public Expr Lte(Expr expr1, Expr expr2) {
		return vc.leExpr(expr1, expr2);
	}

	public Expr Gt(Expr expr1, Expr expr2) {
		return vc.gtExpr(expr1, expr2);
	}

	public Expr Gte(Expr expr1, Expr expr2) {
		return vc.geExpr(expr1, expr2);
	}

	public Expr Implies(Expr expr1, Expr expr2) {
		return vc.impliesExpr(expr1, expr2);
	}

	public Expr Eq(Expr expr1, Expr expr2) {
		return vc.eqExpr(expr1, expr2);
	}

	public Expr Neq(Expr expr1, Expr expr2) {
		return vc.notExpr(vc.eqExpr(expr1, expr2));
	}

	public Expr Ite(Expr expr1, Expr expr2, Expr expr3) {
		return vc.iteExpr(expr1, expr2, expr3);
	}

	//-----------------------------------------------------
	// Bitvector expressions

	public Expr BvNeg(Expr expr) {
		return vc.newBVNegExpr(expr);
	}

	public Expr BvPlus(int numbits, Expr expr1, Expr expr2) {
		List<Expr> exprs = new LinkedList<Expr>();
		exprs.add(expr1);
		exprs.add(expr2);
		return vc.newBVPlusExpr(numbits, exprs);
	}

	public Expr BvMinus(Expr expr1, Expr expr2) {
		return vc.newBVSubExpr(expr1, expr2);
	}

	public Expr BvMul(int numbits, Expr expr1, Expr expr2) {
		return vc.newBVMultExpr(numbits, expr1, expr2);
	}

	public Expr BvSDiv(Expr expr1, Expr expr2) {
		return vc.newBVSDivExpr(expr1, expr2);
	}

	public Expr BvUDiv(Expr expr1, Expr expr2) {
		return vc.newBVUDivExpr(expr1, expr2);
	}

	public Expr BvOr(Expr expr1, Expr expr2) {
		return vc.newBVOrExpr(expr1, expr2);
	}

	public Expr BvAnd(Expr expr1, Expr expr2) {
		return vc.newBVAndExpr(expr1, expr2);
	}

	public Expr BvXor(Expr expr1, Expr expr2) {
		return vc.newBVXorExpr(expr1, expr2);
	}

	public Expr BvNor(Expr expr1, Expr expr2) {
		return vc.newBVNorExpr(expr1, expr2);
	}

	public Expr BvXnor(Expr expr1, Expr expr2) {
		return vc.newBVXnorExpr(expr1, expr2);
	}

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
	public Expr BvLt(Expr expr1, Expr expr2) {
		return vc.newBVLTExpr(expr1, expr2);
	}

	public Expr BvLte(Expr expr1, Expr expr2) {
		return vc.newBVLEExpr(expr1, expr2);
	}

	public Expr BvGt(Expr expr1, Expr expr2) {
		return vc.notExpr(vc.newBVLEExpr(expr1, expr2));
	}

	public Expr BvGte(Expr expr1, Expr expr2) {
		return vc.notExpr(vc.newBVLTExpr(expr1, expr2));
	}
}
