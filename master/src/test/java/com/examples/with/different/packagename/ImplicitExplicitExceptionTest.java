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
