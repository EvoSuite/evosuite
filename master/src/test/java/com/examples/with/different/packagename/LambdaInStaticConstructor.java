package com.examples.with.different.packagename;

import java.io.File;

public class LambdaInStaticConstructor {

    static {

        File[] libDirs = new File[] { new File("foo"), new File("bar") };
        for (File libDir : libDirs) {
            libDir.listFiles((dir, name) -> name.endsWith(".jar"));
        }

    }
}
