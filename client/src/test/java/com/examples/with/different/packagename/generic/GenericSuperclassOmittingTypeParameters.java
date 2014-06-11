package com.examples.with.different.packagename.generic;

import java.util.ArrayList;

@SuppressWarnings("rawtypes")
public class GenericSuperclassOmittingTypeParameters extends ArrayList {

	private static final long serialVersionUID = 1L;

	public boolean testMe(Object o) {
		if(super.contains(o))
			return true;
		else
			return false;
	}
	
}
