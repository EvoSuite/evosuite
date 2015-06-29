package org.evosuite.testcase.jee;

import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by Andrea Arcuri on 29/06/15.
 */
public class InjectionSupportTest {

    @Test
    public void testGetInjectorForEntityManager() throws Exception {
        Assert.assertNotNull(InjectionSupport.getInjectorForEntityManager());
    }
}