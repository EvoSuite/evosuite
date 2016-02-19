/**
 * Copyright (C) 2010-2016 Gordon Fraser, Andrea Arcuri and EvoSuite
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
package org.evosuite.testcase;

import org.evosuite.Properties;
import org.evosuite.runtime.annotation.BoundInputVariable;
import org.evosuite.runtime.annotation.Constraints;
import org.evosuite.runtime.javaee.injection.Injector;
import org.evosuite.runtime.javaee.javax.servlet.EvoServletConfig;
import org.evosuite.runtime.javaee.javax.servlet.EvoServletState;
import org.evosuite.runtime.javaee.javax.servlet.http.EvoHttpServletRequest;
import org.evosuite.testcase.statements.MethodStatement;
import org.evosuite.testcase.statements.StringPrimitiveStatement;
import org.evosuite.testcase.variable.VariableReference;
import org.evosuite.utils.generic.GenericConstructor;
import org.evosuite.utils.generic.GenericMethod;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.servlet.Servlet;
import javax.servlet.http.HttpServlet;

/**
 * Created by Andrea Arcuri on 06/06/15.
 */
public class ConstraintVerifierTest {

    private static final double defaultPRP = Properties.PRIMITIVE_REUSE_PROBABILITY;
    private static final double defaultORP = Properties.OBJECT_REUSE_PROBABILITY;
    private static final boolean defaultJEE = Properties.JEE;

    @Before
    public void init(){
        Properties.PRIMITIVE_REUSE_PROBABILITY = 1; //be sure now new statements are automatically generated
        Properties.OBJECT_REUSE_PROBABILITY = 1;
        Properties.JEE = false; // be sure it is false, as to avoid possible side-effects (eg automated insertion with Injector)
    }

    @After
    public void tearDown(){
        Properties.PRIMITIVE_REUSE_PROBABILITY = defaultPRP;
        Properties.OBJECT_REUSE_PROBABILITY = defaultORP;
        Properties.JEE = defaultJEE;
    }

    private static class FakeServlet extends HttpServlet{
        public FakeServlet(){}

        public void foo(){}
    }

    public static HttpServlet getAFakeServletInstance(){
        return new FakeServlet();
    }

    public static void takeServletAsInput(FakeServlet servlet){
    }

    @Constraints(noNullInputs = true, notMutable = true, noDirectInsertion = true)
    public static  void fakeInjection(@BoundInputVariable(initializer = true) Servlet servlet){
    }

    public static EvoHttpServletRequest getNullEvoHttpServletRequest(){
        return null;
    }

    @Constraints(noDirectInsertion = true)
    public static void noDirect(){

    }

    @Test
    public void testNoDirectInsertion() throws Exception{
        TestChromosome tc = new TestChromosome();
        TestFactory factory = TestFactory.getInstance();

        GenericMethod gm = new GenericMethod(ConstraintVerifierTest.class.getDeclaredMethod("noDirect", null),
                ConstraintVerifierTest.class);

        //should not be possible to insert it
        int pos = ConstraintVerifier.getAValidPositionForInsertion(gm, tc.getTestCase(), 0);
        Assert.assertTrue(pos < 0);
    }

    @Test
    public void testPostConstructIssue() throws Exception{
        TestChromosome tc = new TestChromosome();
        TestFactory factory = TestFactory.getInstance();


        VariableReference servlet = factory.addConstructor(tc.getTestCase(),
                new GenericConstructor(FakeServlet.class.getDeclaredConstructor(), FakeServlet.class), 0, 0);


        //this will also add a statement for the Class
        factory.addMethod(tc.getTestCase(),
                new GenericMethod(Injector.class.getDeclaredMethod("executePostConstruct", Object.class, Class.class),
                        Injector.class), 1, 0);

        Assert.assertEquals(3, tc.size());

        Assert.assertTrue(ConstraintVerifier.verifyTest(tc));

        boolean mutated = tc.deleteStatement(factory, 2);
        Assert.assertFalse(mutated); //should fail to delete the post construct
        Assert.assertEquals(3, tc.size());

        Assert.assertFalse(ConstraintVerifier.canDelete(tc.getTestCase(), 1));

        mutated = tc.deleteStatement(factory, 1);
        Assert.assertFalse(mutated); //eliminating the Class.class var should fail as well, as post construct accepts no null
        Assert.assertEquals(3, tc.size());

        mutated = tc.deleteStatement(factory, 0);
        Assert.assertTrue(mutated); //eliminating the servlet is fine
        Assert.assertEquals(1, tc.size()); // tricky, as Class.class is not bounded to the servlet

        Assert.assertTrue(ConstraintVerifier.verifyTest(tc));
    }


    @Test
    public void testUniqueConstructors() throws Exception{

        TestChromosome tc = new TestChromosome();
        TestFactory factory = TestFactory.getInstance();

        Properties.JEE = true;
        factory.addConstructor(tc.getTestCase(),
                new GenericConstructor(FakeServlet.class.getDeclaredConstructor(), FakeServlet.class), 0, 0);

        //doing it a second time should fail
        try {
            factory.addConstructor(tc.getTestCase(),
                    new GenericConstructor(FakeServlet.class.getDeclaredConstructor(), FakeServlet.class), 0, 0);
            Assert.fail();
        } catch (Exception e){
            //expected
        }

    }

    @Test
    public void testDeleteAfter()  throws Exception{

        TestChromosome tc = new TestChromosome();
        TestFactory factory = TestFactory.getInstance();

        //get a var for EvoHttpServletRequest
        VariableReference req = factory.addMethod(tc.getTestCase(),
                new GenericMethod(ConstraintVerifierTest.class.getDeclaredMethod("getNullEvoHttpServletRequest"),
                        ConstraintVerifierTest.class), 0, 0);

        //make it a POST
        factory.addMethod(tc.getTestCase(),
                new GenericMethod(EvoHttpServletRequest.class.getDeclaredMethod("asPOST"),EvoHttpServletRequest.class),
                1, 0);

        //now add a call to 'asMultipartFormData' which does depend on POST
        factory.addMethod(tc.getTestCase(),
                new GenericMethod(EvoHttpServletRequest.class.getDeclaredMethod("asMultipartFormData"),
                        EvoHttpServletRequest.class), 2, 0);

        Assert.assertEquals(3, tc.size());
        Assert.assertTrue(ConstraintVerifier.verifyTest(tc));

        //deleting the last one should be fine, but not the POST, as asMultipartFormData has an 'after' dependency on it
        Assert.assertTrue(ConstraintVerifier.canDelete(tc.getTestCase(), 2));
        Assert.assertFalse(ConstraintVerifier.canDelete(tc.getTestCase(), 1)); // should not be able to delete POST directly

        //what about the first statement where the var is obtained? that should be valid to delete
        Assert.assertTrue(ConstraintVerifier.canDelete(tc.getTestCase(), 0));

        boolean mutated = tc.deleteStatement(factory, 1);
        Assert.assertFalse(mutated); //should fail to delete POST
        Assert.assertEquals(3, tc.size());

        mutated = tc.deleteStatement(factory, 0);
        Assert.assertTrue(mutated);
        Assert.assertEquals(0, tc.size()); //should end up deleting everything
    }


    @Test
    public void testCanDelete() throws Exception {

        TestChromosome tc = new TestChromosome();
        TestFactory factory = TestFactory.getInstance();

        VariableReference servlet = factory.addConstructor(tc.getTestCase(),
                new GenericConstructor(FakeServlet.class.getDeclaredConstructor(), FakeServlet.class), 0, 0);

        //initializing bounding variable method called directly after the new
        factory.addMethod(tc.getTestCase(),
                new GenericMethod(Injector.class.getDeclaredMethod("executePostConstruct", Object.class), Injector.class), 1, 0);


        Assert.assertEquals(2, tc.size());
        Assert.assertTrue(ConstraintVerifier.verifyTest(tc));

        Assert.assertTrue(ConstraintVerifier.canDelete(tc.getTestCase(), 0)); //bounded variable can be deleted
        Assert.assertFalse(ConstraintVerifier.canDelete(tc.getTestCase(), 1)); // method using bounded variable should not be deleted

        boolean mutated = tc.deleteStatement(factory, 1);
        Assert.assertFalse(mutated); //should fail
        Assert.assertEquals(2, tc.size());

        mutated = tc.deleteStatement(factory, 0);
        Assert.assertTrue(mutated);
        Assert.assertEquals(0, tc.size());// deleting first statement should have had effect of removing the second as well
    }

    @Test
    public void testDelete_multipleVarsThatCouldBeReused() throws Exception {

        TestChromosome tc = new TestChromosome();
        TestFactory factory = TestFactory.getInstance();

        VariableReference servlet = factory.addConstructor(tc.getTestCase(),
                new GenericConstructor(FakeServlet.class.getDeclaredConstructor(), FakeServlet.class), 0, 0);

        //initializing bounding variable method called directly after the new
        factory.addMethod(tc.getTestCase(),
                new GenericMethod(Injector.class.getDeclaredMethod("executePostConstruct", Object.class), Injector.class), 1, 0);

        //now do it again
        VariableReference secondServlet = factory.addConstructor(tc.getTestCase(),
                new GenericConstructor(FakeServlet.class.getDeclaredConstructor(), FakeServlet.class), 2, 0);
        factory.addMethod(tc.getTestCase(),
                new GenericMethod(Injector.class.getDeclaredMethod("executePostConstruct", Object.class), Injector.class), 3, 0);
        MethodStatement mt = (MethodStatement) tc.getTestCase().getStatement(3);
        //be sure it is using the second servlet
        mt.replace(servlet,secondServlet);


        Assert.assertEquals(4, tc.size());
        Assert.assertTrue(ConstraintVerifier.verifyTest(tc));

        Assert.assertTrue(ConstraintVerifier.canDelete(tc.getTestCase(), 0)); //bounded variable can be deleted
        Assert.assertFalse(ConstraintVerifier.canDelete(tc.getTestCase(), 1)); // method using bounded variable should not be deleted
        Assert.assertTrue(ConstraintVerifier.canDelete(tc.getTestCase(), 2)); //bounded variable can be deleted
        Assert.assertFalse(ConstraintVerifier.canDelete(tc.getTestCase(), 3)); // method using bounded variable should not be deleted


        boolean mutated = tc.deleteStatement(factory, 2);
        Assert.assertTrue(mutated);
        /*
            deleting the bounded variable in position 2 should lead to also delete the initializing variable in 3,
            and not end up with 3 re-using 0
         */
        Assert.assertTrue(ConstraintVerifier.verifyTest(tc));
        Assert.assertEquals(2, tc.size());
    }



    @Test
    public void testInitializingBoundedVariable_wrong_callingMethodsBeforeInit() throws Exception {
        TestChromosome tc = new TestChromosome();
        TestFactory factory = TestFactory.getInstance();

        VariableReference servlet = factory.addConstructor(tc.getTestCase(),
                new GenericConstructor(FakeServlet.class.getDeclaredConstructor(), FakeServlet.class), 0, 0);
        factory.addMethodFor(tc.getTestCase(), servlet,
                new GenericMethod(FakeServlet.class.getDeclaredMethod("foo"), FakeServlet.class), 1);

        //initializing bounding variable method cannot be called here after "foo" is called on the bounded variable
        factory.addMethod(tc.getTestCase(),
                new GenericMethod(Injector.class.getDeclaredMethod("executePostConstruct",Object.class), Injector.class), 2, 0);

        Assert.assertEquals(3, tc.size());
        Assert.assertFalse(ConstraintVerifier.verifyTest(tc));
    }

    @Test
    public void testInitializingBoundedVariable_wrong_inputInOtherMethodBeforeInit() throws Exception {
        TestChromosome tc = new TestChromosome();
        TestFactory factory = TestFactory.getInstance();

        VariableReference servlet = factory.addConstructor(tc.getTestCase(),
                new GenericConstructor(FakeServlet.class.getDeclaredConstructor(), FakeServlet.class), 0, 0);
        factory.addMethod(tc.getTestCase(),
                new GenericMethod(ConstraintVerifierTest.class.getDeclaredMethod("takeServletAsInput", FakeServlet.class),
                        ConstraintVerifierTest.class), 1, 0);

        //initializing bounding variable method cannot be called here after the bounded variable has been used as input in some other method
        factory.addMethod(tc.getTestCase(),
                new GenericMethod(Injector.class.getDeclaredMethod("executePostConstruct",Object.class), Injector.class), 2, 0);

        Assert.assertEquals(3, tc.size());
        Assert.assertFalse(ConstraintVerifier.verifyTest(tc));
    }

    @Test
    public void testInitializingBoundedVariable_correct() throws Exception {
        TestChromosome tc = new TestChromosome();
        TestFactory factory = TestFactory.getInstance();

        VariableReference servlet = factory.addConstructor(tc.getTestCase(),
                new GenericConstructor(FakeServlet.class.getDeclaredConstructor(), FakeServlet.class), 0, 0);
        //initializing bounding variable method called directly after the new
        factory.addMethod(tc.getTestCase(),
                new GenericMethod(Injector.class.getDeclaredMethod("executePostConstruct",Object.class), Injector.class), 1, 0);

        //method on servlet after the bounding variable initialization: it is ok
        factory.addMethodFor(tc.getTestCase(), servlet,
                new GenericMethod(FakeServlet.class.getDeclaredMethod("foo"), FakeServlet.class), 2);

        Assert.assertEquals(3, tc.size());
        Assert.assertTrue(ConstraintVerifier.verifyTest(tc));
    }


    @Test
    public void testInitializingBoundedVariable_correct_severalCalls() throws Exception {
        TestChromosome tc = new TestChromosome();
        TestFactory factory = TestFactory.getInstance();

        VariableReference servlet = factory.addConstructor(tc.getTestCase(),
                new GenericConstructor(FakeServlet.class.getDeclaredConstructor(), FakeServlet.class), 0, 0);

        //both calls on same bounding variable

        factory.addMethod(tc.getTestCase(),
                new GenericMethod(
                        ConstraintVerifierTest.class.getDeclaredMethod("fakeInjection",Servlet.class),
                        ConstraintVerifierTest.class), 1, 0);

        //this is an atMostOnce type
        factory.addMethod(tc.getTestCase(),
                new GenericMethod(Injector.class.getDeclaredMethod("executePostConstruct",Object.class), Injector.class), 2, 0);

        Assert.assertEquals(3, tc.size());
        Assert.assertTrue(ConstraintVerifier.verifyTest(tc));
    }

    @Test
    public void testInitializingBoundedVariable_wrong_atMostOnce() throws Exception {
        TestChromosome tc = new TestChromosome();
        TestFactory factory = TestFactory.getInstance();

        VariableReference servlet = factory.addConstructor(tc.getTestCase(),
                new GenericConstructor(FakeServlet.class.getDeclaredConstructor(), FakeServlet.class), 0, 0);

        //initializing bounding variable method called directly after the new
        factory.addMethod(tc.getTestCase(),
                new GenericMethod(Injector.class.getDeclaredMethod("executePostConstruct", Object.class), Injector.class), 1, 0);


        Assert.assertEquals(2, tc.size());
        Assert.assertTrue(ConstraintVerifier.verifyTest(tc));


        // this should be invalid, as executePostConstruct can be used only once on same bounded variable
        factory.addMethod(tc.getTestCase(),
                new GenericMethod(Injector.class.getDeclaredMethod("executePostConstruct",Object.class), Injector.class), 2, 0);

        Assert.assertEquals(3, tc.size());
        Assert.assertFalse(ConstraintVerifier.verifyTest(tc));
    }


    @Test
    public void testInitializingBoundedVariable_wrong_noConstructor() throws Exception {
        TestChromosome tc = new TestChromosome();
        TestFactory factory = TestFactory.getInstance();

        factory.addMethod(tc.getTestCase(),
                new GenericMethod(ConstraintVerifierTest.class.getDeclaredMethod("getAFakeServletInstance"),
                        ConstraintVerifierTest.class), 0, 0);


        //initializing bounding variable method called on instance not generated with new
        factory.addMethod(tc.getTestCase(),
                new GenericMethod(Injector.class.getDeclaredMethod("executePostConstruct", Object.class), Injector.class), 1, 0);

        Assert.assertEquals(2, tc.size());
        Assert.assertFalse(ConstraintVerifier.verifyTest(tc));
    }


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
    public void testAfter() throws Exception{
        TestChromosome tc = new TestChromosome();
        TestFactory factory = TestFactory.getInstance();

        //this method has an "after" constraint on initServlet
        factory.addMethod(tc.getTestCase(),
                new GenericMethod(EvoServletState.class.getDeclaredMethod("getRequest"), EvoServletState.class), 0, 0);

        Assert.assertEquals(1, tc.size());
        Assert.assertFalse(ConstraintVerifier.verifyTest(tc));

        VariableReference con = factory.addConstructor(tc.getTestCase(),
                new GenericConstructor(FakeServlet.class.getDeclaredConstructor(), FakeServlet.class), 0, 0);
        factory.addMethod(tc.getTestCase(),
                new GenericMethod(EvoServletState.class.getDeclaredMethod("initServlet",Servlet.class), EvoServletState.class), 1, 0);

        Assert.assertEquals(3, tc.size());
        Assert.assertTrue(ConstraintVerifier.verifyTest(tc));
    }

    @Test
    public void testAtMostOnce() throws Exception{
        TestChromosome tc = new TestChromosome();
        TestFactory factory = TestFactory.getInstance();


        VariableReference servlet = factory.addConstructor(tc.getTestCase(),
                new GenericConstructor(FakeServlet.class.getDeclaredConstructor(), FakeServlet.class), 0, 0);
        factory.addMethod(tc.getTestCase(),
                new GenericMethod(EvoServletState.class.getDeclaredMethod("initServlet", Servlet.class), EvoServletState.class), 1, 0);

        //2 different methods that can be used at most once
        factory.addMethod(tc.getTestCase(),
                new GenericMethod(EvoServletState.class.getDeclaredMethod("getRequest"), EvoServletState.class), 2, 0);
        factory.addMethod(tc.getTestCase(),
                new GenericMethod(EvoServletState.class.getDeclaredMethod("getResponse"),EvoServletState.class),3,0);

        Assert.assertEquals(4, tc.size());
        Assert.assertTrue(ConstraintVerifier.verifyTest(tc));

        //add an invalid new call
        factory.addMethod(tc.getTestCase(),
                new GenericMethod(EvoServletState.class.getDeclaredMethod("getResponse"), EvoServletState.class), 4, 0);

        Assert.assertEquals(5, tc.size());
        Assert.assertFalse(ConstraintVerifier.verifyTest(tc)); //check should fail
    }

    @Test
    public void testNoNullInputs_notNull() throws Exception{

        TestChromosome tc = new TestChromosome();
        TestFactory factory = TestFactory.getInstance();


        VariableReference servlet = factory.addConstructor(tc.getTestCase(),
                new GenericConstructor(FakeServlet.class.getDeclaredConstructor(), FakeServlet.class), 0, 0);
        factory.addMethod(tc.getTestCase(),
                new GenericMethod(EvoServletState.class.getDeclaredMethod("initServlet",Servlet.class), EvoServletState.class), 1, 0);

        StringPrimitiveStatement foo = new StringPrimitiveStatement(tc.getTestCase(), "foo");
        tc.getTestCase().addStatement(foo);
        VariableReference con = factory.addMethod(tc.getTestCase(),
                new GenericMethod(EvoServletState.class.getDeclaredMethod("getConfiguration"), EvoServletState.class), 3, 0);
        factory.addMethodFor(tc.getTestCase(), con,
                new GenericMethod(EvoServletConfig.class.getDeclaredMethod("createDispatcher", String.class),
                        EvoServletConfig.class), 4);

        Assert.assertEquals(5, tc.size());
        Assert.assertTrue(ConstraintVerifier.verifyTest(tc));
    }

    @Test
    public void testNoNullInputs_nullString() throws Exception{

        TestChromosome tc = new TestChromosome();
        TestFactory factory = TestFactory.getInstance();

        VariableReference servlet = factory.addConstructor(tc.getTestCase(),
                new GenericConstructor(FakeServlet.class.getDeclaredConstructor(), FakeServlet.class), 0, 0);
        factory.addMethod(tc.getTestCase(),
                new GenericMethod(EvoServletState.class.getDeclaredMethod("initServlet",Servlet.class), EvoServletState.class), 1, 0);

        //shouldn't be able to pass it to createDispatcher
        StringPrimitiveStatement foo = new StringPrimitiveStatement(tc.getTestCase(), null);
        tc.getTestCase().addStatement(foo);

        VariableReference con = factory.addMethod(tc.getTestCase(),
                new GenericMethod(EvoServletState.class.getDeclaredMethod("getConfiguration"), EvoServletState.class), 3, 0);
        factory.addMethodFor(tc.getTestCase(), con,
                new GenericMethod(EvoServletConfig.class.getDeclaredMethod("createDispatcher", String.class),
                        EvoServletConfig.class), 4);

        /*
            even if "foo" is null and we have the P of object reuse to 1, still "foo"
            will not be used, as "null" constraint is always enforced
         */

        Assert.assertEquals(tc.getTestCase().toCode(), 6, tc.size());
        Assert.assertTrue(ConstraintVerifier.verifyTest(tc));
    }

    @Test
    public void testExcludeOthers() throws Exception{

        TestChromosome tc = new TestChromosome();
        TestFactory factory = TestFactory.getInstance();

        VariableReference servlet = factory.addConstructor(tc.getTestCase(),
                new GenericConstructor(FakeServlet.class.getDeclaredConstructor(), FakeServlet.class), 0, 0);
        factory.addMethod(tc.getTestCase(),
                new GenericMethod(EvoServletState.class.getDeclaredMethod("initServlet",Servlet.class), EvoServletState.class), 1, 0);

        VariableReference req = factory.addMethod(tc.getTestCase(),
                new GenericMethod(EvoServletState.class.getDeclaredMethod("getRequest"), EvoServletState.class), 2, 0);
        factory.addMethodFor(tc.getTestCase(), req,
                new GenericMethod(EvoHttpServletRequest.class.getDeclaredMethod("asGET"), EvoHttpServletRequest.class), 3);

        Assert.assertEquals(tc.getTestCase().toCode(), 4, tc.size());
        Assert.assertTrue(ConstraintVerifier.verifyTest(tc));


        //once it is set as GET, we should not be able to change it to POST
        factory.addMethodFor(tc.getTestCase(), req,
                new GenericMethod(EvoHttpServletRequest.class.getDeclaredMethod("asPOST"), EvoHttpServletRequest.class), 4);

        Assert.assertEquals(tc.getTestCase().toCode(), 5, tc.size());
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
