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
package org.evosuite.testcase.jee;

import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by Andrea Arcuri on 29/06/15.
 */
public class InjectionSupportTest {

    @Test
    public void testGetInjectorForEntityManager() throws Exception {
        Assert.assertNotNull(InjectionSupport.getInjectorForEntityManager());
    }

    @Test
    public void testGetInjectorForEntityManagerFactory() throws Exception {
        Assert.assertNotNull(InjectionSupport.getInjectorForEntityManagerFactory());
    }

    @Test
    public void testGetInjectorForUserTransaction() throws Exception {
        Assert.assertNotNull(InjectionSupport.getInjectorForUserTransaction());
    }

    @Test
    public void testGetInjectorForEvent() throws Exception {
        Assert.assertNotNull(InjectionSupport.getInjectorForEvent());
    }
}