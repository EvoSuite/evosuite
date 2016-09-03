package com.examples.with.different.packagename.reflection;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by gordon on 03/09/2016.
 */
public class PrivateMethodWithTypeVariable<T extends Number> {
    private String toPathString(final List<T> pathElements) {
        // Just to have some branches...
        if(pathElements.isEmpty())
            return "";

        return pathElements.stream().map(i -> i.toString()).collect(Collectors.joining("/"));
    }
}
