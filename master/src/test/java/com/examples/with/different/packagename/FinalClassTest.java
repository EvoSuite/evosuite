package com.examples.with.different.packagename;

import static org.junit.Assert.assertTrue;

import java.lang.reflect.Modifier;

import org.junit.Test;

public class FinalClassTest  {

    @Test
    public void testClass() {
        assertTrue(Modifier.isFinal(FinalClass.class.getModifiers()));
    }
}
