package org.evosuite.runtime.javaee.javax.servlet.http;

import org.evosuite.runtime.javaee.TestDataJavaEE;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * Used to test both Request and Response together
 *
 * Created by Andrea Arcuri on 20/05/15.
 */
public class HttpServletTest {

    @Before
    public void init(){
        TestDataJavaEE.getInstance().reset();
    }

    @Test
    public void testSimpleScenarioWithParams() throws IOException {

        EvoSuiteHttpServletRequest req = new EvoSuiteHttpServletRequest();
        EvoSuiteHttpServletResponse res = new EvoSuiteHttpServletResponse();

        req.addParam("foo", "bar");

        PrintWriter out = res.getWriter();
        out.print(req.getParameter("foo"));
        res.flushBuffer();

        Assert.assertEquals("bar",res.getBody());
    }
}
