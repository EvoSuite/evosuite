/*
 * Copyright (C) 2010-2018 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3.0 of the License, or
 * (at your option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.junit;

import com.examples.with.different.packagename.junit.Foo;
import org.apache.commons.io.FileUtils;
import org.junit.Test;

import javax.tools.*;
import javax.tools.JavaCompiler.CompilationTask;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.Charset;
import java.util.*;

import static org.junit.Assert.assertNotNull;

public class FooTestClassLoader {

    private static final String FOO_TEST_CLASS_NAME = "com.examples.with.different.packagename.junit.FooTest";

    @Test
    public void testFooTestClassCreation() throws IOException {
        Class<?> clazz = loadFooTestClass();
        assertNotNull(clazz);
    }

    private static String createFooTestJavaText() {
        StringBuffer buff = new StringBuffer();
        buff.append("package com.examples.with.different.packagename.junit;\n");
        buff.append("import static org.junit.Assert.assertEquals;\n");
        buff.append("import org.junit.Test;\n");
        buff.append("public class FooTest {\n");

        buff.append("	@Test\n");
        buff.append("	public void test1() {\n");
        buff.append("		Foo foo = new Foo();\n");
        buff.append("		int result = foo.add(10, 15);\n");
        buff.append("		assertEquals(25, result);\n");
        buff.append("	}\n");

        buff.append("	@Test\n");
        buff.append("	public void test2() {\n");
        buff.append("		Foo foo = new Foo();\n");
        buff.append("		int result = foo.add(20, 35);\n");
        buff.append("		assertEquals(55, result);\n");
        buff.append("	}\n");

        buff.append("	@Test \n");
        buff.append("	public void test3() {\n");
        buff.append("		Foo foo = new Foo();\n");
        buff.append("		int result = foo.add(10, 35);\n");
        buff.append("		assertEquals(46, result);\n");
        buff.append("	}\n");

        buff.append("}\n");

        return buff.toString();
    }

    private static File createNewTmpDir() {
        File dir = null;
        String dirName = FileUtils.getTempDirectoryPath() + File.separator
                + "EvoSuite_" + JUnitResultBuilderTest.class.getCanonicalName()
                + "_" + +System.currentTimeMillis();

        //first create a tmp folder
        dir = new File(dirName);
        if (!dir.mkdirs()) {
            return null;
        }

        if (!dir.exists()) {
            return null;
        }

        return dir;
    }

    public Class<?> loadFooTestClass() {

        File tempDir = createNewTmpDir();
        if (tempDir == null) {
            return null; //fail
        }

        String javaSrcDirName = tempDir.getAbsolutePath() + File.separator
                + "src";

        String javaBinDirName = tempDir.getAbsolutePath() + File.separator
                + "bin";

        String javaSrcPackageDirName = javaSrcDirName
                + File.separator
                + "com.examples.with.different.packagename.junit".replace(".",
                File.separator);

        String javaBinPackageDirName = javaBinDirName
                + File.separator
                + "com.examples.with.different.packagename.junit".replace(".",
                File.separator);

        File javaBinDir = new File(javaBinDirName);
        if (!javaBinDir.mkdirs()) {
            return null; //fail
        }

        File packageDir = new File(javaSrcPackageDirName);
        if (!packageDir.mkdirs()) {
            return null; //fail
        }

        String javaFilename = javaSrcPackageDirName + File.separator
                + "FooTest.java";
        String classFilename = javaBinPackageDirName + File.separator
                + "FooTest.class";

        String javaSourceText = createFooTestJavaText();
        File javaFile = writeJavaSourceText(javaSourceText, javaFilename);
        if (javaFile == null) {
            return null; //fail
        }

        boolean compiled = compileJavaFile(javaBinDirName, javaFile);
        if (!compiled) {
            return null; //fail
        }

        File classFile = new File(classFilename);
        if (!classFile.exists()) {
            return null; //fail
        }

        Class<?> clazz = loadClass(javaBinDir);
        if (clazz == null) {
            return null; //fail
        }

        //delete class file on exit
        classFile.delete();
        return clazz;
    }

    private static Class<?> loadClass(File javaBinDir) {

        URLClassLoader urlClassLoader = null;
        try {
            URI javaBinURI = javaBinDir.toURI();
            URL javaBinURL = javaBinURI.toURL();
            urlClassLoader = new URLClassLoader(new URL[]{javaBinURL},
                    Foo.class.getClassLoader());
            Class<?> clazz = urlClassLoader.loadClass(FOO_TEST_CLASS_NAME);
            return clazz;
        } catch (ClassNotFoundException e) {
            return null;
        } catch (MalformedURLException e) {
            return null;
        }
    }

    private static boolean compileJavaFile(String javaBinDirName, File javaFile) {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            return false; //fail
        }

        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(
                diagnostics, Locale.getDefault(), Charset.forName("UTF-8"));
        Iterable<? extends JavaFileObject> compilationUnits = fileManager
                .getJavaFileObjectsFromFiles(Collections
                        .singletonList(javaFile));

        List<String> optionList;
        optionList = new ArrayList<>();
        optionList.addAll(Arrays.asList("-d", javaBinDirName));
        CompilationTask task = compiler.getTask(null, fileManager, diagnostics,
                optionList, null, compilationUnits);
        boolean compiled = task.call();
        try {
            fileManager.close();
        } catch (IOException e) {
            return false;
        }
        return compiled;
    }

    private File writeJavaSourceText(String javaSourceText, String javaFilename) {
        File javaFile = new File(javaFilename);
        if (javaFile.exists()) {
            return null; //fail
        }

        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(javaFilename));
            writer.write(javaSourceText);
            writer.close();
        } catch (IOException e) {
            return null;
        }
        return javaFile;
    }
}
