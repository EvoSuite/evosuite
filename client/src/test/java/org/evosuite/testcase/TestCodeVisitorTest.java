package org.evosuite.testcase;

import org.evosuite.ga.ConstructionFailedException;
import org.evosuite.testcase.statements.Statement;
import org.evosuite.testcase.variable.VariableReference;
import org.evosuite.utils.GenericConstructor;
import org.evosuite.utils.GenericMethod;
import org.junit.Test;

import javax.servlet.Servlet;
import javax.servlet.http.HttpServlet;
import java.lang.reflect.Method;

/**
 * Created by Andrea Arcuri on 02/07/15.
 */
public class TestCodeVisitorTest {

    public static <T extends Servlet> T foo(T servlet){ return servlet;}

    public static class FakeServlet extends HttpServlet {
        public FakeServlet(){}
    }


    @Test
    public void testGenerics() throws NoSuchMethodException, ConstructionFailedException {
        TestCase tc = new DefaultTestCase();

        VariableReference servlet = TestFactory.getInstance().addConstructor(tc,
                new GenericConstructor(FakeServlet.class.getDeclaredConstructor(), FakeServlet.class), 0, 0);


        Method m = TestCodeVisitorTest.class.getDeclaredMethod("foo", Servlet.class);
        GenericMethod gm = new GenericMethod(m,TestCodeVisitorTest.class);
        TestFactory.getInstance().addMethod(tc,gm,1,0);

        TestCodeVisitor visitor = new TestCodeVisitor();
        tc.accept(visitor);
    }
}
