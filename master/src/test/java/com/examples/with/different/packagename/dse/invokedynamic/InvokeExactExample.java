/**
 * Copyright (C) 2010-2018 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 * <p>
 * This file is part of EvoSuite.
 * <p>
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3.0 of the License, or
 * (at your option) any later version.
 * <p>
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 * <p>
 * You should have received a copy of the GNU Lesser General Public
 * License along with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package com.examples.with.different.packagename.dse.invokedynamic;

import java.io.PrintStream;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

/**
 * Taken from Deep static analysis of invokeDynamic.
 *
 * @author Ignacio Lebrero
 */
public class InvokeExactExample {

    public static void test1() throws Throwable {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        MethodType mType = MethodType.methodType(void.class, String.class);
        MethodHandle println = lookup.findVirtual(PrintStream.class, "println", mType);
        println.invokeExact(System.out, "hello, world");

        int pos = 0;  // receiver in leading position
        MethodHandle println2out = MethodHandles.insertArguments(println, pos, System.out);
        println2out.invokeExact("hello, world");
    }
}
