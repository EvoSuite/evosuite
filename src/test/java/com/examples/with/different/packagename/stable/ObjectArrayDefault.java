package com.examples.with.different.packagename.stable;

public class ObjectArrayDefault {

	private final Object[] objectArray;

	public ObjectArrayDefault(Object[] myFloatArray) {
		this.objectArray = myFloatArray;
	}

	public boolean isEmpty() {
		return this.objectArray.length == 0;
	}

	public boolean isNull() {
		for (int i = 0; i < objectArray.length; i++) {
			Object f = objectArray[i];
			if (f != null)
				return false;
		}
		return true;
	}
	
	public String printArray() {
		StringBuffer b = new StringBuffer();
		for (int i = 0; i < objectArray.length; i++) {
			Object f = objectArray[i];
			String f_str = f.toString();
			b.append(f_str);
		}
		return b.toString();
	}
}
