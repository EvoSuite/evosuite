package com.examples.with.different.packagename.contracts;

public class EqualsNull {

	
	public boolean equals(Object o) {
		if(o == null)
			return true;
		else
			return o == this;
	}
}
