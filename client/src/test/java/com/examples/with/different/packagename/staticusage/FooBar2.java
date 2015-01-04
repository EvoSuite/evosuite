package com.examples.with.different.packagename.staticusage;

public class FooBar2 {

	public static int unused_int_field = 0;
	
	public static int used_int_field = 0;
	
	public static void init_unused_int_field() {
		unused_int_field = Integer.MAX_VALUE;
	}
	public static void init_used_int_field() {
		used_int_field = Integer.MIN_VALUE;
	}
}
