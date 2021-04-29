package org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser;

import java.net.URL;
import java.net.URLClassLoader;

public class AnalyzingClassLoader extends URLClassLoader {
    public AnalyzingClassLoader(URL[] urls) {
        super(urls);
    }


    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        return super.loadClass(name);
    }
}
