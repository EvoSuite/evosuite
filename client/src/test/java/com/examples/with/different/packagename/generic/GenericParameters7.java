package com.examples.with.different.packagename.generic;

import java.util.ArrayList;
import java.util.List;

public class GenericParameters7 {

	private Object test = new Object();
	
	public boolean testMe(List<Object> targetList) {
		if(targetList.get(0) == test)
			return true;				
		else
			return false;
	}
	
	@SuppressWarnings("rawtypes")
	public List getTargetList() {
		List<Object> targetList = new ArrayList<Object>();
		targetList.add(test);
		return targetList;
	}
	
}
