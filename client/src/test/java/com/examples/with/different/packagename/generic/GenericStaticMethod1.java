package com.examples.with.different.packagename.generic;

import java.util.Collections;
import java.util.List;

public class GenericStaticMethod1 {
	public static <E> List<E> unmodifiableList(final List<E> list) {
		if(list.get(0) instanceof Character) {
			System.out.println("OK");
		}
        return Collections.unmodifiableList(list);
    }
}
