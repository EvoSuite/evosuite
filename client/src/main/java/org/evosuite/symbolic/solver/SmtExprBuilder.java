/*
 * Copyright (C) 2010-2018 Gordon Fraser, Andrea Arcuri and EvoSuite
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

import org.evosuite.symbolic.solver.smt.*;

public abstract class SmtExprBuilder {

    public static final SmtIntConstant ZERO_INT = mkIntConstant(0);

    public static final SmtIntConstant NINE_INT = mkIntConstant(9);

    public static final SmtStringConstant ZERO_STRING = mkStringConstant("0");

    public static final SmtStringConstant NINE_STRING = mkStringConstant("9");

    public static final SmtStringConstant PLUS = mkStringConstant("+");

    public static final SmtStringConstant MINUS = mkStringConstant("-");

    public static final SmtStringConstant BLANK = mkStringConstant("\b");

    public static final SmtRealConstant ZERO_REAL = mkRealConstant(0);

    public static final SmtIntConstant ONE_INT = mkIntConstant(1);

    public static final SmtBooleanConstant TRUE = mkBooleanConstant(true);

    public static final SmtBooleanConstant FALSE = mkBooleanConstant(false);

    public static final SmtStringConstant TRUE_STRING = mkStringConstant(Boolean.toString(true));

    public static final SmtStringConstant FALSE_STRING = mkStringConstant(Boolean.toString(false));

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

    public static SmtExpr mkITE(SmtExpr condExpr, SmtExpr thenExpr, SmtExpr elseExpr) {
        return new SmtOperation(SmtOperation.Operator.ITE, condExpr, thenExpr, elseExpr);
    }

    public static SmtExpr mkLt(SmtExpr left, SmtExpr right) {
        return new SmtOperation(SmtOperation.Operator.LT, left, right);
    }

    public static SmtExpr mkArrayLoad(SmtExpr arrayExpr, SmtExpr indexExpr) {
        return new SmtOperation(SmtOperation.Operator.SELECT, arrayExpr, indexExpr);
    }

    public static SmtExpr mkArrayStore(SmtExpr arrayExpr, SmtExpr indexExpr, SmtExpr valueExpression) {
        return new SmtOperation(SmtOperation.Operator.STORE, arrayExpr, indexExpr, valueExpression);
    }

    public static SmtIntConstant mkIntConstant(long longValue) {
        return new SmtIntConstant(longValue);
    }

    public static SmtExpr mkIntegerArrayConstant(Object arrayValue) {
        return new SmtArrayConstant.SmtIntegerArrayConstant(arrayValue);
    }

    public static SmtExpr mkRealArrayConstant(Object arrayValue) {
        return new SmtArrayConstant.SmtRealArrayConstant(arrayValue);
    }

    public static SmtExpr mkStringArrayConstant(Object arrayValue) {
        return new SmtArrayConstant.SmtStringArrayConstant(arrayValue);
    }

    public static SmtExpr mkReferenceArrayConstant(Object arrayValue) {
        return new SmtArrayConstant.SmtReferenceArrayConstant(arrayValue);
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

    public static SmtExpr mkToInt(SmtExpr realExpr) {
        return new SmtOperation(SmtOperation.Operator.TO_INT, realExpr);
    }

    public static SmtExpr mkToReal(SmtExpr intExpr) {
        return new SmtOperation(SmtOperation.Operator.TO_REAL, intExpr);

    }

    public static SmtRealVariable mkRealVariable(String varName) {
        return new SmtRealVariable(varName);
    }

    public static SmtIntVariable mkIntVariable(String varName) {
        return new SmtIntVariable(varName);
    }

    public static SmtExpr mkIntegerArrayVariable(String varName) {
        return new SmtArrayVariable.SmtIntegerArrayVariable(varName);
    }

    public static SmtExpr mkRealArrayVariable(String varName) {
        return new SmtArrayVariable.SmtRealArrayVariable(varName);
    }

    public static SmtExpr mkStringArrayVariable(String varName) {
        return new SmtArrayVariable.SmtStringArrayVariable(varName);
    }

    public static SmtExpr mkReferenceArrayVariable(String varName) {
        return new SmtArrayVariable.SmtReferenceArrayVariable(varName);
    }

    public static SmtExpr mkStrSubstr(SmtExpr stringExpr, SmtExpr startIndex, SmtExpr offset) {
        return new SmtOperation(SmtOperation.Operator.STR_SUBSTR, stringExpr, startIndex, offset);
    }

    public static SmtStringConstant mkStringConstant(String stringValue) {
        return new SmtStringConstant(stringValue);
    }

    public static SmtExpr mkStrReplace(SmtExpr stringExpr, SmtExpr targetExpr, SmtExpr replacementExpr) {
        return new SmtOperation(SmtOperation.Operator.STR_REPLACE, stringExpr, targetExpr, replacementExpr);
    }

    public static SmtExpr mkStringVariable(String varName) {
        return new SmtStringVariable(varName);
    }

    public static SmtExpr mkStrIndexOf(SmtExpr stringExpr, SmtExpr termExpr, SmtExpr indexExpr) {
        return new SmtOperation(SmtOperation.Operator.STR_INDEXOF, stringExpr, termExpr, indexExpr);
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
        return new SmtOperation(SmtOperation.Operator.STR_PREFIXOF, stringExpr, termExpr);

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

    public static SmtExpr mkREConcat(SmtExpr... args) {
        return new SmtOperation(SmtOperation.Operator.RE_CONCAT, args);

    }

    public static SmtExpr mkStrToRE(SmtStringConstant strConstant) {
        return new SmtOperation(SmtOperation.Operator.STR_TO_RE, strConstant);
    }

    public static SmtExpr mkREKleeneStar(SmtExpr expr) {
        return new SmtOperation(SmtOperation.Operator.RE_KLEENE_STAR, expr);
    }

    public static SmtExpr mkStrInRE(SmtExpr left, SmtExpr right) {
        return new SmtOperation(SmtOperation.Operator.STR_IN_RE, left, right);
    }

    public static SmtExpr mkREUnion(SmtExpr left, SmtExpr right) {
        return new SmtOperation(SmtOperation.Operator.RE_UNION, left, right);
    }

    public static SmtExpr mkREOpt(SmtExpr e) {
        return new SmtOperation(SmtOperation.Operator.RE_OPT, e);
    }

    public static SmtExpr mkREAllChar() {
        return new SmtOperation(SmtOperation.Operator.RE_ALLCHAR);
    }

    public static SmtExpr mkREKleeneCross(SmtExpr regExpr) {
        return new SmtOperation(SmtOperation.Operator.RE_KLEENE_CROSS, regExpr);
    }

    public static SmtExpr mkLoop(SmtExpr regExpr, SmtIntConstant minExpr) {
        return new SmtOperation(SmtOperation.Operator.RE_LOOP, regExpr, minExpr);
    }

    public static SmtExpr mkLoop(SmtExpr regExpr, SmtIntConstant minExpr, SmtIntConstant maxExpr) {
        return new SmtOperation(SmtOperation.Operator.RE_LOOP, regExpr, minExpr, maxExpr);

    }

    public static SmtExpr mkRERange(SmtExpr fromExpr, SmtExpr toExpr) {
        return new SmtOperation(SmtOperation.Operator.RE_RANGE, fromExpr, toExpr);
    }

    public static SmtExpr mkRem(SmtExpr left, SmtExpr right) {
        return new SmtOperation(SmtOperation.Operator.REM, left, right);
    }

    public static SmtExpr mkConcat(SmtExpr left, SmtExpr right) {
        return new SmtOperation(SmtOperation.Operator.CONCAT, left, right);
    }

    public static SmtExpr mkReplace(SmtExpr strExpr, SmtExpr targetExpr, SmtExpr replacementExpr) {
        return new SmtOperation(SmtOperation.Operator.REPLACE, strExpr, targetExpr, replacementExpr);
    }

    public static SmtExpr mkSubstring(SmtExpr string, SmtExpr fromExpr, SmtExpr toExpr) {
        return new SmtOperation(SmtOperation.Operator.SUBSTRING, string, fromExpr, toExpr);
    }

    public static SmtExpr mkEndsWith(SmtExpr left, SmtExpr right) {
        return new SmtOperation(SmtOperation.Operator.ENDSWITH, left, right);

    }

    public static SmtExpr mkStartsWith(SmtExpr left, SmtExpr right) {
        return new SmtOperation(SmtOperation.Operator.STARTSWITH, left, right);
    }

    public static SmtExpr mkIndexOf(SmtExpr left, SmtExpr right) {
        return new SmtOperation(SmtOperation.Operator.INDEXOF, left, right);
    }

    public static SmtExpr mkStringLength(SmtExpr stringExpr) {
        return new SmtOperation(SmtOperation.Operator.STR_LEN, stringExpr);

    }

    public static SmtConstantDeclaration mkIntConstantDeclaration(String constName) {
        return new SmtConstantDeclaration(constName, SmtSort.INT);
    }

    public static SmtConstantDeclaration mkRealConstantDeclaration(String constName) {
        return new SmtConstantDeclaration(constName, SmtSort.REAL);
    }

    public static SmtConstantDeclaration mkStringConstantDeclaration(String constName) {
        return new SmtConstantDeclaration(constName, SmtSort.STRING);
    }

    public static SmtFunctionDeclaration mkIntFunctionDeclaration(String funcName) {
        return new SmtFunctionDeclaration(funcName, SmtSort.INT);
    }

    public static SmtFunctionDeclaration mkRealFunctionDeclaration(String funcName) {
        return new SmtFunctionDeclaration(funcName, SmtSort.REAL);
    }

    public static SmtFunctionDeclaration mkStringFunctionDeclaration(String funcName) {
        return new SmtFunctionDeclaration(funcName, SmtSort.STRING);
    }

    public static SmtFunctionDeclaration mkIntegerArrayFunctionDeclaration(String funcName) {
        return mkArrayFunctionDeclaration(funcName, SmtSort.INT, SmtSort.INT);
    }

    public static SmtFunctionDeclaration mkStringArrayFunctionDeclaration(String funcName) {
        return mkArrayFunctionDeclaration(funcName, SmtSort.INT, SmtSort.STRING);
    }

    public static SmtFunctionDeclaration mkRealArrayFunctionDeclaration(String funcName) {
        return mkArrayFunctionDeclaration(funcName, SmtSort.INT, SmtSort.REAL);
    }

    private static SmtFunctionDeclaration mkArrayFunctionDeclaration(String funcName, SmtSort indexSort, SmtSort valueSort) {
        return new SmtFunctionDeclaration(funcName, SmtSort.ARRAY, indexSort, valueSort);
    }

    public static SmtConstantDeclaration mkRealArrayConstantDeclaration(String constName) {
        return mkArrayConstantDeclaration(constName, SmtSort.INT, SmtSort.REAL);
    }

    public static SmtConstantDeclaration mkIntegerArrayConstantDeclaration(String constName) {
        return mkArrayConstantDeclaration(constName, SmtSort.INT, SmtSort.INT);
    }

    public static SmtConstantDeclaration mkStringArrayConstantDeclaration(String constName) {
        return mkArrayConstantDeclaration(constName, SmtSort.INT, SmtSort.STRING);
    }

    /**
     * TODO: Eventually expand the sorts of any declaration to more than one
     */
    public static SmtConstantDeclaration mkArrayConstantDeclaration(String constName, SmtSort indexSort, SmtSort valueSort) {
        return new SmtConstantDeclaration(constName, SmtSort.ARRAY, indexSort, valueSort);
    }

    public static SmtExpr mkStrIndexOf(SmtExpr left, SmtExpr right) {
        return new SmtOperation(SmtOperation.Operator.STR_INDEXOF, left, right);
    }
}
