package com.examples.with.different.packagename.dse.array;

public class ArrayLengthExample {

	public ArrayLengthExample() {
	}

	public static int length(int[] array) {
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

	public static int length(int[] array, int a) {
		array[2] = a;

		if (array[2] == 2) {
			return 1;
		} else if (array[1] + a == 3){
			return 0;
		} else {
			return -1;
		}
	}

}