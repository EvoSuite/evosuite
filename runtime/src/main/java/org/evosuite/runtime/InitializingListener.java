/**
 * Copyright (C) 2010-2016 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 * <p/>
 * This file is part of EvoSuite.
 * <p/>
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3.0 of the License, or
 * (at your option) any later version.
 * <p/>
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 * <p/>
 * You should have received a copy of the GNU Lesser General Public
 * License along with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.runtime;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;

import org.evosuite.runtime.agent.InstrumentingAgent;
import org.junit.runner.Description;
import org.junit.runner.notification.RunListener;

/**
 * When running tests from a build tool (eg  "mvn test" when using Maven)
 * we need to use this listener (eg, in Maven configured from pom.xml).
 * Reason is to address the following two issues:
 * <p/>
 * <ul>
 * <li>
 * "manual" tests executed before EvoSuite ones that
 * lead to load system under test classes while the Java Agent is not active.
 * </li>
 * <li>
 *     Even if no manual test is run, still JUnit could still load some system
 *     under test classes before Java Agent is activated, messing up with
 *     the internal bytecode instrumentation used for environment mocking (eg
 *     file system and networking)
 * </li>
 * </ul>
 * <p/>
 * <p/>
 * Note: bytecode re-instrumenting is not really an option,
 * as it has its limitations (eg don't change signature of classes/methods)
 *
 * @author arcuri
 */
public class InitializingListener extends RunListener {

    /**
     * Name of the method that is used to initialize the SUT classes
     */
    public static final String INITIALIZE_CLASSES_METHOD = "initializeClasses";

    /**
     * File name of list of scaffolding files to use for initialization.
     */
    public static final String SCAFFOLDING_LIST_FILE_STRING = ".scaffolding_list.tmp";

    /**
     * Property used for example in Ant to specify where the EvoSuite tests have been compiled.
     */
    public static final String COMPILED_TESTS_FOLDER_PROPERTY = "EvoSuiteCompiledTestFolder";


    //TODO: need to move out of this class, as to avoid dependency on JUnit RunListener
    public static String getScaffoldingListFilePath() {
        //we could use a system property here if we want to change location
        return SCAFFOLDING_LIST_FILE_STRING;
    }

    @Override
    public void testRunStarted(Description description) throws Exception {

        java.lang.System.out.println("Executing " + InitializingListener.class.getName());

		/*
            Here we cannot trust what passed as "Description", as it could had
			been not initialized. This is for example the case for Maven, and
			who knows what would be in Ant and Gradle.
		 */


        /*
            This is not 100% correct, but anyway this is done only when running tests with "mvn test"
            by the final users, not really in the experiments.
            So, activating everything should be fine
         */
        RuntimeSettings.activateAllMocking();
        RuntimeSettings.mockSystemIn = true;
        RuntimeSettings.resetStaticState = true;

        List<String> list;
        String compiledTestsFolder = java.lang.System.getProperty(COMPILED_TESTS_FOLDER_PROPERTY);

        /*
            We have 2 different approaches based on Maven and Ant.
            TODO: we ll need to handle also Gradle, and possibly find a simpler, unified way
         */
        if(compiledTestsFolder == null){
            list = classesToInitFromScaffoldingFile();
        } else {
            list = InitializingListenerUtils.scanClassesToInit(new File(compiledTestsFolder));
        }

        InstrumentingAgent.initialize();

        for (String name : list) {
            Method m = null;
            try {
                //reflection might load some SUT class
                InstrumentingAgent.activate();
                Class<?> test = InitializingListener.class.getClassLoader().loadClass(name);
                m = test.getDeclaredMethod(INITIALIZE_CLASSES_METHOD);
                m.setAccessible(true);
            } catch (NoSuchMethodException e) {
                /*
				 * this is ok.
				 * Note: we could skip the test based on some pattern on the
				 * name, but not really so important in the end
				 */
            } catch (Exception e) {
                java.lang.System.out.println("Exception while loading class " + name + ": " + e.getMessage());
            } finally {
                InstrumentingAgent.deactivate();
            }

            if (m == null) {
                continue;
            }

            try {
                m.invoke(null);
            } catch (Exception e) {
                java.lang.System.out.println("Exception while calling " + name + "." + INITIALIZE_CLASSES_METHOD + "(): " + e.getMessage());
            }
        }
    }




    private List<String> classesToInitFromScaffoldingFile() {

        List<String> list = new ArrayList<>();

        File scaffolding = new File(InitializingListener.SCAFFOLDING_LIST_FILE_STRING);
        if (!scaffolding.exists()) {
            java.lang.System.out.println(
                    "WARN: scaffolding file not found. If this module has tests, recall to call the preparation step " +
                            "before executing the tests. For example, in Maven you need to make sure that " +
                            "'evosuite:prepare' is called. See documentation at www.evosuite.org for further details.");
            return list;
        }

        try (Scanner in = new Scanner(scaffolding)){
            while (in.hasNext()) {
                list.add(in.next().trim());
            }
        } catch (Exception e) {
            java.lang.System.out.println("ERROR while reading scaffolding list file: " + e.getMessage());
        }

        return list;
    }
}
