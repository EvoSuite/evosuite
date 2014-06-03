package com.examples.with.different.packagename.staticfield;

public class UnstableAssertion {

	private static boolean flag;
	private static int value;

	public void setFlag(int x) {
		if (flag == false) {
			flag = true;
			return;
		}
	}

	public boolean getFlag() {
		return flag;
	}
	
	public void setValue(int x) {
		if (flag==false) {
			flag=true;
			if (x==0) {
				value=0;
			} else {
				value=-1;
			}
		} else {
			value= +1;
		}
	}
	
	public int getValue() {
		return value;
	}
	
	

}
