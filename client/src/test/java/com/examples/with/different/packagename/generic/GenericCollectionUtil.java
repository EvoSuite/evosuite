package com.examples.with.different.packagename.generic;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class GenericCollectionUtil {

    public static <E> List<E> intersection(final List<? extends E> list1, final List<? extends E> list2) {
        final List<E> result = new ArrayList<E>();

        List<? extends E> smaller = list1;
        List<? extends E> larger = list2;
        if (list1.size() > list2.size()) {
            smaller = list2;
            larger = list1;
        }

        final HashSet<E> hashSet = new HashSet<E>(smaller);

        for (final E e : larger) {
            if (hashSet.contains(e)) {
                result.add(e);
                hashSet.remove(e);
            }
        }
        return result;
    }
}
