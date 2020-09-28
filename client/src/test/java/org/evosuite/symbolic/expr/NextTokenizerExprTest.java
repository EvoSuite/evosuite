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
package org.evosuite.symbolic.expr;

import org.evosuite.symbolic.expr.str.StringConstant;
import org.evosuite.symbolic.expr.token.NewTokenizerExpr;
import org.evosuite.symbolic.expr.token.NextTokenizerExpr;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class NextTokenizerExprTest {


    @Test
    public void testEquals() {
        StringConstant helloWorldStr = new StringConstant("Hello World");
        StringConstant delimiterStr = new StringConstant(" ");
        NewTokenizerExpr newTokenizerExpr = new NewTokenizerExpr(helloWorldStr, delimiterStr);
        NextTokenizerExpr left = new NextTokenizerExpr(newTokenizerExpr);
        NextTokenizerExpr right = new NextTokenizerExpr(newTokenizerExpr);
        assertEquals(left, right);
    }

    @Test
    public void testNotEquals() {
        StringConstant helloWorldStr = new StringConstant("Hello World");
        StringConstant goodByeWorldStr = new StringConstant("Goodbye World");
        StringConstant delimiterStr = new StringConstant(" ");
        NewTokenizerExpr leftNewTokenizerExpr = new NewTokenizerExpr(helloWorldStr, delimiterStr);
        NextTokenizerExpr left = new NextTokenizerExpr(leftNewTokenizerExpr);

        NewTokenizerExpr rightNewTokenizerExpr = new NewTokenizerExpr(goodByeWorldStr, delimiterStr);
        NextTokenizerExpr right = new NextTokenizerExpr(rightNewTokenizerExpr);
        assertNotEquals(left, right);
    }
}
