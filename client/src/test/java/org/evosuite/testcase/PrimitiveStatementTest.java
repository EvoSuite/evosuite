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
package org.evosuite.testcase;

import org.evosuite.testcase.statements.PrimitiveStatement;
import org.evosuite.testcase.statements.StringPrimitiveStatement;
import org.evosuite.testcase.statements.numeric.IntPrimitiveStatement;
import org.junit.Assert;
import org.junit.Test;

public class PrimitiveStatementTest {

    @Test
    public void testSame() {

        TestCase tc = new DefaultTestCase();
        PrimitiveStatement<?> aInt = new IntPrimitiveStatement(tc, 42);
        Assert.assertTrue(aInt.same(aInt));
        Assert.assertFalse(aInt.same(null));

        PrimitiveStatement<?> fooString = new StringPrimitiveStatement(tc, "foo");
        Assert.assertFalse(aInt.same(fooString));

        PrimitiveStatement<?> nullString = new StringPrimitiveStatement(tc, null);
        Assert.assertFalse(nullString.same(fooString));
        Assert.assertFalse(fooString.same(nullString));


        //TODO: how to make it work?
        //PrimitiveStatement<?> anotherNullString = new StringPrimitiveStatement(tc,null);
        //Assert.assertTrue(nullString.same(anotherNullString));
        //Assert.assertTrue(anotherNullString.same(nullString));
    }
}
