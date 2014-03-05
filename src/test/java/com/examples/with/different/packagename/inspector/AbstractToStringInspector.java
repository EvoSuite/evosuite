package com.examples.with.different.packagename.inspector;

public abstract class AbstractToStringInspector {

	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(super.toString());
		return builder.toString();
	}
}
