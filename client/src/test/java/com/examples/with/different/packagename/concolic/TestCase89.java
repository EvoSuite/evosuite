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


public class TestCase89 {

    public class InnerClass {

        private int innerField;

        public InnerClass(int val) {
            innerField = TestCase89.this.outerField;
            outerField = val;
        }

    }

    protected int outerField = 4;

    public static void test(int int0) {
        TestCase89 outerClassInstance = new TestCase89();
        InnerClass innerClass = outerClassInstance.new InnerClass(
                int0);
        int int1 = outerClassInstance.outerField;
        Assertions.checkEquals(int0, int1);
    }
}
