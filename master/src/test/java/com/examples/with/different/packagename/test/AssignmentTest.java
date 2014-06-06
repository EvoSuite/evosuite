package com.examples.with.different.packagename.test;

public class AssignmentTest {

	class Foo {
		public int x = 0;
	}

	public Foo foo = new Foo();

	public void foo(AssignmentTest other) {
		if (other.foo.x != 0 && foo.x != 0) {
			// Target
			System.out.println("test");
		}
	}

	public void bar(AssignmentTest other) {
		if (other.foo.x != 0 && foo.x != 0) {
			if (other.foo.x != foo.x) {
				// Target
				System.out.println("test");
			}
		}
	}
}
