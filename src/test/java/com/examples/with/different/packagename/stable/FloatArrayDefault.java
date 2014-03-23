package com.examples.with.different.packagename.stable;

public class FloatArrayDefault {

	private final Float[] floatArray;

	public FloatArrayDefault(Float[] myFloatArray) {
		this.floatArray = myFloatArray;
	}

	public boolean isEmpty() {
		return this.floatArray.length == 0;
	}

	public boolean isNull() {
		for (int i = 0; i < floatArray.length; i++) {
			Float f = floatArray[i];
			if (f != null)
				return false;
		}
		return true;
	}
	
	public String printArray() {
		StringBuffer b = new StringBuffer();
		for (int i = 0; i < floatArray.length; i++) {
			Float f = floatArray[i];
			String f_str = f.toString();
			b.append(f_str);
		}
		return b.toString();
	}
	
	/*
	public boolean moreThanTwoAndNonNull() {
		if (moreThanTwo() &&  isNonNull())
			return true;
		else
			return false;
	}
	
	
	public boolean moreThanTwoAndNull() {
		if (moreThanTwo() &&  !isNonNull())
			return true;
		else
			return false;
	}

	public boolean moreThanTwo() {
		if (this.floatArray.length > 2)
			return true;
		else
			return false;
	}
	*/
}
