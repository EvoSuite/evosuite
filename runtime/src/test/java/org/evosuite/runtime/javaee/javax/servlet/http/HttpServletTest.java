package org.evosuite.runtime.javaee.javax.servlet.http;

import org.evosuite.runtime.javaee.TestDataJavaEE;
import org.evosuite.runtime.javaee.javax.servlet.EvoServletConfig;
import org.evosuite.runtime.javaee.javax.servlet.EvoServletState;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.servlet.AsyncContext;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
        EvoServletState.reset();
    }

    @Test
    public void testSimpleScenarioWithParams() throws IOException {

        EvoSuiteHttpServletRequest req = new EvoSuiteHttpServletRequest();
        EvoSuiteHttpServletResponse res = new EvoSuiteHttpServletResponse();

        req.addParam("foo", "bar");

        PrintWriter out = res.getWriter();
        out.print(req.getParameter("foo"));
        res.flushBuffer();

        Assert.assertEquals("bar", res.getBody());
    }


    @Test
    public void testInitServlet() throws Exception{

        final String delegate = "/result.jsp";

        Assert.assertFalse(TestDataJavaEE.getInstance().getViewOfDispatchers().contains(delegate));

        HttpServlet servlet = new HttpServlet() {
            @Override
            public void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
                RequestDispatcher dispatcher = getServletContext().getRequestDispatcher(delegate);
                dispatcher.forward(req , resp);
            }
        };

        EvoSuiteHttpServletRequest req = new EvoSuiteHttpServletRequest();
        EvoSuiteHttpServletResponse resp = new EvoSuiteHttpServletResponse();

        try{
            servlet.service(req,resp);
            Assert.fail();
        } catch(IllegalStateException e){
            //expected
        }

        EvoServletConfig conf = new EvoServletConfig();
        servlet.init(conf);

        try{
            servlet.service(req,resp);
            Assert.fail();
        } catch(NullPointerException e){
            //expected
        }

        conf.createDispatcher(delegate);
        servlet.init(conf);
        servlet.service(req, resp);

        String body = resp.getBody();
        Assert.assertNotEquals(EvoSuiteHttpServletResponse.WARN_NO_COMMITTED, body);
        Assert.assertTrue(body.length() > 0);
        Assert.assertTrue(body.contains(delegate)); //the name of the delegate should appear in the response

        Assert.assertTrue(TestDataJavaEE.getInstance().getViewOfDispatchers().contains(delegate));
    }

    @Test
    public void testNoAsyn() throws ServletException {

        HttpServlet servlet = new HttpServlet() {
            @Override
            public void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            }
        };
        EvoServletState.initServlet(servlet);
        boolean supported = EvoServletState.getRequest().isAsyncSupported();
        Assert.assertFalse(supported);
    }

    @Test
    public void testAsyn() throws ServletException, IOException {

        HttpServlet servlet = new  AnnotatedServlet_for_testAsyn();
        EvoServletState.initServlet(servlet);
        boolean supported = EvoServletState.getRequest().isAsyncSupported();
        Assert.assertTrue(supported);

        servlet.service(EvoServletState.getRequest(), EvoServletState.getResponse());

        String body = EvoServletState.getResponse().getBody();
        Assert.assertEquals("foo", body);
    }

    @WebServlet(value = "/bar", asyncSupported = true)
    private class AnnotatedServlet_for_testAsyn extends HttpServlet {
        @Override
        public void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            AsyncContext context = req.startAsync();
            PrintWriter out = context.getResponse().getWriter();
            out.print("foo");
            out.close();
        }
    }
}
