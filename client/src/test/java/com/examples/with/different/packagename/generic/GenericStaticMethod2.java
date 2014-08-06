package com.examples.with.different.packagename.generic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class GenericStaticMethod2 {

	public static <E> List<E> select(final Collection<? extends E> inputCollection,
            final Collection<? super E> predicate) {
		if(inputCollection.iterator().next() instanceof Integer) {
			System.out.println("OK 1");
		} else if(inputCollection.iterator().next() instanceof List) {
			System.out.println("OK 2");
		} 
        return new ArrayList<E>(inputCollection);
    }
	
}
