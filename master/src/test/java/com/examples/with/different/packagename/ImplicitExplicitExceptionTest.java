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
package com.examples.with.different.packagename;

import org.junit.Test;

import static org.junit.Assert.fail;

/**
 * Created by gordon on 03/01/2016.
 */
public class ImplicitExplicitExceptionTest {

    @Test
    public void testImplicit() {
        try {
            ImplicitExplicitException sut = new ImplicitExplicitException();
            sut.implicit(null);
            fail("Expected NullPointerException");
        } catch(NullPointerException e) {

        }
    }

    @Test
    public void testExplicit() {
        try {
            ImplicitExplicitException sut = new ImplicitExplicitException();
            sut.explicit(null);
            fail("Expected NullPointerException");
        } catch(NullPointerException e) {

        }
    }

    @Test
    public void testImplicitDeclared() {
        try {
            ImplicitExplicitException sut = new ImplicitExplicitException();
            sut.implicitDeclared(null);
            fail("Expected NullPointerException");
        } catch(NullPointerException e) {

        }
    }

    @Test
    public void testExplicitDeclared() {
        try {
            ImplicitExplicitException sut = new ImplicitExplicitException();
            sut.explicitDeclared(null);
            fail("Expected NullPointerException");
        } catch(NullPointerException e) {

        }
    }

    @Test
    public void testDirectExplicitDeclared() {
        try {
            ImplicitExplicitException sut = new ImplicitExplicitException();
            sut.directExplicitDeclared();
            fail("Expected IllegalArgumentException");
        } catch(IllegalArgumentException e) {

        }
    }


}
