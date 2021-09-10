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
package org.evosuite.testcase.variable;

import org.evosuite.Properties;
import org.evosuite.symbolic.expr.bv.IntegerValue;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ArrayLengthSymbolicUtilTest {

    @Test
    public void buildArraySymbolicLengthExpression() {
        ArraySymbolicLengthName arraySymbolicLengthName = mock(ArraySymbolicLengthName.class);
        when(arraySymbolicLengthName.getSymbolicName()).thenReturn("test_var");
        IntegerValue integerValue;

        //When Arrays enabled, it should create an IntegerVariable
        Properties.IS_DSE_ARRAYS_SUPPORT_ENABLED = true;
        integerValue = ArrayLengthSymbolicUtil.buildArraySymbolicLengthExpression(0, arraySymbolicLengthName);
        assertTrue(integerValue.containsSymbolicVariable());

        //When Arrays disabled, it should create an IntegerConstant so it won't propagate later on
        Properties.IS_DSE_ARRAYS_SUPPORT_ENABLED = false;
        integerValue = ArrayLengthSymbolicUtil.buildArraySymbolicLengthExpression(0, arraySymbolicLengthName);
        assertFalse(integerValue.containsSymbolicVariable());
    }

    @Test
    public void isSymbolicArraysSupportEnabled() {
        Properties.IS_DSE_ARRAYS_SUPPORT_ENABLED = true;
        assertTrue(ArrayLengthSymbolicUtil.isSymbolicArraysSupportEnabled());

        Properties.IS_DSE_ARRAYS_SUPPORT_ENABLED = false;
        assertFalse(ArrayLengthSymbolicUtil.isSymbolicArraysSupportEnabled());
    }
}