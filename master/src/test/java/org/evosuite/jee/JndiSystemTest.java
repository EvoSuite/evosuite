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
package org.evosuite.jee;

import com.examples.with.different.packagename.jee.jndi.*;
import org.evosuite.Properties;
import org.evosuite.SystemTestBase;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by Andrea Arcuri on 06/12/15.
 */
public class JndiSystemTest extends SystemTestBase {

    @Before
    public void init(){
        Properties.JEE = true;
    }

    @Test
    public void testNoCast(){
        do100percentLineTest(NoCastJndiLookup.class);
    }

    @Test
    public void testBeanCastJndiLookupNoHint(){
        do100percentLineTest(BeanCastJndiLookupNoHint.class);
    }

    @Test
    public void testBeanCastJndiLookupWithHint(){
        do100percentLineTest(BeanCastJndiLookupWithHint.class);
    }

    @Test
    public void testStringJndiLookupNoHint(){
        do100percentLineTest(StringJndiLookupNoHint.class);
    }

    @Test
    public void testStringJndiLookupWithHint(){
        do100percentLineTest(StringJndiLookupWithHint.class);
    }

}
