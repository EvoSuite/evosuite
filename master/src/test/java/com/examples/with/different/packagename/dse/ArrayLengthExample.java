package com.examples.with.different.packagename.dse;

public class ArrayLengthExample {

	public ArrayLengthExample() {
	}

	public static int max(int[] array) {
		new ArrayLengthExample();
		if (array==null) {
			return -1;
		} else if (array.length==0) {
			return 0;
		} else if (array.length==1) {
			return 1;
		} else {
			return array.length;
		}
	}

}
