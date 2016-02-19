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
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by Andrea Arcuri on 20/05/15.
 */
public class EvoHttpServletRequestTest {

    @Before
    public void init(){
        TestDataJavaEE.getInstance().reset();
    }

    @Test
    public void testGetUri(){
        EvoHttpServletRequest req = new EvoHttpServletRequest();
        Assert.assertNotNull(req.getRequestURI());
    }

    @Test
    public void testAsPost(){
        String POST = "POST";
        EvoHttpServletRequest req = new EvoHttpServletRequest();
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

        EvoHttpServletRequest req = new EvoHttpServletRequest();
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

        EvoHttpServletRequest req = new EvoHttpServletRequest();
        req.getParameterMap().containsKey(p1);

        Assert.assertTrue(TestDataJavaEE.getInstance().getViewOfHttpRequestParameters().contains(p1));
        Assert.assertFalse(TestDataJavaEE.getInstance().getViewOfHttpRequestParameters().contains(p2));

        req.getParameterMap().keySet().contains(p2);

        Assert.assertTrue(TestDataJavaEE.getInstance().getViewOfHttpRequestParameters().contains(p1));
        Assert.assertTrue(TestDataJavaEE.getInstance().getViewOfHttpRequestParameters().contains(p2));
    }
}
