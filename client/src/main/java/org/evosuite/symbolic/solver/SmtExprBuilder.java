/**
 * Copyright (C) 2010-2016 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3.0 of the License, or
 * (at your option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.symbolic.solver;

import org.evosuite.symbolic.solver.smt.SmtBooleanConstant;
import org.evosuite.symbolic.solver.smt.SmtConstantDeclaration;
import org.evosuite.symbolic.solver.smt.SmtExpr;
import org.evosuite.symbolic.solver.smt.SmtFunctionDeclaration;
import org.evosuite.symbolic.solver.smt.SmtIntConstant;
import org.evosuite.symbolic.solver.smt.SmtIntVariable;
import org.evosuite.symbolic.solver.smt.SmtOperation;
import org.evosuite.symbolic.solver.smt.SmtRealConstant;
import org.evosuite.symbolic.solver.smt.SmtRealVariable;
import org.evosuite.symbolic.solver.smt.SmtStringConstant;
import org.evosuite.symbolic.solver.smt.SmtStringVariable;

public abstract class SmtExprBuilder {

	public static final SmtIntConstant ZERO_INT = mkIntConstant(0);
	public static final SmtRealConstant ZERO_REAL = mkRealConstant(0);
	public static final SmtIntConstant ONE_INT = mkIntConstant(1);
	public static final SmtBooleanConstant TRUE = mkBooleanConstant(true);
	public static final SmtBooleanConstant FALSE = mkBooleanConstant(false);

	public static SmtExpr mkIntDiv(SmtExpr left, SmtExpr right) {
		return new SmtOperation(SmtOperation.Operator.DIV, left, right);
	}

	public static SmtExpr mkRealDiv(SmtExpr left, SmtExpr right) {
		return new SmtOperation(SmtOperation.Operator.SLASH, left, right);
	}

	private static SmtBooleanConstant mkBooleanConstant(boolean b) {
		return new SmtBooleanConstant(b);
	}

	public static SmtExpr mkMul(SmtExpr left, SmtExpr right) {
		return new SmtOperation(SmtOperation.Operator.MUL, left, right);
	}

	public static SmtExpr mkSub(SmtExpr left, SmtExpr right) {
		return new SmtOperation(SmtOperation.Operator.MINUS, left, right);
	}

	public static SmtExpr mkAdd(SmtExpr left, SmtExpr right) {
		return new SmtOperation(SmtOperation.Operator.ADD, left, right);
	}

	public static SmtExpr mkMod(SmtExpr left, SmtExpr right) {
		return new SmtOperation(SmtOperation.Operator.MOD, left, right);
	}

	public static SmtExpr mkInt2BV(int bitwidth, SmtExpr arg) {
		return new SmtOperation(SmtOperation.Operator.INT2BV32, arg);
	}

	public static SmtExpr mkBVOR(SmtExpr left, SmtExpr right) {
		return new SmtOperation(SmtOperation.Operator.BVOR, left, right);
	}

	public static SmtExpr mkBV2Nat(SmtExpr arg) {
		return new SmtOperation(SmtOperation.Operator.BV2Nat, arg);
	}

	public static SmtExpr mkBVAND(SmtExpr left, SmtExpr right) {
		return new SmtOperation(SmtOperation.Operator.BVAND, left, right);
	}

	public static SmtExpr mkBVXOR(SmtExpr left, SmtExpr right) {
		return new SmtOperation(SmtOperation.Operator.BVXOR, left, right);
	}

	public static SmtExpr mkBV2Int(SmtExpr arg) {
		return new SmtOperation(SmtOperation.Operator.BV2INT, arg);
	}

	public static SmtExpr mkBVSHL(SmtExpr left, SmtExpr right) {
		return new SmtOperation(SmtOperation.Operator.BVSHL, left, right);
	}

	public static SmtExpr mkBVASHR(SmtExpr left, SmtExpr right) {
		return new SmtOperation(SmtOperation.Operator.BVASHR, left, right);
	}

	public static SmtExpr mkBVLSHR(SmtExpr left, SmtExpr right) {
		return new SmtOperation(SmtOperation.Operator.BVLSHR, left, right);
	}

	public static SmtExpr mkGt(SmtExpr left, SmtExpr right) {
		return new SmtOperation(SmtOperation.Operator.GT, left, right);
	}

	public static SmtExpr mkITE(SmtExpr condExpr, SmtExpr thenExpr,
			SmtExpr elseExpr) {
		return new SmtOperation(SmtOperation.Operator.ITE, condExpr, thenExpr,
				elseExpr);

	}

	public static SmtExpr mkLt(SmtExpr left, SmtExpr right) {
		return new SmtOperation(SmtOperation.Operator.LT, left, right);

	}

	public static SmtIntConstant mkIntConstant(long longValue) {
		return new SmtIntConstant(longValue);
	}

	public static SmtRealConstant mkRealConstant(double doubleValue) {
		return new SmtRealConstant(doubleValue);
	}

	public static SmtExpr mkGe(SmtExpr left, SmtExpr right) {
		return new SmtOperation(SmtOperation.Operator.GE, left, right);

	}

	public static SmtExpr mkNeg(SmtExpr expr) {
		return new SmtOperation(SmtOperation.Operator.MINUS, expr);
	}

	public static SmtExpr mkReal2Int(SmtExpr realExpr) {
		return new SmtOperation(SmtOperation.Operator.REAL2INT, realExpr);
	}

	public static SmtExpr mkInt2Real(SmtExpr intExpr) {
		return new SmtOperation(SmtOperation.Operator.INT2REAL, intExpr);

	}

	public static SmtRealVariable mkRealVariable(String varName) {
		return new SmtRealVariable(varName);
	}

	public static SmtIntVariable mkIntVariable(String varName) {
		return new SmtIntVariable(varName);

	}

	public static SmtExpr mkStrSubstring(SmtExpr stringExpr,
			SmtExpr startIndex, SmtExpr offset) {
		return new SmtOperation(SmtOperation.Operator.STR_SUBSTR, stringExpr,
				startIndex, offset);
	}

	public static SmtStringConstant mkStringConstant(String stringValue) {
		return new SmtStringConstant(stringValue);
	}

	public static SmtExpr mkStrReplace(SmtExpr stringExpr, SmtExpr targetExpr,
			SmtExpr replacementExpr) {
		return new SmtOperation(SmtOperation.Operator.STR_REPLACE, stringExpr,
				targetExpr, replacementExpr);
	}

	public static SmtExpr mkStringVariable(String varName) {
		return new SmtStringVariable(varName);
	}

	public static SmtExpr mkStrIndexOf(SmtExpr stringExpr, SmtExpr termExpr,
			SmtExpr indexExpr) {
		return new SmtOperation(SmtOperation.Operator.STR_INDEXOF, stringExpr,
				termExpr, indexExpr);
	}

	public static SmtExpr mkEq(SmtExpr left, SmtExpr right) {
		return new SmtOperation(SmtOperation.Operator.EQ, left, right);

	}

	public static SmtExpr mkStrConcat(SmtExpr left, SmtExpr right) {
		return new SmtOperation(SmtOperation.Operator.STR_CONCAT, left, right);

	}

	public static SmtExpr mkIntToStr(SmtExpr arg) {
		return new SmtOperation(SmtOperation.Operator.INT_TO_STR, arg);
	}

	public static SmtExpr mkStrSuffixOf(SmtExpr left, SmtExpr right) {
		return new SmtOperation(SmtOperation.Operator.STR_SUFFIXOF, left, right);
	}

	public static SmtExpr mkStrContains(SmtExpr left, SmtExpr right) {
		return new SmtOperation(SmtOperation.Operator.STR_CONTAINS, left, right);

	}

	public static SmtExpr mkStrAt(SmtExpr left, SmtExpr right) {
		return new SmtOperation(SmtOperation.Operator.STR_AT, left, right);

	}

	public static SmtExpr mkCharToInt(SmtExpr arg) {
		return new SmtOperation(SmtOperation.Operator.CHAR_TO_INT, arg);
	}

	public static SmtExpr mkStrPrefixOf(SmtExpr stringExpr, SmtExpr termExpr) {
		return new SmtOperation(SmtOperation.Operator.STR_PREFIXOF, stringExpr,
				termExpr);

	}

	public static SmtExpr mkIntToChar(SmtExpr arg) {
		return new SmtOperation(SmtOperation.Operator.INT_TO_CHAR, arg);

	}

	public static SmtExpr mkStrLen(SmtExpr arg) {
		return new SmtOperation(SmtOperation.Operator.STR_LEN, arg);
	}

	public static SmtExpr mkLe(SmtExpr left, SmtExpr right) {
		return new SmtOperation(SmtOperation.Operator.LE, left, right);

	}

	public static SmtExpr mkNot(SmtExpr arg) {
		return new SmtOperation(SmtOperation.Operator.NOT, arg);
	}

	public static SmtExpr mkStrToInt(SmtExpr arg) {
		return new SmtOperation(SmtOperation.Operator.STR_TO_INT, arg);
	}

	public static SmtExpr mkAbs(SmtExpr arg) {
		return new SmtOperation(SmtOperation.Operator.ABS, arg);
	}

	public static SmtExpr mkBVADD(SmtExpr left, SmtExpr right) {
		return new SmtOperation(SmtOperation.Operator.BVADD, left, right);
	}

	public static SmtExpr mkRegExpConcat(SmtExpr left, SmtExpr right) {
		return new SmtOperation(SmtOperation.Operator.REG_EXP_CONCAT, left,
				right);

	}

	public static SmtExpr mkStrToRegExp(SmtStringConstant strConstant) {
		return new SmtOperation(SmtOperation.Operator.STR_TO_REG_EXP,
				strConstant);
	}

	public static SmtExpr mkReKleeneStar(SmtExpr expr) {
		return new SmtOperation(SmtOperation.Operator.REG_EXP_KLEENE_STAR, expr);
	}

	public static SmtExpr mkStrInRegExp(SmtExpr left, SmtExpr right) {
		return new SmtOperation(SmtOperation.Operator.STR_IN_REG_EXP, left,
				right);
	}

	public static SmtExpr mkRegExpUnion(SmtExpr left, SmtExpr right) {
		return new SmtOperation(SmtOperation.Operator.REG_EXP_UNION, left,
				right);
	}

	public static SmtExpr mkRegExpOptional(SmtExpr e) {
		return new SmtOperation(SmtOperation.Operator.REG_EXP_OPTIONAL, e);
	}

	public static SmtExpr mkRegExpAllChar() {
		return new SmtOperation(SmtOperation.Operator.REG_EXP_ALL_CHAR);
	}

	public static SmtExpr mkRegExpKleeCross(SmtExpr regExpr) {
		return new SmtOperation(SmtOperation.Operator.REG_EXP_KLEENE_CROSS,
				regExpr);
	}

	public static SmtExpr mkLoop(SmtExpr regExpr, SmtIntConstant minExpr) {
		return new SmtOperation(SmtOperation.Operator.REG_EXP_LOOP, regExpr,
				minExpr);
	}

	public static SmtExpr mkLoop(SmtExpr regExpr, SmtIntConstant minExpr,
			SmtIntConstant maxExpr) {
		return new SmtOperation(SmtOperation.Operator.REG_EXP_LOOP, regExpr,
				minExpr, maxExpr);

	}

	public static SmtExpr mkRegExpRange(SmtExpr fromExpr, SmtExpr toExpr) {
		return new SmtOperation(SmtOperation.Operator.REG_EXP_RANGE, fromExpr,
				toExpr);
	}

	public static SmtExpr mkRem(SmtExpr left, SmtExpr right) {
		return new SmtOperation(SmtOperation.Operator.REM, left, right);
	}

	public static SmtExpr mkConcat(SmtExpr left, SmtExpr right) {
		return new SmtOperation(SmtOperation.Operator.CONCAT, left, right);
	}

	public static SmtExpr mkReplace(SmtExpr strExpr, SmtExpr targetExpr,
			SmtExpr replacementExpr) {
		return new SmtOperation(SmtOperation.Operator.REPLACE, strExpr,
				targetExpr, replacementExpr);
	}

	public static SmtExpr mkSubstring(SmtExpr string, SmtExpr fromExpr,
			SmtExpr toExpr) {
		return new SmtOperation(SmtOperation.Operator.SUBSTRING, string,
				fromExpr, toExpr);
	}

	public static SmtExpr mkEndsWith(SmtExpr left, SmtExpr right) {
		return new SmtOperation(SmtOperation.Operator.ENDSWITH, left, right);

	}

	public static SmtExpr mkContains(SmtExpr left, SmtExpr right) {
		return new SmtOperation(SmtOperation.Operator.CONTAINS, left, right);

	}

	public static SmtExpr mkStartsWith(SmtExpr left, SmtExpr right) {
		return new SmtOperation(SmtOperation.Operator.STARTSWITH, left, right);
	}

	public static SmtExpr mkIndexOf(SmtExpr left, SmtExpr right) {
		return new SmtOperation(SmtOperation.Operator.INDEXOF, left, right);
	}

	public static SmtExpr mkLength(SmtExpr stringExpr) {
		return new SmtOperation(SmtOperation.Operator.LENGTH, stringExpr);

	}

	public static SmtConstantDeclaration mkIntConstantDeclaration(
			String constName) {
		return new SmtConstantDeclaration(constName, "Int");
	}

	public static SmtConstantDeclaration mkRealConstantDeclaration(
			String constName) {
		return new SmtConstantDeclaration(constName, "Real");
	}

	public static SmtConstantDeclaration mkStringConstantDeclaration(
			String constName) {
		return new SmtConstantDeclaration(constName, "String");
	}

	public static SmtFunctionDeclaration mkIntFunctionDeclaration(
			String funcName) {
		return new SmtFunctionDeclaration(funcName, "Int");
	}

	public static SmtFunctionDeclaration mkRealFunctionDeclaration(
			String funcName) {
		return new SmtFunctionDeclaration(funcName, "Real");
	}

	public static SmtFunctionDeclaration mkStringFunctionDeclaration(
			String funcName) {
		return new SmtFunctionDeclaration(funcName, "String");
	}

}
