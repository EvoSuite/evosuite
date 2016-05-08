package org.evosuite.maventest;

import org.apache.commons.io.FileUtils;
import org.apache.maven.it.VerificationException;
import org.apache.maven.it.Verifier;
import org.evosuite.runtime.InitializingListener;
import org.junit.After;
import org.junit.Before;
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
        }
    }


    @Test
    public void testCompile() throws Exception{
        Verifier verifier  = getVerifier(projects);
        verifier.executeGoal("compile");
        verifier.verifyTextInLog("SimpleModule");
        verifier.verifyTextInLog("ModuleWithOneDependency");
    }


    @Test
    public void testESClean() throws Exception{
        Verifier verifier  = getVerifier(simple);
        verifier.addCliOption("evosuite:clean");
        verifier.executeGoal("clean");

        Path es = getESFolder(simple);
        assertFalse(Files.exists(es));
    }


    @Test
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

    @Test
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


    @Test
    public void testModuleWithDependency() throws Exception{

        String cut = "org.maven_test_project.mwod.OneDependencyClass";

        Verifier verifier  = getVerifier(dependency);
        verifier.addCliOption("evosuite:generate");
        verifier.executeGoal("compile");

        verifyLogFilesExist(dependency, cut);
    }

    @Test
    public void testExportWithTests() throws Exception {

        /*
            FIXME this fails because we do not use Agent.
            Likely Agent has to be on by default
         */

        String target = "src/evo";

        Verifier verifier  = getVerifier(dependency);
        verifier.addCliOption("evosuite:generate");
        verifier.addCliOption("evosuite:export");
        verifier.addCliOption("-DtargetFolder="+target);

        verifier.executeGoal("test");

        Files.exists(dependency.resolve(target));
        verifyLogFilesExist(dependency,"org.maven_test_project.mwod.OneDependencyClass");
    }

    @Test
    public void testExportWithTestsWithAgent() throws Exception {

        Verifier verifier  = getVerifier(dependency);
        addGenerateAndExportOption(verifier);

        verifier.executeGoal("test");

        Files.exists(dependency.resolve(srcEvo));
        String cut = "org.maven_test_project.mwod.OneDependencyClass";
        verifyLogFilesExist(dependency,cut);
        verifyESTestsRunFor(verifier,cut);
    }


    @Test
    public void testEnv() throws Exception{
        Verifier verifier  = getVerifier(env);
        addGenerateAndExportOption(verifier);

        verifier.executeGoal("test");

        Files.exists(env.resolve(srcEvo));
        String cut = "org.maven_test_project.em.FileCheck";
        verifyLogFilesExist(env,cut);
        verifyESTestsRunFor(verifier,cut);
    }

    @Test
    public void testJaCoCoNoEnv() throws Exception{

        Verifier verifier  = getVerifier(dependency);
        addGenerateAndExportOption(verifier);
        verifier.addCliOption("-Pjacoco");

        verifier.executeGoal("verify");

        Files.exists(dependency.resolve(srcEvo));
        String cut = "org.maven_test_project.mwod.OneDependencyClass";
        verifyLogFilesExist(dependency,cut);
        verifyESTestsRunFor(verifier,cut);
        verifyJaCoCoFileExists(dependency);
    }

    @Test
    public void testJaCoCoWithEnv() throws Exception{

        Verifier verifier  = getVerifier(env);
        addGenerateAndExportOption(verifier);
        verifier.addCliOption("-Pjacoco");

        verifier.executeGoal("verify");

        Files.exists(env.resolve(srcEvo));
        String cut = "org.maven_test_project.em.FileCheck";
        verifyLogFilesExist(env,cut);
        verifyESTestsRunFor(verifier,cut);
        verifyJaCoCoFileExists(env);
    }

    @Test
    public void testJaCoCoPass() throws Exception{
        Verifier verifier = getVerifier(coverage);
        verifier.addCliOption("-Pjacoco");

        verifier.executeGoal("verify");
        verifyJaCoCoFileExists(coverage);
    }

    @Test
    public void testJaCoCoFail() throws Exception{
        Verifier verifier = getVerifier(coverage);
        verifier.addCliOption("-Dtest=SimpleClassPartialTest");

        verifier.executeGoal("verify");

        verifier.executeGoal("clean");
        verifier.addCliOption("-Pjacoco");
        try{
            verifier.executeGoal("verify");
            fail();
        } catch (Exception e){
            //expected, as JaCoCo coverage check should had failed
        }
    }

    //------------------------------------------------------------------------------------------------------------------

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
