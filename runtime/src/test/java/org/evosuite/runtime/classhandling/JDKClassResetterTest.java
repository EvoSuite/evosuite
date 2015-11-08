package org.evosuite.runtime.classhandling;


import org.junit.Test;

import java.awt.*;

import static org.junit.Assert.fail;

/**
 * Created by Andrea Arcuri on 08/11/15.
 */
public class JDKClassResetterTest  {

    private static class FooKey extends RenderingHints.Key{

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

        try{
            FooKey copy = new FooKey(keyValue);
            fail();
        } catch(Exception e){
            //expected, as cannot make a copy
        }

        JDKClassResetter.reset();

        //after reset, copy should be fine
        FooKey copy = new FooKey(keyValue);
    }
}