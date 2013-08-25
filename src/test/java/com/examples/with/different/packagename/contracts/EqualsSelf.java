package com.examples.with.different.packagename.contracts;

public class EqualsSelf {

	public boolean equals(Object other) {
		if(this == other)
			return false;
		else
			return other.equals(this);
	}
	
}
