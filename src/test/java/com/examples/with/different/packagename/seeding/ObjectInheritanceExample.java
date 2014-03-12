package com.examples.with.different.packagename.seeding;

public class ObjectInheritanceExample {

	public static abstract class A {
		public abstract boolean fooBar();
	}
	
	public static class B extends A {
		@Override
		public boolean fooBar() {
			return false;
		}
	}

	public static class C extends A {
		private boolean value = false;
		
		public void setFooBar(boolean value) {
			this.value = value;
			
		}
		@Override
		public boolean fooBar() {
			return value;
		}
	}

	public boolean testMe(Object o) {
		A a = (A)o;
		if(a.fooBar())
			return true;
		else
			return false;
		
	}
	
}
