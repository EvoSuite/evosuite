package com.examples.with.different.packagename.assertion;

public class ArrayObjects {

	public Object[] testMe(Object[] parameter) {
		Object[] newArray = new Object[parameter.length];
		for(int i = 0; i < parameter.length; i++)
			if(parameter[i] != null)
				newArray[i] = parameter[i];
		return newArray;
	}
	
	
}
