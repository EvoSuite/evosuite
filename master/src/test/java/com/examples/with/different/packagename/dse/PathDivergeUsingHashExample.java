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
package com.examples.with.different.packagename.dse;

import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Classic path divergence problem example.
 */
public class PathDivergeUsingHashExample {
    public PathDivergeUsingHashExample() {
    }

    public static int test(int x, int y) {
        if (x != new HashCodeBuilder().append(y).build()) {
            if (y == 10) {
                return -1;
            } else {
                return 1;
            }
        }

        return 0;
    }
}
