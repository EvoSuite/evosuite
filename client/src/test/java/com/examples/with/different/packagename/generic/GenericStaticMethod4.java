package com.examples.with.different.packagename.generic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class GenericStaticMethod4 {

    public static <E> List<E> synchronizedList(final List<E> list) {
        return Collections.synchronizedList(list);
    }
    
    public static <E> List<E> select(final Collection<? extends E> inputCollection,
            final Predicate<? super E> predicate) {
    	List<E> result = new ArrayList<E>();
    	for(E elem : inputCollection) {
    		if(predicate.evaluate(elem)) {
    			result.add(elem);
    		}
    	}
        return result;
    }
	
    public static <E> List<E> selectRejected(final Collection<? extends E> inputCollection,
            final Predicate<? super E> predicate) {
    	List<E> result = new ArrayList<E>();
    	for(E elem : inputCollection) {
    		if(!predicate.evaluate(elem)) {
    			result.add(elem);
    		}
    	}
        return result;
    }
}
