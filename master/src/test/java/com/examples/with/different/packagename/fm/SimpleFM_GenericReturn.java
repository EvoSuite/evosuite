package com.examples.with.different.packagename.fm;

import java.util.List;

public class SimpleFM_GenericReturn {

	public static interface Foo<T> {
	
		public List<T> foo();
	}
	
    public boolean bar(Foo<String> bar, String x){
        if(bar.foo().isEmpty())
        	return true;
        else
        	return false;
    }
}
