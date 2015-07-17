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

    @Test
    public void testGetInjectorForEntityManagerFactory() throws Exception {
        Assert.assertNotNull(InjectionSupport.getInjectorForEntityManagerFactory());
    }

    @Test
    public void testGetInjectorForUserTransaction() throws Exception {
        Assert.assertNotNull(InjectionSupport.getInjectorForUserTransaction());
    }

    @Test
    public void testGetInjectorForEvent() throws Exception {
        Assert.assertNotNull(InjectionSupport.getInjectorForEvent());
    }
}