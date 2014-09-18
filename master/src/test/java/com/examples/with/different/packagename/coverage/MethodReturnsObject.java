package com.examples.with.different.packagename.coverage;

import java.util.Calendar;

public class MethodReturnsObject {

	public Object testObject(Integer integer){
		if(integer==null){
			return null;
		} else {
			return new Object();
		}
	}
}
