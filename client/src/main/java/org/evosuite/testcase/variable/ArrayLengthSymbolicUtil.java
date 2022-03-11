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
import org.evosuite.symbolic.expr.bv.IntegerConstant;
import org.evosuite.symbolic.expr.bv.IntegerValue;
import org.evosuite.symbolic.expr.bv.IntegerVariable;

/**
 * Helper methods for the Array length symbolic related things.
 *
 * @author Ignacio Lebrero
 */
public class ArrayLengthSymbolicUtil {

    /**
     * Represents the minimum value that an array dimension could have
     */
    public static final int ARRAY_DIMENSION_LENGTH_MIN_VALUE = 0;

    /**
     * Position of the dimension of an uni-dimensional array
     */
    public static final int UNIDIMENTIONAL_ARRAY_VALUE = 0;

    /**
     * Creates the expression for an array length.
     *
     * @param length
     * @param arraySymbolicLengthName
     * @return
     */
    public static IntegerValue buildArraySymbolicLengthExpression(int length, ArraySymbolicLengthName arraySymbolicLengthName) {
        IntegerValue lengthExpression;

        // If arrays support is enabled, we create the symbolic value
        if (isSymbolicArraysSupportEnabled()) {
            lengthExpression = new IntegerVariable(
                    arraySymbolicLengthName.getSymbolicName(),
                    length,
                    ARRAY_DIMENSION_LENGTH_MIN_VALUE,
                    Integer.MAX_VALUE);

            // Otherwise a constant value is created
        } else {
            lengthExpression = new IntegerConstant(length);
        }
        return lengthExpression;
    }

    /**
     * Checks if support for symbolic arrays is enabled.
     *
     * @return
     */
    public static boolean isSymbolicArraysSupportEnabled() {
        return Properties.IS_DSE_ARRAYS_SUPPORT_ENABLED;
    }
}
