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

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.evosuite.Properties;
import org.evosuite.TestGenerationContext;
import org.evosuite.TimeController;
import org.evosuite.classpath.ClassPathHandler;
import org.evosuite.instrumentation.NonInstrumentingClassLoader;
import org.evosuite.junit.writer.TestSuiteWriter;
import org.evosuite.junit.writer.TestSuiteWriterUtils;
import org.evosuite.runtime.classhandling.JDKClassResetter;
import org.evosuite.runtime.sandbox.Sandbox;
import org.evosuite.runtime.util.JarPathing;
import org.evosuite.testcase.TestCase;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.launcher.*;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.tools.*;
import javax.tools.JavaCompiler.CompilationTask;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.platform.engine.discovery.ClassNameFilter.includeClassNamePatterns;

/**
 * This class is used to check if a set of test cases are valid for JUnit: ie,
 * if they can be compiled, they do not fail, and if running them a second time
 * produces same result (ie not fail).
 *
 * @author arcuri
 */
public abstract class JUnitAnalyzer {

    private static final Logger logger = LoggerFactory.getLogger(JUnitAnalyzer.class);

    private static int dirCounter = 0;

    private static final String JAVA = ".java";
    private static final String CLASS = ".class";

    private static NonInstrumentingClassLoader loader = new NonInstrumentingClassLoader();

    private static final VersionDependentAnalyzing versionDependentAnalyzer;

    static {
        versionDependentAnalyzer = Properties.TEST_FORMAT == Properties.OutputFormat.JUNIT5 ?
                new JUnit5Analyzing() : new JUnit4Analyzing();
    }

    /**
     * Try to compile each test separately, and remove the ones that cannot be
     * compiled
     *
     * @param tests
     */
    public static void removeTestsThatDoNotCompile(List<TestCase> tests) {

        logger.info("Going to execute: removeTestsThatDoNotCompile");

        if (tests == null || tests.isEmpty()) { //nothing to do
            return;
        }

        Iterator<TestCase> iter = tests.iterator();

        while (iter.hasNext()) {
            if (!TimeController.getInstance().hasTimeToExecuteATestCase()) {
                break;
            }

            TestCase test = iter.next();

            File dir = createNewTmpDir();
            if (dir == null) {
                logger.warn("Failed to create tmp dir");
                return;
            }
            logger.debug("Created tmp folder: " + dir.getAbsolutePath());

            try {
                List<TestCase> singleList = new ArrayList<>();
                singleList.add(test);
                List<File> generated = compileTests(singleList, dir);
                if (generated == null) {
                    iter.remove();
                    String code = test.toCode();
                    logger.error("Failed to compile test case:\n" + code);
                }
            } finally {
                //let's be sure we clean up all what we wrote on disk
                if (dir != null) {
                    try {
                        FileUtils.deleteDirectory(dir);
                        logger.debug("Deleted tmp folder: " + dir.getAbsolutePath());
                    } catch (Exception e) {
                        logger.error("Cannot delete tmp dir: " + dir.getAbsolutePath(), e);
                    }
                }
            }

        } // end of while
    }

    /**
     * Compile and run all the test cases, and mark as "unstable" all the ones
     * that fail during execution (ie, unstable assertions).
     *
     * <p>
     * If a test fail due to an exception not related to a JUnit assertion, then
     * remove such test from the input list
     *
     * @param tests
     * @return the number of unstable tests
     */
    public static int handleTestsThatAreUnstable(List<TestCase> tests) {

        int numUnstable = 0;
        logger.info("Going to execute: handleTestsThatAreUnstable");

        if (tests == null || tests.isEmpty()) { //nothing to do
            return numUnstable;
        }

        File dir = createNewTmpDir();
        if (dir == null) {
            logger.error("Failed to create tmp dir");
            return numUnstable;
        }
        logger.debug("Created tmp folder: " + dir.getAbsolutePath());

        try {
            List<File> generated = compileTests(tests, dir);
            if (generated == null) {
                /*
                 * Note: in theory this shouldn't really happen, as check for compilation
                 * is done before calling this method
                 */
                logger.warn("Failed to compile the test cases ");
                return numUnstable;
            }

            if (!TimeController.getInstance().hasTimeToExecuteATestCase()) {
                logger.error("Ran out of time while checking tests");
                return numUnstable;
            }

            // Create a new classloader so that each test gets freshly loaded classes
            loader = new NonInstrumentingClassLoader();
            Class<?>[] testClasses = loadTests(generated);

            if (testClasses == null) {
                logger.error("Found no classes for compiled tests");
                return numUnstable;
            }

            JUnitResult result = runTests(testClasses, dir);

            if (result.wasSuccessful()) {
                return numUnstable; //everything is OK
            }


            failure_loop:
            for (JUnitFailure failure : result.getFailures()) {
                String testName = failure.getDescriptionMethodName();//TODO check if correct
                for (int i = 0; i < tests.size(); i++) {
                    if (TestSuiteWriterUtils.getNameOfTest(tests, i).equals(testName)) {
                        if (tests.get(i).isFailing()) {
                            logger.info("Failure is expected, continuing...");
                            continue failure_loop;
                        }
                    }
                }

                if (testName == null) {
                    /*
                     * this can happen if there is a failure in the scaffolding (eg @AfterClass/@BeforeClass).
                     * in such case, everything need to be deleted
                     */
                    StringBuilder sb = new StringBuilder();
                    sb.append("Issue in scaffolding of the test suite: ").append(failure.getMessage()).append("\n");
                    sb.append("Stack trace:\n");
                    for (String elem : failure.getExceptionStackTrace()) {
                        sb.append(elem).append("\n");
                    }
                    logger.error(sb.toString());
                    numUnstable = tests.size();
                    tests.clear();
                    return numUnstable;
                }

                // On the Sheffield cluster, the "well-known fle is not secure" issue is impossible to understand,
                // so it might be best to ignore it for now.
                if (testName.equals("initializationError") && failure.getMessage().contains("Failed to attach Java Agent")) {
                    logger.warn("Likely error with EvoSuite instrumentation, ignoring failure in test execution");
                    continue failure_loop;
                }


                logger.warn("Found unstable test named " + testName + " -> "
                        + failure.getExceptionClassName() + ": " + failure.getMessage());

                for (String elem : failure.getExceptionStackTrace()) {
                    logger.info("Exception trace: {}", elem);
                }

                boolean toRemove = !(failure.isAssertionError());

                for (int i = 0; i < tests.size(); i++) {
                    if (TestSuiteWriterUtils.getNameOfTest(tests, i).equals(testName)) {
                        logger.warn("Failing test:\n " + tests.get(i).toCode());
                        numUnstable++;
                        /*
                         * we have a match. should we remove it or mark as unstable?
                         * When we have an Assert.* failing, we can just comment out
                         * all the assertions in the test case. If it is an "assert"
                         * in the SUT that fails, we do want to have the JUnit test fail.
                         * On the other hand, if a test fail due to an uncaught exception,
                         * we should delete it, as it would either represent a bug in EvoSuite
                         * or something we cannot (easily) fix here
                         */
                        if (!toRemove) {
                            logger.debug("Going to mark test as unstable: " + testName);
                            tests.get(i).setUnstable(true);
                        } else {
                            logger.debug("Going to remove unstable test: " + testName);
                            tests.remove(i);
                        }
                        break;
                    }
                }
            }
        } catch (Exception e) {
            logger.error("" + e, e);
            return numUnstable;
        } finally {
            //let's be sure we clean up all what we wrote on disk

            if (dir != null) {
                try {
                    FileUtils.deleteDirectory(dir);
                } catch (Exception e) {
                    logger.warn("Cannot delete tmp dir: " + dir.getName(), e);
                }
            }

        }

        //if we arrive here, then it means at least one test was unstable
        return numUnstable;
    }

    private static JUnitResult runTests(Class<?>[] testClasses, File testClassDir)
            throws JUnitExecutionException {
        return runJUnitOnCurrentProcess(testClasses);
    }


    private static JUnitResult runJUnitOnCurrentProcess(Class<?>[] testClasses) {
        return versionDependentAnalyzer.runJUnitOnCurrentProcess(testClasses);
    }

    /**
     * Check if it is possible to use the Java compiler.
     *
     * @return
     */
    public static boolean isJavaCompilerAvailable() {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        return compiler != null;
    }

    // We have to have a unique name for this test suite as it is loaded by the
    // EvoSuite classloader, and thus cannot easily be re-loaded
    private static int NUM = 0;

    private static List<File> compileTests(List<TestCase> tests, File dir) {

        TestSuiteWriter suite = new TestSuiteWriter();
        suite.insertAllTests(tests);

        //to get name, remove all package before last '.'
        int beginIndex = Properties.TARGET_CLASS.lastIndexOf(".") + 1;
        String name = Properties.TARGET_CLASS.substring(beginIndex);
        name += "_" + (NUM++) + "_tmp_" + Properties.JUNIT_SUFFIX; //postfix

        try {
            //now generate the JUnit test case
            List<File> generated = suite.writeTestSuite(name, dir.getAbsolutePath(), Collections.EMPTY_LIST);
            for (File file : generated) {
                if (!file.exists()) {
                    logger.error("Supposed to generate " + file
                            + " but it does not exist");
                    return null;
                }
            }

            //try to compile the test cases
            JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
            if (compiler == null) {
                logger.error("No Java compiler is available");
                return null;
            }

            DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
            Locale locale = Locale.getDefault();
            Charset charset = StandardCharsets.UTF_8;
            StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics,
                    locale,
                    charset);

            Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjectsFromFiles(generated);

            String evosuiteCP = ClassPathHandler.getInstance().getEvoSuiteClassPath();
            if (JarPathing.containsAPathingJar(evosuiteCP)) {
                evosuiteCP = JarPathing.expandPathingJars(evosuiteCP);
            }

            String targetProjectCP = ClassPathHandler.getInstance().getTargetProjectClasspath();
            if (JarPathing.containsAPathingJar(targetProjectCP)) {
                targetProjectCP = JarPathing.expandPathingJars(targetProjectCP);
            }

            String classpath = targetProjectCP + File.pathSeparator + evosuiteCP;

            List<String> optionList = new ArrayList<>(Arrays.asList("-classpath", classpath));

            CompilationTask task = compiler.getTask(null, fileManager, diagnostics,
                    optionList, null, compilationUnits);
            boolean compiled = task.call();
            fileManager.close();

            if (!compiled) {
                logger.error("Compilation failed on compilation units: " + compilationUnits);
                logger.error("Classpath: " + classpath);
                //TODO remove
                logger.error("evosuiteCP: " + evosuiteCP);


                for (Diagnostic<?> diagnostic : diagnostics.getDiagnostics()) {
                    if (diagnostic.getMessage(null).startsWith("error while writing")) {
                        logger.error("Error is due to file permissions, ignoring...");
                        return generated;
                    }
                    logger.error("Diagnostic: " + diagnostic.getMessage(null) + ": "
                            + diagnostic.getLineNumber());
                }

                StringBuffer buffer = new StringBuffer();
                for (JavaFileObject sourceFile : compilationUnits) {
                    List<String> lines = FileUtils.readLines(new File(sourceFile.toUri().getPath()));

                    buffer.append(compilationUnits.iterator().next().toString() + "\n");

                    for (int i = 0; i < lines.size(); i++) {
                        buffer.append((i + 1) + ": " + lines.get(i) + "\n");
                    }
                }
                logger.error(buffer.toString());
                return null;
            }

            return generated;

        } catch (IOException e) {
            logger.error("" + e, e);
            return null;
        }
    }

    protected static File createNewTmpDir() {
        File dir = null;
        String dirName = FileUtils.getTempDirectoryPath() + File.separator + "EvoSuite_"
                + (dirCounter++) + "_" + +System.currentTimeMillis();

        //first create a tmp folder
        dir = new File(dirName);
        if (!dir.mkdirs()) {
            logger.error("Cannot create tmp dir: " + dirName);
            return null;
        }

        if (!dir.exists()) {
            logger.error("Weird behavior: we created folder, but Java cannot determine if it exists? Folder: "
                    + dirName);
            return null;
        }

        return dir;
    }

    private static Class<?>[] loadTests(List<File> tests) {

        /*
         * Ideally, when we run a generated test case, it
         * will automatically use JavaAgent to instrument the CUT.
         * But here we have already loaded the CUT by now, so that
         * mechanism will not work.
         *
         * A simple option is to just use an instrumenting class loader,
         * as it does exactly the same type of instrumentation.
         * But a better idea would be to use a new
         * non-instrumenting classloader to re-load the CUT, and so see
         * if the JavaAgent works properly.
         */
        Class<?>[] testClasses = getClassesFromFiles(tests);
        List<File> otherClasses = listOnlyFiles(tests);
        /*
         * this is important to force the loading of all files generated
         * in the target folder.
         * If we do not do that, then we will miss all the anonymous classes
         */
        getClassesFromFiles(otherClasses);

        return testClasses;
    }

    private static List<File> listOnlyFiles(List<File> tests) throws IllegalArgumentException {
        if (tests == null || tests.isEmpty()) {
            return null;
        }

        Set<String> classNames = new LinkedHashSet<>();

        File parentFolder = tests.get(0).getParentFile();
        for (File file : tests) {
            if (!file.getParentFile().equals(parentFolder)) {
                throw new IllegalArgumentException("Tests file are not in the same folder");
            }
            classNames.add(removeFileExtension(file.getName()));
        }

        /*
         * if we already loaded a CUT due to its .java, do not want
         * to re-loaded it for a .class file that is in the same folder
         */

        List<File> otherClasses = new LinkedList<>();

        for (File file : parentFolder.listFiles()) {
            String name = removeFileExtension(file.getName());

            if (classNames.contains(name)) {
                continue;
            }

            classNames.add(name);
            otherClasses.add(file);
        }

        return otherClasses;
    }


    private static String removeFileExtension(String str) {
        if (str == null) {
            return null;
        }

        int pos = str.lastIndexOf(".");

        if (pos == -1) {
            return str;
        }

        return str.substring(0, pos);
    }

    /**
     * <p>
     * The output of EvoSuite is a set of test cases. For debugging and
     * experiment, we usually would not write any JUnit to file. But we still
     * want to see if test cases can compile and execute properly. As EvoSuite
     * is supposed to only capture the current behavior of the SUT, all
     * generated test cases should pass.
     * </p>
     *
     * <p>
     * Here we compile to a tmp folder, load and execute the test cases, and
     * then clean up (ie delete all generated files).
     * </p>
     *
     * @param tests
     * @return
     * @deprecated not used anymore, as check are done in different methods now, and old "assert" was not really valid
     */
    public static boolean verifyCompilationAndExecution(List<TestCase> tests) {

        if (tests == null || tests.isEmpty()) {
            //nothing to compile or run
            return true;
        }

        File dir = createNewTmpDir();
        if (dir == null) {
            logger.warn("Failed to create tmp dir");
            return false;
        }

        try {
            List<File> generated = compileTests(tests, dir);
            if (generated == null) {
                logger.warn("Failed to compile the test cases ");
                return false;
            }

            //as last step, execute the generated/compiled test cases

            Class<?>[] testClasses = loadTests(generated);

            if (testClasses == null) {
                logger.error("Found no classes for compiled tests");
                return false;
            }

            JUnitResult result = runTests(testClasses, dir);

            if (!result.wasSuccessful()) {
                logger.error("" + result.getFailureCount() + " test cases failed");
                for (JUnitFailure failure : result.getFailures()) {
                    logger.error("Failure " + failure.getExceptionClassName() + ": "
                            + failure.getMessage() + "\n" + failure.getTrace());
                }
                return false;
            } else {
                /*
                 * OK, it was successful, but was there any test case at all?
                 *
                 * Here we just log (and not return false), as it might be that EvoSuite is just not able to generate
                 * any test case for this SUT
                 */
                if (result.getRunCount() == 0) {
                    logger.warn("There was no test to run");
                }
            }

        } catch (Exception e) {
            logger.error("" + e, e);
            return false;
        } finally {
            //let's be sure we clean up all what we wrote on disk
            if (dir != null) {
                try {
                    FileUtils.deleteDirectory(dir);
                } catch (IOException e) {
                    logger.warn("Cannot delete tmp dir: " + dir.getName(), e);
                }
            }
        }

        logger.debug("Successfully compiled and run test cases generated for "
                + Properties.TARGET_CLASS);
        return true;
    }

    /**
     * Given a list of files representing .java/.class classes, load them (it
     * assumes the classpath to be correctly set)
     *
     * @param files
     * @return
     */
    private static Class<?>[] getClassesFromFiles(Collection<File> files) {
        /*
         * first load only the scaffolding files
         */
        for (File file : files) {
            if (!isScaffolding(file)) {
                continue;
            }
            loadClass(file);
        }

        List<Class<?>> classes = new ArrayList<>();

        /*
         * once the scaffoldings are loaded, we can load the tests that
         * depend on them
         */
        for (File file : files) {
            if (isScaffolding(file)) {
                continue;
            }
            Class<?> clazz = loadClass(file);
            if (clazz != null) {
                classes.add(clazz);
            }
        }

        return classes.toArray(new Class<?>[classes.size()]);
    }

    private static boolean isScaffolding(File file) {
        String name = file.getName();
        return name.endsWith("_" + Properties.SCAFFOLDING_SUFFIX + JAVA) ||
                name.endsWith("_" + Properties.SCAFFOLDING_SUFFIX + CLASS);
    }

    private static Class<?> loadClass(File file) {
        if (!file.isFile()) {
            return null;
        }

        String packagePrefix = Properties.CLASS_PREFIX;
        if (!packagePrefix.isEmpty() && !packagePrefix.endsWith(".")) {
            packagePrefix += ".";
        }

        String name = file.getName();

        if (!name.endsWith(JAVA) && !name.endsWith(CLASS)) {
            /*
             * this could happen when we scan a folder for all src/compiled
             * files
             */
            return null;
        }

        String fileName = file.getAbsolutePath();

        if (name.endsWith(JAVA)) {
            name = name.substring(0, name.length() - JAVA.length());
            fileName = fileName.substring(0, fileName.length() - JAVA.length()) + ".class";
        } else {
            assert name.endsWith(CLASS);
            name = name.substring(0, name.length() - CLASS.length());
        }

        String className = packagePrefix + name;

        Class<?> testClass = null;
        try {
            logger.info("Loading class " + className);
            //testClass = ((InstrumentingClassLoader) TestGenerationContext.getInstance().getClassLoaderForSUT()).loadClassFromFile(className,
            testClass = loader.loadClassFromFile(className, fileName);
        } catch (ClassNotFoundException e) {
            logger.error("Failed to load test case " + className + " from file "
                    + file.getAbsolutePath() + " , error " + e, e);
        }
        return testClass;
    }

    /**
     * Class defining what functionality must be defined for different JUNIT versions.
     */
    private static abstract class VersionDependentAnalyzing {
        abstract JUnitResult runJUnitOnCurrentProcess(Class<?>[] testClasses);
    }

    /**
     * Define functionality for JUnit 4 Tests.
     */
    private static class JUnit4Analyzing extends VersionDependentAnalyzing {
        @Override
        JUnitResult runJUnitOnCurrentProcess(Class<?>[] testClasses) {

            JUnitCore runner = new JUnitCore();

            /*
             * Why deactivating the sandbox? This is pretty tricky.
             * The JUnitCore runner will execute the test cases on a new
             * thread, which might not be privileged. If the test cases need
             * the JavaAgent, then they will fail due to the sandbox :(
             * Note: if the test cases need a sandbox, they will have code
             * to do that by their self. When they do it, the initialization
             * will be after the agent is already loaded.
             */
            boolean wasSandboxOn = Sandbox.isSecurityManagerInitialized();

            Set<Thread> privileged = null;
            if (wasSandboxOn) {
                privileged = Sandbox.resetDefaultSecurityManager();
            }

            Result result = null;
            ClassLoader currentLoader = Thread.currentThread().getContextClassLoader();

            try {
                TestGenerationContext.getInstance().goingToExecuteSUTCode();
                Thread.currentThread().setContextClassLoader(testClasses[0].getClassLoader());
                JDKClassResetter.reset(); //be sure we reset it here, otherwise "init" in the test case would take current changed state
                result = runner.run(testClasses);
            } finally {
                Thread.currentThread().setContextClassLoader(currentLoader);
                TestGenerationContext.getInstance().doneWithExecutingSUTCode();
            }


            if (wasSandboxOn) {
                //only activate Sandbox if it was already active before
                if (!Sandbox.isSecurityManagerInitialized())
                    Sandbox.initializeSecurityManagerForSUT(privileged);
            } else {
                if (Sandbox.isSecurityManagerInitialized()) {
                    logger.warn("EvoSuite problem: tests set up a security manager, but they do not remove it after execution");
                    Sandbox.resetDefaultSecurityManager();
                }
            }

            JUnitResultBuilder builder = new JUnitResultBuilder();
            return builder.build(result);
        }
    }


    /**
     * Define functionality for JUnit 5 tests.
     */
    private static class JUnit5Analyzing extends VersionDependentAnalyzing {

        @Override
        JUnitResult runJUnitOnCurrentProcess(Class<?>[] testClasses) {

            boolean wasSandboxOn = Sandbox.isSecurityManagerInitialized();

            Set<Thread> privileged = null;
            if (wasSandboxOn) {
                privileged = Sandbox.resetDefaultSecurityManager();
            }

            List<Pair<TestIdentifier, TestExecutionResult>> result = new ArrayList<>();
            ClassLoader currentLoader = Thread.currentThread().getContextClassLoader();


            try {
                TestGenerationContext.getInstance().goingToExecuteSUTCode();
                Thread.currentThread().setContextClassLoader(testClasses[0].getClassLoader());
                JDKClassResetter.reset(); //be sure we reset it here, otherwise "init" in the test case would take current changed state
                LauncherDiscoveryRequest request_ = LauncherDiscoveryRequestBuilder.request()
                        .selectors(Arrays.stream(testClasses).map(DiscoverySelectors::selectClass).collect(Collectors.toList()))
                        .filters(includeClassNamePatterns(".*Test"))
                        .build();
                Launcher launcher = LauncherFactory.create();
                TestPlan testPlan = launcher.discover(request_);
                launcher.registerTestExecutionListeners(new TestExecutionListener() {
                    @Override
                    public void executionFinished(TestIdentifier testIdentifier, TestExecutionResult testExecutionResult) {
                        result.add(Pair.of(testIdentifier, testExecutionResult));
                    }
                });

                launcher.execute(request_);
            } finally {
                Thread.currentThread().setContextClassLoader(currentLoader);
                TestGenerationContext.getInstance().doneWithExecutingSUTCode();
            }


            if (wasSandboxOn) {
                //only activate Sandbox if it was already active before
                if (!Sandbox.isSecurityManagerInitialized())
                    Sandbox.initializeSecurityManagerForSUT(privileged);
            } else {
                if (Sandbox.isSecurityManagerInitialized()) {
                    logger.warn("EvoSuite problem: tests set up a security manager, but they do not remove it after execution");
                    Sandbox.resetDefaultSecurityManager();
                }
            }

            JUnitResultBuilder builder = new JUnitResultBuilder();
            return builder.build(result);
        }
    }
}
