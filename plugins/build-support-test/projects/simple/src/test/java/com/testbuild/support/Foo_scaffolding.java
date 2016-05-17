package com.testbuild.support;

/**
 * Important that this file name ends with "scaffolding" and
 * has a method called "initializeClasses"
 */
public class Foo_scaffolding {

    private static void initializeClasses() {
        org.evosuite.runtime.classhandling.ClassStateSupport.initializeClasses(Foo_scaffolding.class.getClassLoader() ,
                "com.testbuild.support.Foo"
        );
    }
}
