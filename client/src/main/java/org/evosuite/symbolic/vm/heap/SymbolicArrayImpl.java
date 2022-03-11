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
import org.evosuite.symbolic.expr.ExpressionUtils;
import org.evosuite.symbolic.expr.bv.IntegerValue;
import org.evosuite.symbolic.expr.fp.RealValue;
import org.evosuite.symbolic.expr.str.StringValue;
import org.evosuite.utils.TypeUtil;
import org.objectweb.asm.Type;

import java.util.HashMap;
import java.util.Map;

/**
 * Symbolic Array that represents the heap memory model of a simple array.
 *
 * @author Ignacio Lebrero
 */
public class SymbolicArrayImpl implements SymbolicArray {

    protected Type contentType;
    protected Map<Integer, Expression<?>> contents;

    public SymbolicArrayImpl(Type contentType) {
        this.contents = new HashMap();
        this.contentType = contentType;
    }

    @Override
    public Type getContentType() {
        return contentType;
    }

    @Override
    public Expression get(Integer index) {
        return contents.get(index);
    }

    @Override
    public void set(Integer index, Expression expression) {
        matchType(expression);
        contents.put(index, expression);
    }

    private void matchType(Expression expression) {
        if (ExpressionUtils.isIntegerValue(expression) && !TypeUtil.isIntegerValue(contentType)) {
            throw new IllegalArgumentException("Symbolic array contains type "
                    + IntegerValue.class.getName()
                    + "but provided type doesn't match");
        }

        if (ExpressionUtils.isRealValue(expression) && !TypeUtil.isRealValue(contentType)) {
            throw new IllegalArgumentException("Symbolic array contains type "
                    + RealValue.class.getName()
                    + "but provided type doesn't match");
        }

        if (ExpressionUtils.isStringValue(expression) && !TypeUtil.isStringValue(contentType)) {
            throw new IllegalArgumentException("Symbolic array contains type "
                    + StringValue.class.getName()
                    + "but provided type doesn't match");
        }
    }
}
