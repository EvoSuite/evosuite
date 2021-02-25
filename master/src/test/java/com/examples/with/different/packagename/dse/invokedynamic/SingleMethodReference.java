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

/**
 * Tests for Method references implementation (InvokeDynamic)
 *
 * @author Ignacio Lebrero
 */
public class SingleMethodReference {

    interface GetIntContext {
        int testContext();
    }

    interface GetIntNoContext {
        int testNoContext(int y);
    }

    private static class MyIntegerClassWithState implements GetIntContext {
        private int val;

        public MyIntegerClassWithState(int x) {
            this.val = x;
        }

        @Override
        public int testContext() {
            if (this.val == 5)
                // TODO: This branch should be reached when the symbolic receiver gets fixed
                return 0;
            else
                return 2;
        }
    }

    private static class MyIntegerClassStateless implements GetIntNoContext {

        @Override
        public int testNoContext(int y) {
            if (y == 5)
                return 0;
            else
                return 2;
        }

    }

    public static int instanceRefNoContext(int y) {
        MyIntegerClassStateless myInt = new MyIntegerClassStateless();

        GetIntNoContext magic = myInt::testNoContext;
        return magic.testNoContext(y);
    }

    public static int instanceRefContext(int y) {
        MyIntegerClassWithState myInt = new MyIntegerClassWithState(y);

        GetIntContext magic = myInt::testContext;
        return magic.testContext();
    }
}