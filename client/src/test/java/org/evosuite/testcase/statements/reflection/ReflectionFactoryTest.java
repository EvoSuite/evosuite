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
package org.evosuite.testcase.statements.reflection;

import org.evosuite.Properties;
import org.evosuite.runtime.javaee.injection.Injector;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import java.lang.reflect.Field;

import static org.junit.Assert.*;

/**
 * Created by Andrea Arcuri on 11/08/15.
 */
public class ReflectionFactoryTest {

    private static final boolean DEFALUT_JEE = Properties.JEE;

    public static class Foo{

        private int x;
        public double p;

        @PersistenceContext
        private EntityManager em;
    }

    @After
    public void tearDown(){
        Properties.JEE = DEFALUT_JEE;
    }


    @Test
    public void testGetNumberOfUsableFields() throws Exception {

        Properties.JEE = false;
        Injector.reset();
        ReflectionFactory rf = new ReflectionFactory(Foo.class);
        Assert.assertEquals(2, rf.getNumberOfUsableFields());
        Assert.assertEquals(1, Injector.getAllFieldsToInject(Foo.class).size());
        Assert.assertTrue(Injector.hasEntityManager(Foo.class));

        Properties.JEE = true;
        Injector.reset();
        rf = new ReflectionFactory(Foo.class);
        Assert.assertEquals(1, rf.getNumberOfUsableFields()); //now em should not be accessible any more for Private Access
        Assert.assertEquals(1, Injector.getAllFieldsToInject(Foo.class).size());
        Assert.assertTrue(Injector.hasEntityManager(Foo.class));

        Field f = rf.nextField();
        Assert.assertEquals(Foo.class.getDeclaredField("x") , f);
    }
}