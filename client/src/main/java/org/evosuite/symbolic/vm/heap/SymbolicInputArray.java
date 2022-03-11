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
package org.evosuite.symbolic.vm.heap;

import org.evosuite.symbolic.expr.Expression;
import org.evosuite.symbolic.expr.bv.IntegerVariable;
import org.evosuite.symbolic.expr.fp.RealVariable;
import org.evosuite.symbolic.expr.ref.ReferenceExpression;
import org.evosuite.symbolic.expr.ref.ReferenceVariable;
import org.evosuite.symbolic.expr.ref.array.SymbolicArrayUtil;
import org.evosuite.symbolic.expr.str.StringVariable;
import org.evosuite.utils.TypeUtil;
import org.objectweb.asm.Type;

import java.util.BitSet;

/**
 * Symbolic Array that lazily initialize symbolic fields on demand as well as it's length.
 * It's mainly used for representing arrays that are inputs to the SUT.
 *
 * @author Ignacio Lebrero
 */
public final class SymbolicInputArray extends SymbolicArrayImpl {

    /**
     * Represents the indexes of the array that were stepped with a set call.
     */
    private final BitSet steppedOn;

    /**
     * Symbolic array variable name that this array belongs to
     */
    private final String arrayVariableName;

    public SymbolicInputArray(Type contentType, String arrayVariableName) {
        super(contentType);

        this.steppedOn = new BitSet();
        this.arrayVariableName = arrayVariableName;
    }

    public SymbolicInputArray(SymbolicArray array, String arrayVariableName) {
        this(array.getContentType(), arrayVariableName);
    }

    /**
     * We infer that an input variable should be initialized if the index was never set and
     * a get is used. (Thus a value already set before calling the SUT should be there)
     *
     * @param index
     * @return
     */
    @Override
    public Expression get(Integer index) {
        Expression arrayValue = super.get(index);

        if (arrayValue == null && !steppedOn.get(index)) {
            arrayValue = initDefaultArrayVariable(index, contentType);
            contents.put(index, arrayValue);
        }

        return arrayValue;
    }

    /**
     * We checked the index value as used.
     *
     * @param index
     * @param expression
     */
    @Override
    public void set(Integer index, Expression expression) {
        super.set(index, expression);

        steppedOn.set(index);
    }

    private Expression initDefaultArrayVariable(Integer index, Type componentType) {
        String symbolicArrayVariableName = SymbolicArrayUtil.buildArrayContentVariableName(arrayVariableName, index);

        if (TypeUtil.isIntegerValue(componentType)) {
            return new IntegerVariable(symbolicArrayVariableName, 0, Long.MIN_VALUE, Long.MAX_VALUE);
        }

        if (TypeUtil.isRealValue(componentType)) {
            return new RealVariable(symbolicArrayVariableName, 0, Long.MIN_VALUE, Long.MAX_VALUE);
        }

        if (TypeUtil.isStringValue(componentType)) {
            return new StringVariable(symbolicArrayVariableName, "");
        }

        if (componentType.equals(ReferenceExpression.class)) {
            return new ReferenceVariable(componentType, 0, symbolicArrayVariableName, null);
        }

        throw new UnsupportedOperationException("Symbolic Array content type: " + componentType + " not yet supported");
    }
}
