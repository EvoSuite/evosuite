package com.examples.with.different.packagename.classpath;

public class Foo {

    static {
        new Foo(){@Override public String toString(){return "just force creation of anonymous class";}};
    }


	public static class InternalFooClass{
		
	}
}
