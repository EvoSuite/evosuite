package org.maven_test_project.sm;

public class OneDependencyClass{

	static{
		String empty = org.apache.commons.lang.StringUtils.EMPTY;
	}
	
	public boolean isPositive(int x){
		if(x>0){
			return true;
		} else {
			return false;
		}
	}
	
}