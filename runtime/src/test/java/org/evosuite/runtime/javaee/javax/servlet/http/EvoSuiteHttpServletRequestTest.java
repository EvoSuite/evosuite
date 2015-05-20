package org.evosuite.runtime.javaee.javax.servlet.http;

import org.evosuite.runtime.javaee.TestDataJavaEE;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by Andrea Arcuri on 20/05/15.
 */
public class EvoSuiteHttpServletRequestTest {

    @Before
    public void init(){
        TestDataJavaEE.getInstance().reset();
    }

    @Test
    public void testGetUri(){
        EvoSuiteHttpServletRequest req = new EvoSuiteHttpServletRequest();
        Assert.assertNotNull(req.getRequestURI());
    }

    @Test
    public void testAsPost(){
        String POST = "POST";
        EvoSuiteHttpServletRequest req = new EvoSuiteHttpServletRequest();
        String m = req.getMethod();
        Assert.assertNotEquals(m,POST); //default should not be POST
        req.asPOST();
        m = req.getMethod();
        Assert.assertEquals(POST, m);
    }

    @Test
    public void testAccessToParam(){
        String param = "foo";
        Assert.assertFalse(TestDataJavaEE.getInstance().getViewOfHttpRequestParameters().contains(param));

        EvoSuiteHttpServletRequest req = new EvoSuiteHttpServletRequest();
        String val = req.getParameter(param);
        Assert.assertNull(val);
        Assert.assertTrue(TestDataJavaEE.getInstance().getViewOfHttpRequestParameters().contains(param));

        req.addParam(param, "some value");
        val = req.getParameter(param);
        Assert.assertNotNull(val);
        Assert.assertTrue(TestDataJavaEE.getInstance().getViewOfHttpRequestParameters().contains(param));
    }

    @Test
    public void testParametersMap(){
        String p1 = "p1";
        String p2 = "p2";

        Assert.assertFalse(TestDataJavaEE.getInstance().getViewOfHttpRequestParameters().contains(p1));
        Assert.assertFalse(TestDataJavaEE.getInstance().getViewOfHttpRequestParameters().contains(p2));

        EvoSuiteHttpServletRequest req = new EvoSuiteHttpServletRequest();
        req.getParameterMap().containsKey(p1);

        Assert.assertTrue(TestDataJavaEE.getInstance().getViewOfHttpRequestParameters().contains(p1));
        Assert.assertFalse(TestDataJavaEE.getInstance().getViewOfHttpRequestParameters().contains(p2));

        req.getParameterMap().keySet().contains(p2);

        Assert.assertTrue(TestDataJavaEE.getInstance().getViewOfHttpRequestParameters().contains(p1));
        Assert.assertTrue(TestDataJavaEE.getInstance().getViewOfHttpRequestParameters().contains(p2));
    }
}
