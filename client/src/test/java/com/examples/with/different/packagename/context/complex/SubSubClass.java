package com.examples.with.different.packagename.context.complex;

public class SubSubClass implements ISubSubClass{
 	
	public boolean innermethod(int i){
		if(i>0) return true;
		return false;
	}
	
	public boolean somethingElse(int i) {
		if(i>0) return false;
		return true;
	}
	
}
