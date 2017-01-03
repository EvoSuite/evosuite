package org.evosuite.maventest;

import org.apache.commons.io.FileUtils;
import org.apache.maven.it.VerificationException;
import org.apache.maven.it.Verifier;
import org.evosuite.runtime.InitializingListener;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Properties;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class MavenPluginIT {

    private static final long timeoutInMs = 3 * 60 * 1_000;

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

        String cut = "org.maven_test_project.sm.SimpleClass";

        Verifier verifier  = getVerifier(simple);
        verifier.addCliOption("evosuite:generate");
        verifier.addCliOption("-DtimeInMinutesPerClass=1");
        verifier.addCliOption("-Dcuts="+cut);
        verifier.executeGoal("compile");

        Path es = getESFolder(simple);
        assertTrue(Files.exists(es));

        verifier.verifyTextInLog("Going to generate tests with EvoSuite");
        verifier.verifyTextInLog("New test suites: 1");

        verifyLogFilesExist(simple,cut);
    }

    @Test(timeout = timeoutInMs)
    public void testSimpleMultiCore() throws Exception {

        String a = "org.maven_test_project.sm.SimpleClass";
        String b = "org.maven_test_project.sm.ThrowException";

        Verifier verifier  = getVerifier(simple);
        verifier.addCliOption("evosuite:generate");
        verifier.addCliOption("-DtimeInMinutesPerClass=1");
        verifier.addCliOption("-Dcores=2");

        try {
            verifier.executeGoal("compile");
            fail();
        } catch (VerificationException e){
            //expected, because not enough memory
        }

        verifier.addCliOption("-DmemoryInMB=1000");
        verifier.executeGoal("compile");

        verifyLogFilesExist(simple, a);
        verifyLogFilesExist(simple, b);
    }


    @Test(timeout = timeoutInMs)
    public void testModuleWithDependency() throws Exception{

        String cut = "org.maven_test_project.mwod.OneDependencyClass";

        Verifier verifier  = getVerifier(dependency);
        verifier.addCliOption("evosuite:generate");
        verifier.executeGoal("compile");

        verifyLogFilesExist(dependency, cut);
    }

    @Test(timeout = timeoutInMs)
    public void testExportWithTests() throws Exception {

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
        testVerifyNoEnv("jacoco");
        verifyJaCoCoFileExists(dependency);
    }

    @Test(timeout = timeoutInMs)
    public void testJaCoCoWithEnv() throws Exception{
        testVerfiyWithEnv("jacoco");
        verifyJaCoCoFileExists(env);
    }

    @Test(timeout = timeoutInMs)
    public void testJaCoCoPass() throws Exception{
        testCoveragePass("jacoco");
        verifyJaCoCoFileExists(coverage);
    }

    @Test(timeout = timeoutInMs)
    public void testJaCoCoFail() throws Exception{
        testCoverageFail("jacoco");
        verifyJaCoCoFileExists(coverage);
    }


    //--- JMockit --------------------------------------------------------------


    @Test(timeout = timeoutInMs)
    public void testJMockitNoEnv() throws Exception{
        testVerifyNoEnv("jmockit", 1);
        verifyJMockitFolderExists(dependency);
    }

    @Test(timeout = timeoutInMs)
    public void testJMockitWithEnv() throws Exception{
        testVerfiyWithEnv("jmockit", 1);
        verifyJMockitFolderExists(env);
    }

    @Test(timeout = timeoutInMs)
    public void testJMockitPass() throws Exception{
        testCoveragePass("jmockit");
        verifyJMockitFolderExists(coverage);
    }

    @Test(timeout = timeoutInMs)
    public void testJMockitFail() throws Exception{
        testCoverageFail("jmockit");
        verifyJMockitFolderExists(coverage);
    }


    //--- PowerMock --------------------------------------------------------------

    @Test(timeout = timeoutInMs)
    public void testPowerMockNoEnv() throws Exception{
        testVerifyNoEnv("powermock",1);
    }


    @Test(timeout = timeoutInMs)
    public void testPowerMockWithEnv() throws Exception{
        testVerfiyWithEnv("powermock",1);
    }



    //--- Cobertura --------------------------------------------------------------

    @Test(timeout = timeoutInMs)
    public void testCoberturaNoEnv() throws Exception{
        testVerifyNoEnv("cobertura");
        verifyCoberturaFileExists(dependency);
    }

    @Test(timeout = timeoutInMs)
    public void testCoberturaWithEnv() throws Exception{
        testVerfiyWithEnv("cobertura");
        verifyCoberturaFileExists(env);
    }

    @Test(timeout = timeoutInMs)
    public void testCoberturaPass() throws Exception{
        testCoveragePass("cobertura");
        verifyCoberturaFileExists(coverage);
    }

    @Test(timeout = timeoutInMs)
    public void testCoberturaFail() throws Exception{
        testCoverageFail("cobertura");
        verifyCoberturaFileExists(coverage);
    }

    //--- PIT --------------------------------------------------------------

    @Test(timeout = timeoutInMs)
    public void testPitNoEnv() throws Exception{
        testVerifyNoEnv("pit");
        verifyPitFolderExists(dependency);
    }

    @Test(timeout = timeoutInMs)
    public void testPitWithEnv() throws Exception{
        testVerfiyWithEnv("pit");
        verifyPitFolderExists(env);
    }


    @Test(timeout = timeoutInMs)
    public void testPitPass() throws Exception{
        testCoveragePass("pit");
        verifyPitFolderExists(coverage);
    }

    @Test(timeout = timeoutInMs)
    public void testPitFail() throws Exception{
        testCoverageFail("pit,pitOneTest"); //PIT has its filters for test execution
        verifyPitFolderExists(coverage);
    }


    //------------------------------------------------------------------------------------------------------------------

    private void testVerfiyWithEnv(String profile) throws Exception{
        testVerfiyWithEnv(profile, 1);
    }

    private void testVerfiyWithEnv(String profile, int forkCount) throws Exception{

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
        Verifier verifier = getVerifier(coverage);
        verifier.addCliOption("-P"+profile);
        verifier.executeGoal("verify");
    }

    private void testCoverageFail(String profile) throws Exception{
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
        //TODO remove once off by default
        verifier.addCliOption("-DextraArgs=\"-Duse_separate_classloader=false\"");
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
        Verifier verifier  = new Verifier(targetProject.toAbsolutePath().toString());
        Properties props = new Properties(System.getProperties());
        //update version if run from IDE instead of Maven
        props.put("evosuiteVersion", System.getProperty("evosuiteVersion","1.0.4-SNAPSHOT"));
        verifier.setSystemProperties(props);
        return verifier;
    }

}
