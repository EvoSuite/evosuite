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
package org.evosuite.symbolic.expr.token;

import org.evosuite.symbolic.expr.AbstractExpression;
import org.evosuite.symbolic.expr.str.StringValue;

import java.util.StringTokenizer;

public abstract class TokenizerExpr extends AbstractExpression<StringTokenizer> {

    public TokenizerExpr(int size,
                         boolean containsSymbolicVariable) {
        super(null, size, containsSymbolicVariable);
    }


    private static final long serialVersionUID = 7584961134006709947L;

    public abstract StringValue getDelimiter();

    public abstract StringValue getString();

    public abstract int getNextTokenCount();

}
