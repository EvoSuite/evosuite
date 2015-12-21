package org.evosuite.jee;

import com.examples.with.different.packagename.jee.jndi.*;
import org.evosuite.Properties;
import org.evosuite.SystemTestBase;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by Andrea Arcuri on 06/12/15.
 */
public class JndiSystemTest extends SystemTestBase {

    @Before
    public void init(){
        Properties.JEE = true;
    }

    @Test
    public void testNoCast(){
        do100percentLineTest(NoCastJndiLookup.class);
    }

    @Test
    public void testBeanCastJndiLookupNoHint(){
        do100percentLineTest(BeanCastJndiLookupNoHint.class);
    }

    @Test
    public void testBeanCastJndiLookupWithHint(){
        do100percentLineTest(BeanCastJndiLookupWithHint.class);
    }

    @Test
    public void testStringJndiLookupNoHint(){
        do100percentLineTest(StringJndiLookupNoHint.class);
    }

    @Test
    public void testStringJndiLookupWithHint(){
        do100percentLineTest(StringJndiLookupWithHint.class);
    }

}
