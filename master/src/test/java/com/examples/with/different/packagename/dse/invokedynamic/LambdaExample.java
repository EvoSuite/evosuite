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
package com.examples.with.different.packagename.dse.invokedynamic;

import java.util.function.Function;

/**
 * Simple lambda example code
 *
 * @author Ignacio Lebrero
 */
public class LambdaExample {

    public interface Function2<One, Two, Three> {
        public Three apply(One a, Two b);
    }

    public static int test(int in, int in2) {
        Function2<Integer, Integer, Integer> testLambda2 = (a, b) -> {
            if (a * b == 20) return 0;
            return 1;
        };

        Function<Integer, Integer> testLambda = (a) -> {
            if (a == 2) return 0;
            return 1;
        };

        int result1 = testLambda.apply(in);
        int result2 = testLambda2.apply(result1, in2);

        if (result2 == 0) {
            return 2;
        } else {
            return 1;
        }
    }

}
