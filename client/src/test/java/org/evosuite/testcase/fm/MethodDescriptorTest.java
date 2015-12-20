package org.evosuite.testcase.fm;

import org.junit.Test;

import java.awt.*;
import java.lang.reflect.Method;

import static org.junit.Assert.*;

/**
 * Created by foo on 20/12/15.
 */
public class MethodDescriptorTest {

    @Test
    public void testMatcher() throws Exception{

        Class<?> klass = Graphics2D.class;
        Method m = klass.getDeclaredMethod("getRenderingHint",RenderingHints.Key.class);

        MethodDescriptor md = new MethodDescriptor(m,m.getReturnType());

        String res = md.getInputParameterMatchers();
        assertTrue(res, res.contains("any("));
        assertTrue(res, res.contains("RenderingHints"));
        assertTrue(res, res.contains("Key"));

        assertFalse(res, res.contains("$"));
    }
}