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
package org.evosuite.symbolic.solver;

import java.math.BigDecimal;

public abstract class ResultParser {

    private static final int BIG_DECIMAL_SCALE = 100;

    protected static Double parseRational(boolean sign, String numeratorStr, String denominatorStr) {
        double value;
        try {
            double numerator = Double.parseDouble(numeratorStr);
            double denominator = Double.parseDouble(denominatorStr);
            value = (numerator / denominator);
        } catch (NumberFormatException ex) {
            // Perhaps the numerator or denominator are just bigger than
            // Long.MAX_VALUE
            BigDecimal bigNumerator = new BigDecimal(numeratorStr);
            BigDecimal bigDenominator = new BigDecimal(denominatorStr);
            BigDecimal rational = bigNumerator.divide(bigDenominator, BIG_DECIMAL_SCALE, BigDecimal.ROUND_UP);
            value = rational.doubleValue();
        }
        if (sign == true) {
            return -value;
        } else {
            return value;
        }
    }

}
