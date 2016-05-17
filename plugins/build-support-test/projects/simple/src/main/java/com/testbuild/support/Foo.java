package com.testbuild.support;

import java.io.File;

public class Foo {

    public static boolean doesFileExist(){
        return new File("data"+File.separator+"foo.txt").exists()
                //just due to how tests are run, eg Ant
                || new File("projects"+File.separator+"simple"+File.separator+"data"+File.separator+"foo.txt").exists();
    }
}
