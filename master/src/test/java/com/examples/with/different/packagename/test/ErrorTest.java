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
package com.examples.with.different.packagename.test;

public class ErrorTest {

    public boolean useless = true;

    public int testMe(int x, int y) {
        return x / y;
    }

    public void arrayTest(char[] arr) {
        char x = arr[5];
    }

    public void testField(ErrorTest test1, ErrorTest test2) {
        test1.useless = test2.useless;
    }

    public void testCast(Object o) {
        ErrorTest test = (ErrorTest) o;
    }

    public void testMethod(ErrorTest test) {
        test.testMe(1, 1);
    }

    public void testOverflow(int x, int y) {
        int z = x + y;
    }

    public void testAssertion(int x) {
        assert (x != 1024);
    }
/*
  public void testOverflow(float x, float y) {
    float z = x + y;
  }
  public void testOverflow(double x, double y) {
    double z = x + y;
  }
  public void testOverflow(long x, long y) {
    long z = x + y;
  }
  */
}