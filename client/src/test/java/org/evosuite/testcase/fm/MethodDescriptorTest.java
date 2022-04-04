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
package org.evosuite.testcase.fm;

import org.evosuite.utils.generic.GenericClassFactory;
import org.junit.Test;

import java.awt.*;
import java.lang.reflect.Method;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by foo on 20/12/15.
 */
public class MethodDescriptorTest {

    @Test
    public void testMatcher() throws Exception {

        Class<?> klass = Graphics2D.class;
        Method m = klass.getDeclaredMethod("getRenderingHint", RenderingHints.Key.class);

        MethodDescriptor md = new MethodDescriptor(m, GenericClassFactory.get(m.getReturnType()));

        String res = md.getInputParameterMatchers();
        assertTrue(res, res.contains("any("));
        assertTrue(res, res.contains("RenderingHints"));
        assertTrue(res, res.contains("Key"));

        assertFalse(res, res.contains("$"));
    }
}