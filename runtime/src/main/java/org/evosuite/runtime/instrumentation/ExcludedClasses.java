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
package org.evosuite.runtime.instrumentation;

import org.evosuite.PackageInfo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * The Maven shade plugin replaces strings (https://issues.apache.org/jira/browse/MSHADE-156)
 * To avoid this problem, we have to store the strings in a file and read them here.
 * <p>
 * <p>
 * Created by gordon on 19/03/2016.
 */
public class ExcludedClasses {

    private static boolean classesLoaded = false;

    public static List<String> excludedClasses = new ArrayList<>();

    private static void loadExcludedClassNames() {
        if (classesLoaded)
            return;

        InputStream excludedClassesStream = ExcludedClasses.class.getClassLoader().getResourceAsStream("excluded.classes");
        classesLoaded = true;
        try {
            //Construct BufferedReader from InputStreamReader
            BufferedReader br = new BufferedReader(new InputStreamReader(excludedClassesStream));

            String line = null;
            while ((line = br.readLine()) != null) {
                excludedClasses.add(line);
            }

            br.close();
        } catch (IOException e) {

        }
    }

    /**
     * <p>
     * getPackagesShouldNotBeInstrumented
     * </p>
     *
     * @return the names of class packages EvoSuite is not going to instrument
     */
    static List<String> getPackagesShouldNotBeInstrumented() {
        //explicitly blocking client projects such as specmate is only a
        //temporary solution, TODO allow the user to specify
        //packages that should not be instrumented

        List<String> list = new ArrayList<>();
        loadExcludedClassNames();
        list.add(PackageInfo.getEvoSuitePackage());
        list.addAll(excludedClasses);

        //Note: be sure each package is ended with ".", otherwise you might ban more packages than you wanted

//        list.addAll(Arrays.asList(new String[]{"java.", "javax.", "sun.", PackageInfo.getEvoSuitePackage(), "org.exsyst",
//                "de.unisb.cs.st.testcarver.", "de.unisb.cs.st.evosuite.", "org.uispec4j.",
//                "de.unisb.cs.st.specmate.", "org.xml.", "org.w3c.",
//                "testing.generation.evosuite.", "com.yourkit.", "com.vladium.emma.", "daikon.",
//                "org.netbeans.lib.profiler.", // VisualVM profiler
//                // Need to have these in here to avoid trouble with UnsatisfiedLinkErrors on Mac OS X and Java/Swing apps
//                "apple.", "com.apple.", "com.sun.",
//                "org.junit.", "junit.framework.", // do not instrument test code which will be part of final JUnit
//                "org.apache.xerces.dom3.", "de.unisl.cs.st.bugex.",  "org.mozilla.javascript.gen.c.",
//                "corina.cross.Single",  // I really don't know what is wrong with this class, but we need to exclude it
//                "org.slf4j.",
//                "org.apache.log4j.", // Instrumenting this may lead to errors when tests are run with Ant, which uses log4j
//                "jdk.internal.",
//                "lombok.",//this is used to hook Javac in some projects, leading to weird compilation error of the JUnit tests
//                "dk.brics.automaton.", //used in DSE, and we have a class with that package inside EvoSutie
//                "org.apache.commons.discovery.tools.DiscoverSingleton",
//                "org.apache.commons.discovery.resource.ClassLoaders",
//                "org.apache.commons.discovery.resource.classes.DiscoverClasses",
//                "org.apache.commons.logging.Log",// Leads to ExceptionInInitializerException when re-instrumenting classes that use a logger
//                "org.jcp.xml.dsig.internal.dom.", //Security exception in ExecutionTracer?
//                "com_cenqua_clover.", "com.cenqua.", //these are for Clover code coverage instrumentation
//                "net.sourceforge.cobertura.", // cobertura code coverage instrumentation
//                "javafx.", // JavaFX crashes when instrumented
//                "ch.qos.logback.", // Instrumentation makes logger events sent to the master un-serialisable
//                "major.mutation.", // Runtime library Major mutation tool
//                "org.apache.lucene.util.SPIClassIterator", "org.apache.lucene.analysis.util.AnalysisSPILoader", "org.apache.lucene.analysis.util.CharFilterFactory",
//                "org.apache.struts.util.MessageResources", "org.dom4j.DefaultDocumentFactory" // These classes all cause problems with re-instrumentation
//        }));

        if (RuntimeInstrumentation.getAvoidInstrumentingShadedClasses()) {
            list.addAll(Arrays.asList(/*
             * TODO:
             * These classes are shaded. So, should be no problem in instrumenting them during search, even though
             * they are used by EvoSuite. However, problems arise when running system tests before shading :(
             * For now, we just skip them, but need to check if it leads to side effects
             *
             * Main problem due to libraries used in the generated JUnit files to test JavaEE applications relying on database
             *
             */
                    "org.hibernate.", "org.hsqldb.", "org.jboss.",
                    "org.springframework.", "org.apache.commons.logging.", "javassist.", "antlr.", "org.dom4j.",
                    "org.aopalliance.",
                    "javax.servlet.",//note, Servlet is special. see comments in pom file
                    "org.mockito.", "org.apache", "org.hamcrest", "org.objenesis"));
        }

        return list;
    }
}
