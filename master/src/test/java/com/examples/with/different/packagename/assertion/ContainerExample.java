package com.examples.with.different.packagename.assertion;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ContainerExample {

    private List<String> stuff = new ArrayList<>();

    public Collection<String> add(String thing) {
        if(!thing.equals("foo"))
            stuff.add(thing);
        return stuff;
    }
}
