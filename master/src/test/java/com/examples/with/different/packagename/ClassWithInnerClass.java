package com.examples.with.different.packagename;

public class ClassWithInnerClass {

	public void foo(int x, int y) {
		if(x == 100) {
			AnInnerClass z = new AnInnerClass();
			z.foo(y);
		}
	}
	
	public static class AnInnerClass {
		public boolean foo(int x) {
			if(x == 42)
				return true;
			else
				return false;
		}
	};
}
