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

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Simple closure example
 *
 * @author Ignacio Lebrero
 */
public class TestClosureClass {
    private static List<Integer> list = new ArrayList();

    public static int closureTest(int in) {
        if (in == 0) list.add(1);

        Function<Integer, Integer> testClosure = (a) -> {
            if (check(list, a)) return 0;
            return 1;
        };

        return testClosure.apply(in);
    }

    private static boolean check(List<Integer> vals, int val) {
        return vals.size() > 0 || val > 6;
    }
}