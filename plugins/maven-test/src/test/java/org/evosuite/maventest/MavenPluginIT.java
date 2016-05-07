package org.evosuite.maventest;

import org.apache.commons.io.FileUtils;
import org.apache.maven.it.VerificationException;
import org.apache.maven.it.Verifier;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class MavenPluginIT {

    private final Path projects = Paths.get("projects");
    private final Path simple = projects.resolve("SimpleModule");
    private final Path dependency = projects.resolve("ModuleWithOneDependency");

    private final String srcEvo = "src/evo";

    @Before
    @After
    public void clean() throws Exception{
        Verifier verifier  = getVerifier(projects);
        verifier.addCliOption("evosuite:clean");
        verifier.executeGoal("clean");

        FileUtils.deleteDirectory(projects.resolve(srcEvo).toFile());
        FileUtils.deleteDirectory(simple.resolve(srcEvo).toFile());
        FileUtils.deleteDirectory(dependency.resolve(srcEvo).toFile());

        FileUtils.deleteQuietly(projects.resolve("log.txt").toFile());
        FileUtils.deleteQuietly(simple.resolve("log.txt").toFile());
        FileUtils.deleteQuietly(dependency.resolve("log.txt").toFile());
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
        verifier.addCliOption("evosuite:generate");
        verifier.addCliOption("evosuite:export");
        verifier.addCliOption("-DtargetFolder="+srcEvo);
        verifier.addCliOption("-DextraArgs=\"-Duse_separate_classloader=false\"");

        verifier.executeGoal("test");

        Files.exists(dependency.resolve(srcEvo));
        verifyLogFilesExist(dependency,"org.maven_test_project.mwod.OneDependencyClass");
    }



    //------------------------------------------------------------------------------------------------------------------


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
