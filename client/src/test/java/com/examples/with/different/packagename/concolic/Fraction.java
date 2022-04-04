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
package com.examples.with.different.packagename.concolic;


public class Fraction {

    /**
     * A fraction representing "1/5".
     */
    public static final Fraction ONE_FIFTH = new Fraction(1, 5);

    /**
     * The denominator.
     */
    private final int denominator;

    /**
     * The numerator.
     */
    private final int numerator;

    /**
     * Create a fraction given the numerator and denominator.  The fraction is
     * reduced to lowest terms.
     *
     * @param num the numerator.
     * @param den the denominator.
     * @throws ArithmeticException if the denominator is <code>zero</code>
     */
    public Fraction(int num, int den) {
        if (den == 0) {
            throw MathRuntimeException.createArithmeticException("zero denominator in fraction {0}/{1}",
                    num, den);
        }
        if (den < 0) {
            if (num == Integer.MIN_VALUE || den == Integer.MIN_VALUE) {
                throw MathRuntimeException.createArithmeticException("overflow in fraction {0}/{1}, cannot negate",
                        num, den);
            }
            num = -num;
            den = -den;
        }
        // reduce numerator and denominator by greatest common denominator.
        final int d = MathUtils.gcd(num, den);
        if (d > 1) {
            num /= d;
            den /= d;
        }

        // move sign to numerator.
        if (den < 0) {
            num = -num;
            den = -den;
        }
        this.numerator = num;
        this.denominator = den;
    }

}
