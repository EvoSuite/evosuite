package com.examples.with.different.packagename.assertion;

public class ArrayPrimitiveWrapper {

	public byte[] toPrimitive(Byte[] values) {
		byte[] ret = new byte[values.length];
		for(int i = 0; i < values.length; i++)
			ret[i] = values[i];
		return ret;
	}
	
}
