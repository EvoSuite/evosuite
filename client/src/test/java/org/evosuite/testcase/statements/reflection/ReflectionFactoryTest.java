/*
 * Copyright (C) 2010-2018 Gordon Fraser, Andrea Arcuri and EvoSuite
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
package org.evosuite.testcase.statements.reflection;

import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Field;

/**
 * Created by Andrea Arcuri on 11/08/15.
 */
public class ReflectionFactoryTest {

    public static class Foo {

        private int x;
        public double p;
    }


    @Test
    public void testGetNumberOfUsableFields() throws Exception {

        ReflectionFactory rf = new ReflectionFactory(Foo.class);
        Assert.assertEquals(1, rf.getNumberOfUsableFields());

        Field f = rf.nextField();
        Assert.assertEquals(Foo.class.getDeclaredField("x"), f);
    }
}