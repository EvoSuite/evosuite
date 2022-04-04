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
package org.evosuite.symbolic.expr.ref.array;

import org.junit.Test;

import static org.junit.Assert.*;

public class SymbolicArrayUtilTest {

    @Test
    public void buildArrayContentVariableNameExceptions() {
        IllegalArgumentException arrayName = null;
        IllegalArgumentException negativeindex = null;

        try {
            SymbolicArrayUtil.buildArrayContentVariableName(null, 0);
        } catch (IllegalArgumentException e) {
            arrayName = e;
        }

        try {
            SymbolicArrayUtil.buildArrayContentVariableName("var", -3);
        } catch (IllegalArgumentException e) {
            negativeindex = e;
        }

        assertNotNull(arrayName);
        assertNotNull(negativeindex);

        assertEquals(SymbolicArrayUtil.ARRAY_NAME_CANNOT_BE_NULL, arrayName.getMessage());
        assertEquals(SymbolicArrayUtil.ARRAY_INDEX_CANNOT_BE_LOWER_THAN_0, negativeindex.getMessage());
    }

    @Test
    public void isArrayContentVariableNameExceptions() {
        IllegalArgumentException arrayName = null;

        try {
            SymbolicArrayUtil.isArrayContentVariableName(null);
        } catch (IllegalArgumentException e) {
            arrayName = e;
        }

        assertNotNull(arrayName);
        assertEquals(SymbolicArrayUtil.ARRAY_NAME_CANNOT_BE_NULL, arrayName.getMessage());
    }

    @Test
    public void isArrayContentVariableName() {
        assertTrue(
                SymbolicArrayUtil.isArrayContentVariableName(
                        SymbolicArrayUtil.buildArrayContentVariableName("arr0", 3)
                )
        );
    }
}