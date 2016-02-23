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
import javax.servlet.http.Part;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Scanner;

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
    public void testExecutedMethod() throws ServletException, IOException{

        final boolean[] val = new boolean[3];

        HttpServlet servlet = new HttpServlet() {
            @Override
            public void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
                val[0] = true;
            }
            @Override
            public void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
                val[1] = true;
            }
            @Override
            public void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
                val[2] = true;
            }
        };

        EvoServletState.initServlet(servlet);
        EvoHttpServletRequest req = EvoServletState.getRequest();
        req.asPOST();
        servlet.service(req, EvoServletState.getResponse());
        Assert.assertTrue(val[0]);
        Assert.assertTrue(!val[1]);
        Assert.assertTrue(!val[2]);

        val[0] = false;
        EvoServletState.reset();
        EvoServletState.initServlet(servlet);
        req = EvoServletState.getRequest();
        req.asGET();
        servlet.service(req, EvoServletState.getResponse());
        Assert.assertTrue(!val[0]);
        Assert.assertTrue(val[1]);
        Assert.assertTrue(! val[2]);

        val[1] = false;
        EvoServletState.reset();
        EvoServletState.initServlet(servlet);
        req = EvoServletState.getRequest();
        req.asPUT();
        servlet.service(req, EvoServletState.getResponse());
        Assert.assertTrue(!val[0]);
        Assert.assertTrue(!val[1]);
        Assert.assertTrue(val[2]);

    }

    @Test
    public void testParts() throws ServletException, IOException{

        HttpServlet servlet = new HttpServlet() {
            @Override
            public void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
                Collection<Part> parts = req.getParts();
                String s = "";
                for(Part p : parts){
                    Scanner in = new Scanner(p.getInputStream());
                    s += in.nextLine();
                    in.close();
                }
                PrintWriter out = resp.getWriter();
                out.print(s);
                out.close();
            }
        };

        String msg0 = "foo";
        String msg1 = "bar";

        EvoServletState.initServlet(servlet);
        EvoHttpServletRequest req = EvoServletState.getRequest();
        req.asPOST();
        req.asMultipartFormData();
        req.addPart(new EvoPart("first", msg0));
        req.addPart(new EvoPart("second", msg1));

        servlet.service(req, EvoServletState.getResponse());

        Assert.assertTrue(TestDataJavaEE.getInstance().getViewOfParts().size() == 0);
        Assert.assertTrue(EvoServletState.getResponse().getBody().equals(msg0+msg1));
    }

    @Test
    public void testContentType() throws ServletException, IOException {

        HttpServlet servlet = new HttpServlet() {
            @Override
            public void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
                String contentType = req.getContentType();
                if(!contentType.equals("multipart/form-data")){
                    resp.sendError(42);
                    return;
                }
                resp.flushBuffer();
            }
        };

        Assert.assertFalse(TestDataJavaEE.getInstance().wasContentTypeRead());

        EvoServletState.initServlet(servlet);
        EvoHttpServletRequest req = EvoServletState.getRequest();
        req.asPOST();
        req.asTextXml();

        servlet.service(req, EvoServletState.getResponse());

        Assert.assertTrue(TestDataJavaEE.getInstance().wasContentTypeRead());
        Assert.assertTrue(EvoServletState.getResponse().getBody().contains("42"));

        EvoServletState.reset();
        EvoServletState.initServlet(servlet);
        req = EvoServletState.getRequest();
        req.asPOST();
        req.asMultipartFormData();

        Assert.assertFalse(EvoServletState.getResponse().isCommitted());
        servlet.service(req, EvoServletState.getResponse());

        Assert.assertFalse(EvoServletState.getResponse().getBody().contains("42"));
        Assert.assertTrue(EvoServletState.getResponse().isCommitted());
    }


    @Test
    public void testSimpleScenarioWithParams() throws IOException {

        EvoHttpServletRequest req = new EvoHttpServletRequest();
        EvoHttpServletResponse res = new EvoHttpServletResponse();

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

        EvoHttpServletRequest req = new EvoHttpServletRequest();
        EvoHttpServletResponse resp = new EvoHttpServletResponse();

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
        Assert.assertNotEquals(EvoHttpServletResponse.WARN_NO_COMMITTED, body);
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
