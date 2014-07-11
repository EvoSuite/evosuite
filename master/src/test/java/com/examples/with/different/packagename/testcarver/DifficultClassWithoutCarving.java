package com.examples.with.different.packagename.testcarver;

public class DifficultClassWithoutCarving {

	public boolean testMe(DifficultDependencyClass dependency) {
		if(dependency.isTen())
			return true;
		else
			return false;
	}
}
