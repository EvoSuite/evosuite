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
package org.evosuite.runtime.classhandling;


import org.junit.Test;

import java.awt.*;

import static org.junit.Assert.fail;

/**
 * Created by Andrea Arcuri on 08/11/15.
 */
public class JDKClassResetterTest {

    private static class FooKey extends RenderingHints.Key {

        public FooKey(int privatekey) {
            super(privatekey);
        }

        @Override
        public boolean isCompatibleValue(Object val) {
            return false;
        }
    }

    @Test
    public void testReset() throws Exception {

        JDKClassResetter.init();
        int keyValue = 1234567;

        //this should be fine
        FooKey first = new FooKey(keyValue);

        try {
            FooKey copy = new FooKey(keyValue);
            fail();
        } catch (Exception e) {
            //expected, as cannot make a copy
        }

        JDKClassResetter.reset();

        //after reset, copy should be fine
        FooKey copy = new FooKey(keyValue);
    }
}