package com.examples.with.different.packagename;

import static org.junit.Assert.assertTrue;

import java.lang.reflect.Modifier;

import org.junit.Test;

public class FinalClassTest  {

    @Test
    public void testClass() {
        FinalClass f = new FinalClass();
        assertTrue(Modifier.isFinal(f.getClass().getModifiers()));
    }
}
