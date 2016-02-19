/**
 * Copyright (C) 2010-2016 Gordon Fraser, Andrea Arcuri and EvoSuite
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
package com.examples.with.different.packagename.reflection;

import java.util.Random;

/**
 * Created by foo on 11/12/15.
 */
public class CoverageIssue {

    private static Random rnd = new Random();

    public static boolean getNextBoolean(double prob) {
        return rnd.nextBoolean();
    }

    public static int nextPos(int n) {
        int nn = rnd.nextInt(n*(n+1)/2) + 1;

        int i;
        for (i=1;(i<=n) && (i*(i-1)/2<nn); i++) { }
        return i-1;
    }

}
