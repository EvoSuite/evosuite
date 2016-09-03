package com.examples.with.different.packagename.reflection;


import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by gordon on 03/09/2016.
 */
public class PrivateMethodWithGenerics {

    private static String toPathString(final List<String> pathElements) {
        // Just to have some branches...
        if(pathElements.isEmpty())
            return "";

        return pathElements.stream().collect(Collectors.joining("/"));
    }
}
