package com.testbuild.support;

import java.io.File;

public class Foo {

    public static boolean doesFileExist(){
        return new File("data"+File.separator+"foo.txt").exists();
    }
}
