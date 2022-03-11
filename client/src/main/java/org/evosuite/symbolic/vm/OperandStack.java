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

import org.evosuite.symbolic.expr.bv.IntegerValue;
import org.evosuite.symbolic.expr.fp.RealValue;
import org.evosuite.symbolic.expr.ref.ReferenceExpression;

import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * @author galeotti
 */
public final class OperandStack implements Iterable<Operand> {

    private final Deque<Operand> stack = new LinkedList<>();

    public OperandStack() {
    }

    public void pushBv32(IntegerValue e) {
        stack.push(new Bv32Operand(e));
    }

    public void pushBv64(IntegerValue e) {
        stack.push(new Bv64Operand(e));
    }

    public void pushFp32(RealValue e) {
        stack.push(new Fp32Operand(e));
    }

    public void pushFp64(RealValue e) {
        stack.push(new Fp64Operand(e));
    }

    public void pushRef(ReferenceExpression r) {
        stack.push(new ReferenceOperand(r));
    }

    public void pushNullRef() {
        ReferenceExpression nullExpression = ExpressionFactory.buildNewNullExpression();
        this.stack.push(new ReferenceOperand(nullExpression));
    }

    public ReferenceExpression popRef() {
        Operand ret_val = this.popOperand();
        ReferenceOperand ref = (ReferenceOperand) ret_val;
        return ref.getReference();
    }

    public IntegerValue popBv32() {
        Operand x = this.popOperand();
        Bv32Operand e = (Bv32Operand) x;
        return e.getIntegerExpression();
    }

    public IntegerValue popBv64() {
        Operand x = this.popOperand();
        Bv64Operand e = (Bv64Operand) x;
        return e.getIntegerExpression();
    }

    public RealValue popFp32() {
        Operand x = this.popOperand();
        Fp32Operand e = (Fp32Operand) x;
        return e.getRealExpression();
    }

    public RealValue popFp64() {
        Operand x = this.popOperand();
        Fp64Operand e = (Fp64Operand) x;
        return e.getRealExpression();
    }

    public Operand popOperand() {
        Operand ret_val = this.stack.pop();
        return ret_val;
    }

    public void pushOperand(Operand operand) {
        if (operand == null) {
            throw new IllegalArgumentException("Cannot push a null operand into OperandStack");
        }
        stack.push(operand);
    }

    public RealValue peekFp64() {
        Operand operand = stack.peek();
        Fp64Operand fp64 = (Fp64Operand) operand;
        return fp64.getRealExpression();
    }

    public RealValue peekFp32() {
        Operand operand = stack.peek();
        Fp32Operand fp32 = (Fp32Operand) operand;
        return fp32.getRealExpression();
    }

    public IntegerValue peekBv64() {
        Operand operand = stack.peek();
        Bv64Operand bv64 = (Bv64Operand) operand;
        return bv64.getIntegerExpression();
    }

    public IntegerValue peekBv32() {
        Operand operand = stack.peek();
        Bv32Operand bv32 = (Bv32Operand) operand;
        return bv32.getIntegerExpression();
    }

    public Operand peekOperand() {
        return stack.peek();
    }

    public Iterator<Operand> iterator() {
        return stack.iterator();
    }

    public ReferenceExpression peekRef() {
        Operand operand = this.peekOperand();
        if (!(operand instanceof ReferenceOperand)) {
            throw new ClassCastException(
                    "top of stack is not a reference but an operand of type " + operand.getClass().getCanonicalName());
        }
        ReferenceOperand refOp = (ReferenceOperand) operand;
        ReferenceExpression ref = refOp.getReference();
        return ref;
    }

    @Override
    public String toString() {
        if (this.stack.isEmpty()) {
            return "<<EMPTY_OPERAND_STACK>>";
        }

        StringBuffer buff = new StringBuffer();
        for (Operand operand : this) {
            buff.append(operand.toString() + "\n");
        }
        return buff.toString();
    }

    public int size() {
        return stack.size();
    }

    public boolean isEmpty() {
        return stack.isEmpty();
    }

    public void clearOperands() {
        stack.clear();
    }
}
