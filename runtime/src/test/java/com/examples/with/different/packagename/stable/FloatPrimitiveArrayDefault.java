package com.examples.with.different.packagename.stable;

public class FloatPrimitiveArrayDefault {

	private final float[] floatArray;

	public FloatPrimitiveArrayDefault(float[] myFloatArray) {
		this.floatArray = myFloatArray;
	}

	public boolean isEmpty() {
		return this.floatArray.length == 0;
	}

	public boolean isZero() {
		for (int i = 0; i < floatArray.length; i++) {
			float f = floatArray[i];
			if (f != 0)
				return false;
		}
		return true;
	}
	
	public String printArray() {
		StringBuffer b = new StringBuffer();
		for (int i = 0; i < floatArray.length; i++) {
			float f = floatArray[i];
			String f_str = String.valueOf(f);
			b.append(f_str);
		}
		return b.toString();
	}
	
	public boolean moreThanTwoAndZero() {
		if (moreThanTwo() &&  isZero())
			return true;
		else
			return false;
	}
/*	
	
	public boolean moreThanTwoAndNull() {
		if (moreThanTwo() &&  !isNonNull())
			return true;
		else
			return false;
	}

 */
	public boolean moreThanTwo() {
		if (this.floatArray.length > 2)
			return true;
		else
			return false;
	}
}
