package com.examples.with.different.packagename.purity;

public class PureEquals {

	private final int value;
	
	public PureEquals(int value) {
		this.value = value;
	}
	@Override
	public int hashCode() {
		return this.value;
	}
	
	@Override
	public boolean equals(Object o) {
		if (o==this)
			return true;
		if (o==null)
			return false;
		if (o.getClass().equals(PureEquals.class)) {
			PureEquals that =(PureEquals)o;
			return this.value==that.value;
		} else {
			return false;
		}
	}
	
}
