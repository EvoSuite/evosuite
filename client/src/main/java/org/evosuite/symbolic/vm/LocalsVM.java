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
package org.evosuite.symbolic.vm;

import org.evosuite.dse.AbstractVM;
import org.evosuite.symbolic.expr.bv.IntegerConstant;
import org.evosuite.symbolic.expr.bv.IntegerValue;
import org.evosuite.symbolic.expr.fp.RealConstant;
import org.evosuite.symbolic.expr.fp.RealValue;
import org.evosuite.symbolic.expr.ref.ReferenceConstant;
import org.evosuite.symbolic.expr.ref.ReferenceExpression;

/**
 * @author galeotti
 */
public final class LocalsVM extends AbstractVM {

    private final SymbolicEnvironment env;

    public LocalsVM(SymbolicEnvironment env) {
        this.env = env;
    }

    @Override
    public void NOP() { /**/
    }

    /**
     * http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.doc
     * .html#aconst_null
     */
    @Override
    public void ACONST_NULL() {
        env.topFrame().operandStack.pushNullRef();
    }

    /**
     * http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.
     * doc6.html#iconst_i
     */
    @Override
    public void ICONST_M1() {
        env.topFrame().operandStack.pushBv32(ExpressionFactory.ICONST_M1);
    }

    @Override
    public void ICONST_0() {
        env.topFrame().operandStack.pushBv32(ExpressionFactory.ICONST_0);
    }

    @Override
    public void ICONST_1() {
        env.topFrame().operandStack.pushBv32(ExpressionFactory.ICONST_1);
    }

    @Override
    public void ICONST_2() {
        env.topFrame().operandStack.pushBv32(ExpressionFactory.ICONST_2);
    }

    @Override
    public void ICONST_3() {
        env.topFrame().operandStack.pushBv32(ExpressionFactory.ICONST_3);
    }

    @Override
    public void ICONST_4() {
        env.topFrame().operandStack.pushBv32(ExpressionFactory.ICONST_4);
    }

    @Override
    public void ICONST_5() {
        env.topFrame().operandStack.pushBv32(ExpressionFactory.ICONST_5);
    }

    @Override
    public void LCONST_0() {
        env.topFrame().operandStack.pushBv64(ExpressionFactory.ICONST_0);
    }

    @Override
    public void LCONST_1() {
        env.topFrame().operandStack.pushBv64(ExpressionFactory.ICONST_1);
    }

    @Override
    public void FCONST_0() {
        env.topFrame().operandStack.pushFp32(ExpressionFactory.RCONST_0);
    }

    @Override
    public void FCONST_1() {
        env.topFrame().operandStack.pushFp32(ExpressionFactory.RCONST_1);
    }

    @Override
    public void FCONST_2() {
        env.topFrame().operandStack.pushFp32(ExpressionFactory.RCONST_2);
    }

    @Override
    public void DCONST_0() {
        env.topFrame().operandStack.pushFp64(ExpressionFactory.RCONST_0);
    }

    @Override
    public void DCONST_1() {
        env.topFrame().operandStack.pushFp64(ExpressionFactory.RCONST_1);
    }

    /**
     * Bytecode instruction stream: ... ,0x10, byte, ...
     *
     * <p>
     * Push the byte value that immediately follows this bytecode instruction
     * (0x10) in the bytecode stream. The byte value is sign-extended to an int.
     * <p>
     * http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.
     * doc1.html#bipush
     */
    @Override
    public void BIPUSH(int value) {
        IntegerConstant intConstant = ExpressionFactory
                .buildNewIntegerConstant(value);
        env.topFrame().operandStack.pushBv32(intConstant);
    }

    /**
     * Bytecode instruction stream: ... ,0x11, byte1, byte2, ...
     *
     * <p>
     * Push the short value that immediately follows this bytecode instruction
     * (0x11) in the bytecode stream. The short value is sign-extended to an
     * int.
     * <p>
     * http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.
     * doc13.html#sipush
     */
    @Override
    public void SIPUSH(int value) {
        env.topFrame().operandStack.pushBv32(ExpressionFactory
                .buildNewIntegerConstant(value));
    }

    /**
     * Bytecode instruction stream: ... ,0x12, index, ...
     *
     * <p>
     * Push corresponding symbolic constant from constant pool (at index) onto
     * the operand stack.
     * <p>
     * http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.
     * doc8.html#ldc
     */
    @Override
    public void LDC(int x) {
        env.topFrame().operandStack.pushBv32(ExpressionFactory
                .buildNewIntegerConstant(x));

    }

    @Override
    public void LDC(String x) {
        if (x == null) {
            env.topFrame().operandStack.pushNullRef();
        } else {
            ReferenceConstant stringRef = (ReferenceConstant) env.heap
                    .getReference(x);

            env.topFrame().operandStack.pushRef(stringRef);
        }
    }

    /**
     * Class literal described in Java Spec 3rd ed, Section 15.8.2
     * <p>
     * ".class" Java expression, such as for example in:
     * I.class.isAssignableFrom(..)
     *
     * <p>
     * Represent reference to Class object with symbolic function application:
     * ClassRef(Type)
     *
     * @see http
     * ://java.sun.com/docs/books/jls/third_edition/html/expressions.html
     * #15.8.2
     */
    @Override
    public void LDC(Class<?> x) {
        ReferenceExpression ref = env.heap.getReference(x);
        env.topFrame().operandStack.pushRef(ref);
    }

    @Override
    public void LDC2_W(long x) {
        env.topFrame().operandStack.pushBv64(ExpressionFactory
                .buildNewIntegerConstant(x));
    }

    @Override
    public void LDC(float x) {
        RealConstant realConstant = ExpressionFactory.buildNewRealConstant(x);
        env.topFrame().operandStack.pushFp32(realConstant);
    }

    @Override
    public void LDC2_W(double x) {
        env.topFrame().operandStack.pushFp64(new RealConstant(x));
    }

    /**
     * ... ==> ..., value
     * <p>
     * http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.
     * doc6.html#iload
     */
    @Override
    public void ILOAD(int i) {
        IntegerValue integerExpression = env.topFrame().localsTable
                .getBv32Local(i);
        env.topFrame().operandStack.pushBv32(integerExpression);
    }

    @Override
    public void LLOAD(int i) {
        IntegerValue integerExpression = env.topFrame().localsTable
                .getBv64Local(i);
        env.topFrame().operandStack.pushBv64(integerExpression);
    }

    @Override
    public void FLOAD(int i) {
        RealValue realExpr = env.topFrame().localsTable
                .getFp32Local(i);
        env.topFrame().operandStack.pushFp32(realExpr);
    }

    @Override
    public void DLOAD(int i) {
        RealValue realExpr = env.topFrame().localsTable
                .getFp64Local(i);
        env.topFrame().operandStack.pushFp64(realExpr);
    }

    @Override
    public void ALOAD(int i) {
        Operand local = env.topFrame().localsTable.getOperand(i);
        env.topFrame().operandStack.pushOperand(local);
    }

    /**
     * ..., value ==> ...
     * <p>
     * http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.
     * doc6.html#istore
     */
    @Override
    public void ISTORE(int i) {
        IntegerValue integerExpr = env.topFrame().operandStack.popBv32();
        env.topFrame().localsTable.setBv32Local(i, integerExpr);
    }

    @Override
    public void LSTORE(int i) {
        IntegerValue integerExpr = env.topFrame().operandStack.popBv64();
        env.topFrame().localsTable.setBv64Local(i, integerExpr);
    }

    @Override
    public void FSTORE(int i) {
        RealValue realExpr = env.topFrame().operandStack.popFp32();
        env.topFrame().localsTable.setFp32Local(i, realExpr);
    }

    @Override
    public void DSTORE(int i) {
        RealValue realExpr = env.topFrame().operandStack.popFp64();
        env.topFrame().localsTable.setFp64Local(i, realExpr);
    }

    @Override
    public void ASTORE(int i) {
        Operand operand = env.topFrame().operandStack.popOperand();
        env.topFrame().localsTable.setOperand(i, operand);
    }
}
