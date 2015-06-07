package org.evosuite.testcase;

import org.evosuite.Properties;
import org.evosuite.runtime.javaee.javax.servlet.EvoServletConfig;
import org.evosuite.runtime.javaee.javax.servlet.EvoServletState;
import org.evosuite.runtime.javaee.javax.servlet.http.EvoHttpServletRequest;
import org.evosuite.testcase.statements.StringPrimitiveStatement;
import org.evosuite.testcase.variable.VariableReference;
import org.evosuite.utils.GenericConstructor;
import org.evosuite.utils.GenericMethod;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by Andrea Arcuri on 06/06/15.
 */
public class ConstraintVerifierTest {

    private static final double defaultPRP = Properties.PRIMITIVE_REUSE_PROBABILITY;
    private static final double defaultORP = Properties.OBJECT_REUSE_PROBABILITY;

    @Before
    public void init(){
        Properties.PRIMITIVE_REUSE_PROBABILITY = 1; //be sure now new statements are automatically generated
        Properties.OBJECT_REUSE_PROBABILITY = 1;
    }

    @After
    public void tearDown(){
        Properties.PRIMITIVE_REUSE_PROBABILITY = defaultPRP;
        Properties.OBJECT_REUSE_PROBABILITY = defaultORP;
    }

    //TODO test after
    //TODO test bounded variable init




    @Test
    public void testHasAnyOnlyForAssertionMethod() throws Exception{
        TestChromosome tc = new TestChromosome();
        TestFactory factory = TestFactory.getInstance();

        VariableReference req = factory.addMethod(tc.getTestCase(),
                new GenericMethod(EvoServletState.class.getDeclaredMethod("getRequest"), EvoServletState.class), 0, 0);

        Assert.assertEquals(tc.getTestCase().toCode(), 1, tc.size());
        Assert.assertFalse(ConstraintVerifier.hasAnyOnlyForAssertionMethod(tc.getTestCase()));

        //this method should be only for assertions
        factory.addMethodFor(tc.getTestCase(), req,
                new GenericMethod(EvoHttpServletRequest.class.getDeclaredMethod("isAsyncStarted"), EvoHttpServletRequest.class), 1);

        Assert.assertEquals(tc.getTestCase().toCode(), 2, tc.size());
        Assert.assertTrue(ConstraintVerifier.hasAnyOnlyForAssertionMethod(tc.getTestCase()));
    }

    @Test
    public void testBaseTest() throws Exception{
        TestChromosome tc = new TestChromosome();
        TestFactory factory = TestFactory.getInstance();

        factory.addConstructor(tc.getTestCase(), new GenericConstructor(Object.class.getConstructor(), Object.class), 0, 0);

        Assert.assertEquals(1, tc.size());
        Assert.assertTrue(ConstraintVerifier.verifyTest(tc));
    }


    @Test
    public void testAtMostOnce() throws Exception{
        TestChromosome tc = new TestChromosome();
        TestFactory factory = TestFactory.getInstance();

        factory.addConstructor(tc.getTestCase(),new GenericConstructor(Object.class.getConstructor(),Object.class),0,0);
        //2 different methods that can be used at most once
        factory.addMethod(tc.getTestCase(),
                new GenericMethod(EvoServletState.class.getDeclaredMethod("getRequest"), EvoServletState.class), 1, 0);
        factory.addMethod(tc.getTestCase(),
                new GenericMethod(EvoServletState.class.getDeclaredMethod("getResponse"),EvoServletState.class),2,0);

        Assert.assertEquals(3, tc.size());
        Assert.assertTrue(ConstraintVerifier.verifyTest(tc));

        //add an invalid new call
        factory.addMethod(tc.getTestCase(),
                new GenericMethod(EvoServletState.class.getDeclaredMethod("getResponse"), EvoServletState.class), 3, 0);

        Assert.assertEquals(4, tc.size());
        Assert.assertFalse(ConstraintVerifier.verifyTest(tc)); //check should fail
    }

    @Test
    public void testNoNullInputs_notNull() throws Exception{

        TestChromosome tc = new TestChromosome();
        TestFactory factory = TestFactory.getInstance();


        StringPrimitiveStatement foo = new StringPrimitiveStatement(tc.getTestCase(), "foo");
        tc.getTestCase().addStatement(foo);
        VariableReference con = factory.addMethod(tc.getTestCase(),
                new GenericMethod(EvoServletState.class.getDeclaredMethod("getConfiguration"), EvoServletState.class), 1, 0);
        factory.addMethodFor(tc.getTestCase(), con,
                new GenericMethod(EvoServletConfig.class.getDeclaredMethod("createDispatcher", String.class),
                        EvoServletConfig.class), 2);

        Assert.assertEquals(3, tc.size());
        Assert.assertTrue(ConstraintVerifier.verifyTest(tc));
    }

    @Test
    public void testNoNullInputs_nullString() throws Exception{

        TestChromosome tc = new TestChromosome();
        TestFactory factory = TestFactory.getInstance();

        //shouldn't be able to pass it to createDispatcher
        StringPrimitiveStatement foo = new StringPrimitiveStatement(tc.getTestCase(), null);
        tc.getTestCase().addStatement(foo);
        VariableReference con = factory.addMethod(tc.getTestCase(),
                new GenericMethod(EvoServletState.class.getDeclaredMethod("getConfiguration"), EvoServletState.class), 1, 0);
        factory.addMethodFor(tc.getTestCase(), con,
                new GenericMethod(EvoServletConfig.class.getDeclaredMethod("createDispatcher", String.class),
                        EvoServletConfig.class), 2);

        Assert.assertEquals(tc.getTestCase().toCode(), 3, tc.size());
        Assert.assertFalse(ConstraintVerifier.verifyTest(tc));
    }

    @Test
    public void testExcludeOthers() throws Exception{

        TestChromosome tc = new TestChromosome();
        TestFactory factory = TestFactory.getInstance();

        VariableReference req = factory.addMethod(tc.getTestCase(),
                new GenericMethod(EvoServletState.class.getDeclaredMethod("getRequest"), EvoServletState.class), 0, 0);
        factory.addMethodFor(tc.getTestCase(), req,
                new GenericMethod(EvoHttpServletRequest.class.getDeclaredMethod("asGET"), EvoHttpServletRequest.class), 1);

        Assert.assertEquals(tc.getTestCase().toCode(), 2, tc.size());
        Assert.assertTrue(ConstraintVerifier.verifyTest(tc));


        //once it is set as GET, we should not be able to change it to POST
        factory.addMethodFor(tc.getTestCase(), req,
                new GenericMethod(EvoHttpServletRequest.class.getDeclaredMethod("asPOST"), EvoHttpServletRequest.class), 2);

        Assert.assertEquals(tc.getTestCase().toCode(), 3, tc.size());
        Assert.assertFalse(ConstraintVerifier.verifyTest(tc));
    }

    @Test
    public void testExcludeMethod() throws Exception{
        TestChromosome tc = new TestChromosome();
        TestFactory factory = TestFactory.getInstance();

        //'reset' is a method that shouldn't be used in a test
        factory.addMethod(tc.getTestCase(),
                new GenericMethod(EvoServletState.class.getDeclaredMethod("reset"), EvoServletState.class), 0, 0);

        Assert.assertEquals(tc.getTestCase().toCode(), 1, tc.size());
        Assert.assertFalse(ConstraintVerifier.verifyTest(tc));
    }

    @Test
    public void testEvoSuiteClassExclude() throws Exception{
        TestChromosome tc = new TestChromosome();
        TestFactory factory = TestFactory.getInstance();

        //shouldn't be able to instantiate EvoServletConfig directly
        factory.addConstructor(tc.getTestCase(),
                new GenericConstructor(EvoServletConfig.class.getConstructor(), EvoServletConfig.class), 0, 0);

        Assert.assertEquals(1, tc.size());
        Assert.assertFalse(ConstraintVerifier.verifyTest(tc));
    }
}
