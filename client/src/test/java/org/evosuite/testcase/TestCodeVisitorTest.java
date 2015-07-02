package org.evosuite.testcase;

import org.evosuite.ga.ConstructionFailedException;
import org.evosuite.testcase.statements.Statement;
import org.evosuite.testcase.variable.VariableReference;
import org.evosuite.utils.GenericConstructor;
import org.evosuite.utils.GenericMethod;
import org.evosuite.utils.WildcardTypeImpl;
import org.junit.Assert;
import org.junit.Test;

import javax.servlet.Servlet;
import javax.servlet.http.HttpServlet;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;

/**
 * Created by Andrea Arcuri on 02/07/15.
 */
public class TestCodeVisitorTest {

    public static <T extends Servlet> T foo(T servlet){ return servlet;}

    public static <T> T bar(T obj){return obj;}

    public static class ClassWithGeneric<T extends Servlet>{
        public T hello(T servlet){ return servlet;}
    }

    public static class FakeServlet extends HttpServlet {
        public FakeServlet(){}
    }

    @Test
    public void testGenerics_methodWithExtends() throws NoSuchMethodException, ConstructionFailedException {

        //first construct a test case for the Generic method
        TestCase tc = new DefaultTestCase();
        VariableReference servlet = TestFactory.getInstance().addConstructor(tc,
                new GenericConstructor(FakeServlet.class.getDeclaredConstructor(), FakeServlet.class), 0, 0);
        VariableReference genericClass = TestFactory.getInstance().addConstructor(tc,
                new GenericConstructor(ClassWithGeneric.class.getDeclaredConstructor(), ClassWithGeneric.class), 1, 0);


        Method m = ClassWithGeneric.class.getDeclaredMethod("hello", Servlet.class);
        GenericMethod gm = new GenericMethod(m,ClassWithGeneric.class);
        TestFactory.getInstance().addMethodFor(tc, genericClass, gm, 2);


        //Check if generic types were correctly analyzed/inferred
        Type[] types = gm.getParameterTypes();
        Assert.assertEquals(1, types.length); //only 1 input
        Type type = types[0];
        Assert.assertNotNull(type);
        TypeVariable<?> tv = (TypeVariable<?>) type;
        Assert.assertEquals(1,tv.getBounds().length);

        Class<?> upper = (Class<?>) tv.getBounds()[0];
        Assert.assertEquals(Servlet.class,upper);


        //Finally, visit the test
        TestCodeVisitor visitor = new TestCodeVisitor();
        tc.accept(visitor); //should not throw exception
    }

    @Test
    public void testGenerics_staticMethod() throws NoSuchMethodException, ConstructionFailedException {

        //first construct a test case for the Generic method
        TestCase tc = new DefaultTestCase();
        TestFactory.getInstance().addConstructor(tc,
                new GenericConstructor(Object.class.getDeclaredConstructor(), Object.class), 0, 0);

        Method m = TestCodeVisitorTest.class.getDeclaredMethod("bar", Object.class);
        GenericMethod gm = new GenericMethod(m,TestCodeVisitorTest.class);
        TestFactory.getInstance().addMethod(tc, gm, 1, 0);


        //Check if generic types were correctly analyzed/inferred
        Type[] types = gm.getParameterTypes();
        Assert.assertEquals(1, types.length); //only 1 input
        Type type = types[0];
        Assert.assertNotNull(type);
        WildcardTypeImpl wt = (WildcardTypeImpl) type;
        Assert.assertEquals(0,wt.getLowerBounds().length);
        Assert.assertEquals(1,wt.getUpperBounds().length);

        Class<?> upper = (Class<?>) wt.getUpperBounds()[0];
        Assert.assertEquals(Object.class,upper);


        //Finally, visit the test
        TestCodeVisitor visitor = new TestCodeVisitor();
        tc.accept(visitor); //should not throw exception
    }

    @Test
    public void testGenerics_staticMethodWithExtends() throws NoSuchMethodException, ConstructionFailedException {

        //first construct a test case for the Generic method
        TestCase tc = new DefaultTestCase();
        VariableReference servlet = TestFactory.getInstance().addConstructor(tc,
                new GenericConstructor(FakeServlet.class.getDeclaredConstructor(), FakeServlet.class), 0, 0);

        Method m = TestCodeVisitorTest.class.getDeclaredMethod("foo", Servlet.class);
        GenericMethod gm = new GenericMethod(m,TestCodeVisitorTest.class);
        TestFactory.getInstance().addMethod(tc, gm, 1, 0);


        //Check if generic types were correctly analyzed/inferred
        Type[] types = gm.getParameterTypes();
        Assert.assertEquals(1, types.length); //only 1 input
        Type type = types[0];
        Assert.assertNotNull(type);
        WildcardTypeImpl wt = (WildcardTypeImpl) type;
        Assert.assertEquals(0,wt.getLowerBounds().length);
        Assert.assertEquals(1,wt.getUpperBounds().length);

        Class<?> upper = (Class<?>) wt.getUpperBounds()[0];
        Assert.assertEquals(Servlet.class,upper);


        //Finally, visit the test
        TestCodeVisitor visitor = new TestCodeVisitor();
        tc.accept(visitor); //should not throw exception
    }
}
