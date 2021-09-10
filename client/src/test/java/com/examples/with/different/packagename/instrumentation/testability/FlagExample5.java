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
package com.examples.with.different.packagename.instrumentation.testability;

public class FlagExample5 {

    private boolean testMeHelper(int x, int y) {
        boolean flag = false;
        if (x == 34235) {
            if (x == y || y == -20362)
                flag = true;
        }
        return flag;
    }

    public boolean testMe(int x, int y) {
        if (testMeHelper(x, y)) {
            return true;
        } else {
            return false;
        }
    }
}
