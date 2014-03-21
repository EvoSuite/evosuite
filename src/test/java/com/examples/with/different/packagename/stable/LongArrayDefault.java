package com.examples.with.different.packagename.stable;

public class LongArrayDefault {

	private final Long[] longArray;

	public LongArrayDefault(Long[] myFloatArray) {
		this.longArray = myFloatArray;
	}

	public boolean isEmpty() {
		return this.longArray.length == 0;
	}

	public boolean isNull() {
		for (int i = 0; i < longArray.length; i++) {
			Long f = longArray[i];
			if (f != null)
				return false;
		}
		return true;
	}
	
	public String printArray() {
		StringBuffer b = new StringBuffer();
		for (int i = 0; i < longArray.length; i++) {
			Long f = longArray[i];
			String f_str = f.toString();
			b.append(f_str);
		}
		return b.toString();
	}
}
