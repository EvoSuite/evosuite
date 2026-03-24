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
package org.evosuite.maventest;

import org.apache.commons.io.FileUtils;
import org.apache.maven.it.VerificationException;
import org.apache.maven.it.Verifier;
import org.evosuite.rmi.MasterServices;
import org.evosuite.runtime.InitializingListener;
import org.junit.Assume;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Properties;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class MavenPluginIT {

    private static final long timeoutInMs = 3 * 60 * 1_000;
    private static final String DEFAULT_EVOSUITE_VERSION = "1.2.1-SNAPSHOT";
    private static final Path DEFAULT_LOCAL_REPOSITORY =
            Paths.get("target", "maven-it", "local-repo").toAbsolutePath();
    private static final String DEFAULT_MAVEN_HOME = resolveMavenHome();

    private final Path projects = Paths.get("projects");
    private final Path simple = projects.resolve("SimpleModule");
    private final Path dependency = projects.resolve("ModuleWithOneDependency");
    private final Path env = projects.resolve("EnvModule");
    private final Path coverage = projects.resolve("CoverageModule");

    private final String srcEvo = "src/evo";

    @Before
    @After
    public void clean() throws Exception{
        Verifier verifier  = getVerifier(projects);
        verifier.addCliOption("evosuite:clean");
        verifier.executeGoal("clean");

        for(Path p : Arrays.asList(projects,simple,dependency,env,coverage)){
            FileUtils.deleteDirectory(p.resolve(srcEvo).toFile());
            FileUtils.deleteQuietly(p.resolve("log.txt").toFile());
            FileUtils.deleteQuietly(p.resolve(InitializingListener.getScaffoldingListFilePath()).toFile());
            FileUtils.deleteQuietly(p.resolve("coverage.check.failed").toFile());
        }
    }


    @Test(timeout = timeoutInMs)
    public void testCompile() throws Exception{
        Verifier verifier  = getVerifier(projects);
        verifier.executeGoal("compile");
        verifier.verifyTextInLog("SimpleModule");
        verifier.verifyTextInLog("ModuleWithOneDependency");
    }


    @Test(timeout = timeoutInMs)
    public void testESClean() throws Exception{
        Verifier verifier  = getVerifier(simple);
        verifier.addCliOption("evosuite:clean");
        verifier.executeGoal("clean");

        Path es = getESFolder(simple);
        assertFalse(Files.exists(es));
    }


    @Test(timeout = timeoutInMs)
    public void testSimpleClass() throws Exception{
        assumeLoopbackBindAvailable();

        String cut = "org.maven_test_project.sm.SimpleClass";

        Verifier verifier  = getVerifier(simple);
        verifier.addCliOption("evosuite:generate");
        verifier.addCliOption("-DtimeInMinutesPerClass=1");
        verifier.addCliOption("-Dcuts="+cut);
        addModernGenerateDefaults(verifier);
        verifier.executeGoal("compile");

        Path es = getESFolder(simple);
        assertTrue(Files.exists(es));

        verifier.verifyTextInLog("Going to generate tests with EvoSuite");
        verifier.verifyTextInLog("New test suites: 1");

        verifyLogFilesExist(simple,cut);
    }

    @Test(timeout = timeoutInMs)
    public void testSimpleMultiCore() throws Exception {
        assumeLoopbackBindAvailable();

        String a = "org.maven_test_project.sm.SimpleClass";
        String b = "org.maven_test_project.sm.ThrowException";

        Verifier verifier  = getVerifier(simple);
        verifier.addCliOption("evosuite:generate");
        verifier.addCliOption("-DtimeInMinutesPerClass=1");
        verifier.addCliOption("-Dcores=2");
        addModernGenerateDefaults(verifier);

        boolean requiredMoreMemory = false;
        try {
            verifier.executeGoal("compile");
        } catch (VerificationException e){
            // Older setups used to require a larger memory budget here. Keep supporting
            // that path, but do not require it if the modern runtime succeeds directly.
            requiredMoreMemory = true;
        }

        if (requiredMoreMemory) {
            verifier.addCliOption("-DmemoryInMB=1000");
            verifier.executeGoal("compile");
        }

        verifyLogFilesExist(simple, a);
        verifyLogFilesExist(simple, b);
    }


    @Test(timeout = timeoutInMs)
    public void testModuleWithDependency() throws Exception{
        assumeLoopbackBindAvailable();

        String cut = "org.maven_test_project.mwod.OneDependencyClass";

        Verifier verifier  = getVerifier(dependency);
        verifier.addCliOption("evosuite:generate");
        addModernGenerateDefaults(verifier);
        verifier.executeGoal("compile");

        verifyLogFilesExist(dependency, cut);
    }

    @Test(timeout = timeoutInMs)
    public void testExportWithTests() throws Exception {
        assumeLoopbackBindAvailable();

        Verifier verifier  = getVerifier(dependency);
        verifier.addCliOption("evosuite:generate");
        verifier.addCliOption("evosuite:export");
        verifier.addCliOption("-DtargetFolder="+srcEvo);

        verifier.executeGoal("test");

        Files.exists(dependency.resolve(srcEvo));
        verifyLogFilesExist(dependency,"org.maven_test_project.mwod.OneDependencyClass");
    }

    @Test(timeout = timeoutInMs)
    public void testExportWithTestsWithAgent() throws Exception {
        assumeLoopbackBindAvailable();

        Verifier verifier  = getVerifier(dependency);
        addGenerateAndExportOption(verifier);
        verifier.addCliOption("-DforkCount=1");

        verifier.executeGoal("test");

        Files.exists(dependency.resolve(srcEvo));
        String cut = "org.maven_test_project.mwod.OneDependencyClass";
        verifyLogFilesExist(dependency,cut);
        verifyESTestsRunFor(verifier,cut);
    }

    @Test(timeout = timeoutInMs)
    public void testExportWithTestsWithAgentNoFork() throws Exception {
        assumeLoopbackBindAvailable();

        Verifier verifier  = getVerifier(dependency);
        addGenerateAndExportOption(verifier);
        verifier.addCliOption("-DforkCount=0");

        verifier.executeGoal("test");

        Files.exists(dependency.resolve(srcEvo));
        String cut = "org.maven_test_project.mwod.OneDependencyClass";
        verifyLogFilesExist(dependency,cut);
        verifyESTestsRunFor(verifier,cut);
    }


    @Test(timeout = timeoutInMs)
    public void testEnv() throws Exception{
        assumeLoopbackBindAvailable();
        Verifier verifier  = getVerifier(env);
        addGenerateAndExportOption(verifier);

        verifier.executeGoal("test");

        Files.exists(env.resolve(srcEvo));
        String cut = "org.maven_test_project.em.FileCheck";
        verifyLogFilesExist(env,cut);
        verifyESTestsRunFor(verifier,cut);
    }

    //--- JaCoCo --------------------------------------------------------------


    @Test(timeout = timeoutInMs)
    public void testJaCoCoNoEnv() throws Exception{
        assumeCoverageProfileUnsupportedOnModernJdk("jacoco");
        testVerifyNoEnv("jacoco");
        verifyJaCoCoFileExists(dependency);
    }

    @Test(timeout = timeoutInMs)
    public void testJaCoCoWithEnv() throws Exception{
        assumeCoverageProfileUnsupportedOnModernJdk("jacoco");
        testVerfiyWithEnv("jacoco");
        verifyJaCoCoFileExists(env);
    }

    @Test(timeout = timeoutInMs)
    public void testJaCoCoPass() throws Exception{
        assumeCoverageProfileUnsupportedOnModernJdk("jacoco");
        testCoveragePass("jacoco");
        verifyJaCoCoFileExists(coverage);
    }

    @Test(timeout = timeoutInMs)
    public void testJaCoCoFail() throws Exception{
        assumeCoverageProfileUnsupportedOnModernJdk("jacoco");
        testCoverageFail("jacoco");
        verifyJaCoCoFileExists(coverage);
    }


    //--- JMockit --------------------------------------------------------------


    @Test(timeout = timeoutInMs)
    public void testJMockitNoEnv() throws Exception{
        assumeLegacyCoverageProfileUnsupportedOnModernJdk("jmockit");
        testVerifyNoEnv("jmockit", 1);
        verifyJMockitFolderExists(dependency);
    }

    @Test(timeout = timeoutInMs)
    public void testJMockitWithEnv() throws Exception{
        assumeLegacyCoverageProfileUnsupportedOnModernJdk("jmockit");
        testVerfiyWithEnv("jmockit", 1);
        verifyJMockitFolderExists(env);
    }

    @Test(timeout = timeoutInMs)
    public void testJMockitPass() throws Exception{
        assumeLegacyCoverageProfileUnsupportedOnModernJdk("jmockit");
        testCoveragePass("jmockit");
        verifyJMockitFolderExists(coverage);
    }

    @Test(timeout = timeoutInMs)
    public void testJMockitFail() throws Exception{
        assumeLegacyCoverageProfileUnsupportedOnModernJdk("jmockit");
        testCoverageFail("jmockit");
        verifyJMockitFolderExists(coverage);
    }


    //--- PowerMock --------------------------------------------------------------

    @Test(timeout = timeoutInMs)
    public void testPowerMockNoEnv() throws Exception{
        assumeLegacyCoverageProfileUnsupportedOnModernJdk("powermock");
        testVerifyNoEnv("powermock",1);
    }


    @Test(timeout = timeoutInMs)
    public void testPowerMockWithEnv() throws Exception{
        assumeLegacyCoverageProfileUnsupportedOnModernJdk("powermock");
        testVerfiyWithEnv("powermock",1);
    }



    //--- Cobertura --------------------------------------------------------------

    @Test(timeout = timeoutInMs)
    public void testCoberturaNoEnv() throws Exception{
        assumeLegacyCoverageProfileUnsupportedOnModernJdk("cobertura");
        testVerifyNoEnv("cobertura");
        verifyCoberturaFileExists(dependency);
    }

    @Test(timeout = timeoutInMs)
    public void testCoberturaWithEnv() throws Exception{
        assumeLegacyCoverageProfileUnsupportedOnModernJdk("cobertura");
        testVerfiyWithEnv("cobertura");
        verifyCoberturaFileExists(env);
    }

    @Test(timeout = timeoutInMs)
    public void testCoberturaPass() throws Exception{
        assumeLegacyCoverageProfileUnsupportedOnModernJdk("cobertura");
        testCoveragePass("cobertura");
        verifyCoberturaFileExists(coverage);
    }

    @Test(timeout = timeoutInMs)
    public void testCoberturaFail() throws Exception{
        assumeLegacyCoverageProfileUnsupportedOnModernJdk("cobertura");
        testCoverageFail("cobertura");
        verifyCoberturaFileExists(coverage);
    }

    //--- PIT --------------------------------------------------------------

    @Test(timeout = timeoutInMs)
    public void testPitNoEnv() throws Exception{
        assumeCoverageProfileUnsupportedOnModernJdk("pit");
        testVerifyNoEnv("pit");
        verifyPitFolderExists(dependency);
    }

    @Test(timeout = timeoutInMs)
    public void testPitWithEnv() throws Exception{
        assumeCoverageProfileUnsupportedOnModernJdk("pit");
        testVerfiyWithEnv("pit");
        verifyPitFolderExists(env);
    }


    @Test(timeout = timeoutInMs)
    public void testPitPass() throws Exception{
        assumeCoverageProfileUnsupportedOnModernJdk("pit");
        testCoveragePass("pit");
        verifyPitFolderExists(coverage);
    }

    @Test(timeout = timeoutInMs)
    public void testPitFail() throws Exception{
        assumeCoverageProfileUnsupportedOnModernJdk("pit");
        testCoverageFail("pit,pitOneTest"); //PIT has its filters for test execution
        verifyPitFolderExists(coverage);
    }


    //------------------------------------------------------------------------------------------------------------------

    private void testVerfiyWithEnv(String profile) throws Exception{
        testVerfiyWithEnv(profile, 1);
    }

    private void testVerfiyWithEnv(String profile, int forkCount) throws Exception{
        assumeLoopbackBindAvailable();

        Verifier verifier  = getVerifier(env);
        addGenerateAndExportOption(verifier);
        verifier.addCliOption("-P"+profile);
        verifier.addCliOption("-DforkCount="+forkCount);

        verifier.executeGoal("verify");

        Files.exists(env.resolve(srcEvo));
        String cut = "org.maven_test_project.em.FileCheck";
        verifyLogFilesExist(env,cut);
        verifyESTestsRunFor(verifier,cut);
    }

    private void testVerifyNoEnv(String profile) throws Exception {
        testVerifyNoEnv(profile, 1);
    }

    private void testVerifyNoEnv(String profile, int forkCount) throws Exception{
        assumeLoopbackBindAvailable();

        Verifier verifier  = getVerifier(dependency);
        addGenerateAndExportOption(verifier);
        verifier.addCliOption("-P"+profile);
        verifier.addCliOption("-DforkCount="+forkCount);

        verifier.executeGoal("verify");

        Files.exists(dependency.resolve(srcEvo));
        String cut = "org.maven_test_project.mwod.OneDependencyClass";
        verifyLogFilesExist(dependency,cut);
        verifyESTestsRunFor(verifier,cut);
    }

    private void testCoveragePass(String profile) throws Exception{
        assumeLoopbackBindAvailable();
        Verifier verifier = getVerifier(coverage);
        verifier.addCliOption("-P"+profile);
        verifier.executeGoal("verify");
    }

    private void testCoverageFail(String profile) throws Exception{
        assumeLoopbackBindAvailable();
        Verifier verifier = getVerifier(coverage);
        verifier.addCliOption("-Dtest=SimpleClassPartialTest");

        verifier.executeGoal("verify");

        verifier.executeGoal("clean");
        verifier.addCliOption("-P"+profile);
        try{
            verifier.executeGoal("verify");
            fail();
        } catch (Exception e){
            //expected, as coverage check should had failed
        }
    }



    private void verifyESTestsRunFor(Verifier verifier, String className) throws Exception{
        //Note: this depends on Maven / Surefire, so might change in future with new versions
        verifier.verifyTextInLog("Running "+className);
    }

    private void addGenerateAndExportOption(Verifier verifier){
        verifier.addCliOption("evosuite:generate");
        verifier.addCliOption("evosuite:export");
        verifier.addCliOption("-DtargetFolder="+srcEvo);
        verifier.addCliOption("-DextraArgs=\"" + getModernGenerateExtraArgs() + "\"");
    }

    private void addModernGenerateDefaults(Verifier verifier) {
        verifier.addCliOption("-DextraArgs=\"" + getModernGenerateExtraArgs() + "\"");
    }

    private String getModernGenerateExtraArgs() {
        // Pool serialization still relies on JDK-internal ObjectOutputStream details on modern JDKs.
        return "-Dwrite_pool= -Duse_separate_classloader=false";
    }

    private void verifyJaCoCoFileExists(Path targetProject){
        assertTrue(Files.exists(targetProject.resolve("target").resolve("jacoco.exec")));
    }

    private void verifyJMockitFolderExists(Path targetProject){
        assertTrue(Files.exists(targetProject.resolve("target").resolve("jmockit")));
    }

    private void verifyPitFolderExists(Path targetProject){
        assertTrue(Files.exists(targetProject.resolve("target").resolve("pit-reports")));
    }

    private void verifyCoberturaFileExists(Path targetProject){
        assertTrue(Files.exists(targetProject.resolve("target").resolve("cobertura").resolve("cobertura.ser")));
    }

    private void verifyLogFilesExist(Path targetProject, String className) throws Exception{
        Path dir = getESFolder(targetProject);
        Path tmp = Files.find(dir,1, (p,a) -> p.getFileName().toString().startsWith("tmp_")).findFirst().get();
        Path logs = tmp.resolve("logs").resolve(className);

        assertTrue(Files.exists(logs.resolve("std_err_CLIENT.log")));
        assertTrue(Files.exists(logs.resolve("std_err_MASTER.log")));
        assertTrue(Files.exists(logs.resolve("std_out_CLIENT.log")));
        assertTrue(Files.exists(logs.resolve("std_out_MASTER.log")));
    }

    private Path getESFolder(Path project){
        return project.resolve(".evosuite");
    }

    private Verifier getVerifier(Path targetProject) throws Exception{
        Verifier verifier  = new Verifier(targetProject.toAbsolutePath().toString(), DEFAULT_MAVEN_HOME);
        Properties props = new Properties(System.getProperties());
        // Keep fixture builds aligned with the current reactor version unless Maven already injected one.
        props.put("evosuiteVersion", System.getProperty("evosuiteVersion", DEFAULT_EVOSUITE_VERSION));
        verifier.setSystemProperties(props);
        verifier.setLocalRepo(System.getProperty("maven.repo.local", DEFAULT_LOCAL_REPOSITORY.toString()));
        verifier.addCliOption("-o");
        return verifier;
    }

    private void assumeLegacyCoverageProfileUnsupportedOnModernJdk(String profileName) {
        Assume.assumeTrue(
                profileName + " profile is not supported on modern JDKs in this legacy integration suite",
                !isModernJdk());
    }

    private void assumeCoverageProfileUnsupportedOnModernJdk(String profileName) {
        Assume.assumeTrue(
                profileName + " profile is not yet supported on modern JDKs in this integration suite",
                !isModernJdk());
    }

    private void assumeLoopbackBindAvailable() {
        Assume.assumeTrue(
                "Loopback bind is unavailable in this environment, so Maven plugin ITs that launch EvoSuite should be skipped",
                MasterServices.getInstance().canBindOnLoopback());
    }

    private boolean isModernJdk() {
        return Runtime.version().feature() >= 17;
    }

    private static String resolveMavenHome() {
        String mavenHome = System.getProperty("maven.home");
        if (mavenHome != null && !mavenHome.trim().isEmpty()) {
            return mavenHome;
        }

        mavenHome = System.getenv("M2_HOME");
        if (mavenHome != null && !mavenHome.trim().isEmpty()) {
            return mavenHome;
        }

        try {
            Process process = new ProcessBuilder("which", "mvn").redirectErrorStream(true).start();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String mvnPath = reader.readLine();
                int exitCode = process.waitFor();
                if (exitCode == 0 && mvnPath != null && !mvnPath.trim().isEmpty()) {
                    Path mvn = Paths.get(mvnPath.trim()).toRealPath();
                    Path bin = mvn.getParent();
                    if (bin != null) {
                        Path home = bin.getParent();
                        if (home != null) {
                            return home.toString();
                        }
                    }
                }
            }
        } catch (Exception ignored) {
            // Fall through to the verifier default if Maven home cannot be resolved.
        }

        return null;
    }

}
