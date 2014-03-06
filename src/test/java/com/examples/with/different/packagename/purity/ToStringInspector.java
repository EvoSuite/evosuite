package com.examples.with.different.packagename.purity;

public class ToStringInspector extends AbstractToStringInspector {

	private String prefix;

	public ToStringInspector(String str) {
		if (str == null) {
			throw new IllegalArgumentException();
		}
		this.prefix = str;
	}

	public String getPrefix() {
		return prefix;
	}

	@Override
	public String toString() {
		return this.getPrefix() + " " + super.toString();
	}
}
