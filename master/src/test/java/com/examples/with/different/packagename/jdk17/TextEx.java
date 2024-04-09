package com.examples.with.different.packagename.jdk17;

public class TextEx {

    public String makeString(){
        String str = "Test";

        return  """
            'The time has come,' the Walrus said,
            'To talk of many things: %s
            Of shoes -- and ships -- and sealing-wax --
            Of cabbages -- and kings --
            And why the sea is boiling hot --
            And whether pigs have wings.'
            """.formatted(str);
    }
}
