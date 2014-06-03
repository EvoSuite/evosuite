package com.examples.with.different.packagename.purity;

public class ImpureEquals {

	private boolean flag =false;
	private final int value;
	
	public int getValue() {
		return value;
	}
	
	public boolean getFlag() {
		return flag;
	}
	
	public ImpureEquals(int value) {
		this.value = value;
	}
	@Override
	public int hashCode() {
		return this.value;
	}
	
	@Override
	public boolean equals(Object o) {
		setFlag();
		if (o==this)
			return true;
		if (o==null)
			return false;
		if (o.getClass().equals(ImpureEquals.class)) {
			ImpureEquals that =(ImpureEquals)o;
			return this.value==that.value;
		} else {
			return false;
		}
	}

	public  void setFlag() {
		flag = true;
	}

	public void clearFlag() {
		flag = false;
	}

}
