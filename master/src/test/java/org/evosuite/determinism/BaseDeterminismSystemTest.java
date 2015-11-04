package org.evosuite.determinism;

import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.SystemTest;
import org.evosuite.utils.LoggingUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.Assert.assertEquals;

/**
 * Created by Andrea Arcuri on 19/10/15.
 */
public class BaseDeterminismSystemTest{

    @BeforeClass
    public static void initClass(){
        LoggingUtils.changeLogbackFile("logback_for_determinism_check.xml");
        Properties.IS_RUNNING_A_SYTEM_TEST = true;
    }

    @AfterClass
    public static void tearDownClass(){
        LoggingUtils.changeLogbackFile("logback.xml");
        Properties.IS_RUNNING_A_SYTEM_TEST = false;
    }


    @Test
    public void testBase(){
        checkDeterminism(com.examples.with.different.packagename.TrivialInt.class);
    }

    public static void checkDeterminism(Class<?> target){

        //dry run, needed to avoid logs of static initializers that are called only once
        run(target);

        String first = run(target);
        String second = run(target);

        assertEquals(first,second);
    }

    private static String run(Class<?> target){

        SystemTest scaffolding = new SystemTest();

        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        PrintStream outStream = new PrintStream(byteStream);
        PrintStream latestOut = System.out;
        System.setOut(outStream);


        scaffolding.setDefaultPropertiesForTestCases(); //@Before
        Properties.CRITERION = new Properties.Criterion[] {
                Properties.Criterion.LINE, Properties.Criterion.BRANCH,
                Properties.Criterion.EXCEPTION, Properties.Criterion.WEAKMUTATION,
                Properties.Criterion.OUTPUT, Properties.Criterion.METHOD,
                Properties.Criterion.METHODNOEXCEPTION, Properties.Criterion.CBRANCH  };


        boolean defaultPrint = Properties.PRINT_TO_SYSTEM;
        Properties.PRINT_TO_SYSTEM = true;

        try {
            EvoSuite evosuite = new EvoSuite();
            String targetClass = target.getCanonicalName();

            Properties.TARGET_CLASS = targetClass;
            String[] command = new String[]{"-generateSuite", "-class", targetClass};

            evosuite.parseCommandLine(command);
        } finally {
            scaffolding.resetStaticVariables();//@After
            System.setOut(latestOut);
            Properties.PRINT_TO_SYSTEM = defaultPrint;
        }

        return byteStream.toString();
    }
}
