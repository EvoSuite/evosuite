package com.examples.with.different.packagename.stable;

public class DoubleArrayDefault {

	private final Double[] doubleArray;

	public DoubleArrayDefault(Double[] myDoubleArray) {
		this.doubleArray = myDoubleArray;
	}

	public boolean isEmpty() {
		return this.doubleArray.length == 0;
	}

	public boolean moreThanTwo() {
		if (this.doubleArray.length > 2)
			return true;
		else
			return false;
	}
	public boolean moreThanTwoAndNull() {
		if (moreThanTwo() &&  isNull())
			return true;
		else
			return false;
	}
	
	
	public boolean moreThanTwoAndNonNull() {
		if (moreThanTwo() &&  !isNull())
			return true;
		else
			return false;
	}
	public boolean isNull() {
		for (int i = 0; i < doubleArray.length; i++) {
			Double f = doubleArray[i];
			if (f != null)
				return false;
		}
		return true;
	}

	public String printArray() {
		StringBuffer b = new StringBuffer();
		for (int i = 0; i < doubleArray.length; i++) {
			Double f = doubleArray[i];
			String f_str = f.toString();
			b.append(f_str);
		}
		return b.toString();
	}
}
