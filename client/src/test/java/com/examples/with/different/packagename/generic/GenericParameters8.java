package com.examples.with.different.packagename.generic;

import java.util.List;

public class GenericParameters8 {

	@SuppressWarnings("rawtypes")
	public boolean testMe(List list) {
		if(list.get(0) instanceof String) {
			return true;
		} else {
			return false;
		}
	}
}
