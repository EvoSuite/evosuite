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

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * Created by Andrea Arcuri on 20/05/15.
 */
public class EvoHttpServletResponseTest {

    @Test
    public void testSimpleWrite() throws IOException {
        String a = "<html>";
        String b = "foo";
        String c = "</html>";

        EvoHttpServletResponse res = new EvoHttpServletResponse();
        Assert.assertFalse(res.isCommitted());

        PrintWriter out = res.getWriter();
        out.print(a);
        out.print(b);
        out.print(c);

        Assert.assertEquals(EvoHttpServletResponse.WARN_NO_COMMITTED, res.getBody());

        out.close();

        Assert.assertTrue(res.isCommitted());
        Assert.assertEquals(a+b+c, res.getBody());
    }
}
