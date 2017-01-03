package com.examples.with.different.packagename.symbolic;

public class Foo {

	public static boolean bar(int x, int y, int z) {
		if (x==0)
			return false;
		
		if (y==0)
			return false;
		
		if (z==0)
			return false;
		
		if (x==y+z)
			return false;
		
		return true;
	}
}
