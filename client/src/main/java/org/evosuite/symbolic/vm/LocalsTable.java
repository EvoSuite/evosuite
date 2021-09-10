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

import java.util.ArrayList;
import java.util.List;

/**
 * @author galeotti
 */
public final class LocalsTable {

    /**
     * List of local variables
     */
    private final List<Operand> locals = new ArrayList<>();

    public LocalsTable(int maxLocals) {
        for (int i = 0; i < maxLocals; i++)
            locals.add(null);
    }

    public void setOperand(int i, Operand operand) {
        locals.set(i, operand);
    }

    public void setFp64Local(int i, RealValue r) {
        locals.set(i, new Fp64Operand(r));
    }

    public void setFp32Local(int i, RealValue r) {
        locals.set(i, new Fp32Operand(r));
    }

    public void setBv32Local(int i, IntegerValue e) {
        locals.set(i, new Bv32Operand(e));
    }

    public void setBv64Local(int i, IntegerValue e) {
        locals.set(i, new Bv64Operand(e));
    }

    public void setRefLocal(int i, ReferenceExpression o) {
        locals.set(i, new ReferenceOperand(o));
    }

    public ReferenceExpression getRefLocal(int i) {
        Operand x = locals.get(i);
        ReferenceOperand refOp = (ReferenceOperand) x;
        return refOp.getReference();
    }

    public Operand getOperand(int i) {
        Operand x = locals.get(i);
        return x;
    }

    public IntegerValue getBv64Local(int i) {
        Operand x = locals.get(i);
        Bv64Operand bv64 = (Bv64Operand) x;
        return bv64.getIntegerExpression();
    }

    public IntegerValue getBv32Local(int i) {
        Operand x = locals.get(i);
        Bv32Operand bv32 = (Bv32Operand) x;
        return bv32.getIntegerExpression();
    }

    public RealValue getFp32Local(int i) {
        Operand x = locals.get(i);
        Fp32Operand fp32 = (Fp32Operand) x;
        return fp32.getRealExpression();
    }

    public RealValue getFp64Local(int i) {
        Operand x = locals.get(i);
        Fp64Operand fp64 = (Fp64Operand) x;
        return fp64.getRealExpression();
    }
}
