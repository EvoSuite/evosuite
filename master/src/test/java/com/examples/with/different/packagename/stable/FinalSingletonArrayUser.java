package com.examples.with.different.packagename.stable;

public class FinalSingletonArrayUser {

	final static byte[] CONSTANT_ARRAY;
	
	static int counter = 0;
	
	static {
		CONSTANT_ARRAY = "\r\n".getBytes();
		counter++;
	}
	
	private final byte[] myArray;
	
	public FinalSingletonArrayUser() {
		myArray = new byte[2];
		System.arraycopy(CONSTANT_ARRAY, 0, myArray, 0, CONSTANT_ARRAY.length);
	}
	
	public boolean isEqualToFirst(byte myFloat) {
		if (myFloat==CONSTANT_ARRAY[0])
			return true;
		else
			return false;
	}
	
	public boolean isEqualToSecond(byte myFloat) {
		if (myFloat==CONSTANT_ARRAY[1])
			return true;
		else
			return false;
	}
	
	public boolean isFirstZero() {
		if (CONSTANT_ARRAY[0]==(byte)0)
			return true;
		else
			return false;
	}

	public boolean isSecondZero() {
		if (CONSTANT_ARRAY[1]==(byte)0)
			return true;
		else
			return false;
	}

	public static void clear() {
		CONSTANT_ARRAY[0] = -1;
		CONSTANT_ARRAY[1] = -1;
 	}

}
