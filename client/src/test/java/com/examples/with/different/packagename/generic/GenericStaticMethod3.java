package com.examples.with.different.packagename.generic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class GenericStaticMethod3 {

    public static <E> List<E> selectRejected(final Collection<? extends E> inputCollection,
            final Predicate<? super E> predicate) {
    	List<E> result = new ArrayList<E>();
    	for(E elem : inputCollection) {
    		if(predicate.evaluate(elem)) {
    			result.add(elem);
    		}
    	}
        return result;
    }
    
    public static <E> List<E> foo(E bla) {
    	List<E> ret = new ArrayList<E>();
    	ret.add(bla);
    	if(bla instanceof Character) {
    		System.out.println("Foo");
    	}
    	if(bla instanceof String) {
    		System.out.println("Bar");
    	}
    	return ret;
    }
}
