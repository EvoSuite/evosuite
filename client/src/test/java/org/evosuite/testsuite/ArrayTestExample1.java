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

package org.evosuite.testsuite;

import org.junit.Ignore;
import org.junit.Test;

/**
 * @author Gordon Fraser
 */
public class ArrayTestExample1 {
    @Ignore
    @Test
    public void test1() {
        int[] test = new int[10];
    }

    @Ignore
    @Test
    public void test2() {
        int[] test = new int[10];
        test[5] = 7;
        test[7] = 3;
        test[9] = 6;
    }

    @Ignore
    @Test
    public void test3() {
        int[] test = new int[10];
        int[] test2 = new int[2];
        test[5] = 7;
        test[7] = 3;
        test[9] = 6;
    }

    @Ignore
    @Test
    public void test4() {
        int[] test = new int[10];
        test[0] = 7;
        test[1] = 7;
        test[2] = 7;
        test[3] = 7;
        test[4] = 7;
        test[5] = 7;
        test[6] = 7;
        test[7] = 3;
        test[9] = 6;
    }

    @Ignore
    @Test
    public void test5() {
        int[] test = new int[2];
        int x = 7;
        test[0] = x;
        test[1] = x;
    }
}
