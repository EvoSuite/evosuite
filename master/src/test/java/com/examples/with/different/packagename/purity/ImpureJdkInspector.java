package com.examples.with.different.packagename.purity;

import java.util.Vector;

public class ImpureJdkInspector {

	private final Vector<String> myVector;

	public ImpureJdkInspector(int size) {
		if (size < 0)
			throw new IllegalArgumentException();

		this.myVector = new Vector<String>(size);
	}

	public int getPureSize() {
		return this.myVector.size();
	}

	public int getImpureSize() {
		this.myVector.clear();
		return 0;
	}
}
