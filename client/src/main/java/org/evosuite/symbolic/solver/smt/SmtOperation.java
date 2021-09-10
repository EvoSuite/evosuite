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
        TO_INT("to_int"), //
        TO_REAL("to_real"), //
        DIV("div"), //
        SLASH("/"), //
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
        STR_TO_INT("str.to.int"), //
        ABS("abs"), //
        BVADD("bvadd"), //
        // regular expressions
        STR_IN_RE("str.in.re"), //
        STR_TO_RE("str.to.re"), //
        RE_CONCAT("re.++"), //
        RE_KLEENE_STAR("re.*"), //
        RE_UNION("re.union"), //
        RE_OPT("re.opt"), //
        RE_ALLCHAR("re.allchar"), //
        RE_KLEENE_CROSS("re.+"), //
        RE_LOOP("re.loop"), //
        RE_RANGE("re.range"), //


        REM("rem"), //
        CONCAT("Concat"), //
        REPLACE("Replace"), //
        SUBSTRING("Substring"), //
        ENDSWITH("Endswith"), //
        CONTAINS("Contains"), //
        STARTSWITH("StarsWith"), //
        INDEXOF("Indexof"), //
        LENGTH("Length"),

        /**
         * Arrays
         */
        SELECT("select"),
        STORE("store");


        private final String rep;

        Operator(String rep) {
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
        result = prime * result + ((operator == null) ? 0 : operator.hashCode());
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
        return operator == other.operator;
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
