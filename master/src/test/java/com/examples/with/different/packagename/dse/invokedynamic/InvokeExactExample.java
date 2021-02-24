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
