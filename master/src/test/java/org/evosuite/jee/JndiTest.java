package org.evosuite.jee;

import com.examples.with.different.packagename.jee.jndi.NoCastJndiLookup;
import org.evosuite.Properties;
import org.evosuite.SystemTest;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by Andrea Arcuri on 06/12/15.
 */
public class JndiTest extends SystemTest{

    @Before
    public void init(){
        Properties.JEE = true;
    }

    @Test
    public void testNoCast(){
        do100percentLineTest(NoCastJndiLookup.class);
    }
}
