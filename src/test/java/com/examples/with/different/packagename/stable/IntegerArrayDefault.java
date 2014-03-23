package com.examples.with.different.packagename.stable;

public class IntegerArrayDefault {

	private final Integer[] integerArray;

	public IntegerArrayDefault(Integer[] myFloatArray) {
		this.integerArray = myFloatArray;
	}

	public boolean isEmpty() {
		return this.integerArray.length == 0;
	}

	public boolean isNull() {
		for (int i = 0; i < integerArray.length; i++) {
			Integer f = integerArray[i];
			if (f != null)
				return false;
		}
		return true;
	}
	
	public String printArray() {
		StringBuffer b = new StringBuffer();
		for (int i = 0; i < integerArray.length; i++) {
			Integer f = integerArray[i];
			String f_str = f.toString();
			b.append(f_str);
		}
		return b.toString();
	}
}
