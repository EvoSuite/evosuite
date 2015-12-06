package org.evosuite.runtime.mock.javax.naming;

import org.evosuite.runtime.Runtime;
import org.evosuite.runtime.RuntimeSettings;
import org.evosuite.runtime.javaee.TestDataJavaEE;
import org.evosuite.runtime.mock.MockFramework;
import org.junit.Before;
import org.junit.Test;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import static org.junit.Assert.*;

/**
 * Created by Andrea Arcuri on 06/12/15.
 */
public class MockInitialContextTest {

    private interface AnInterface{}

    private class AClass implements AnInterface{}

    @Before
    public void init(){
        RuntimeSettings.useJEE = true;
        Runtime.getInstance().resetRuntime();
    }

    @Test
    public void testBasicLookupFail(){

        try {
            InitialContext ic = new InitialContext();
            ic.lookup("global/service/AClass!AnInterface");
            fail();
        } catch (NamingException e) {
            //expected
        }
    }

    @Test
    public void testBasicBindAndLookup(){

        AnInterface foo = new AClass();
        try {
            InitialContext ic = new InitialContext();
            ic.bind("service",foo);
            //no context is available/initialized
            fail();
        } catch (NamingException e) {
            //expected
        }
    }


    @Test
    public void testLookup() throws Exception{
        InitialContext ic = new MockInitialContext();
        String name = "global/service/AClass!AnInterface";

        Object obj =  ic.lookup(name);
        assertNull(obj);
        assertEquals(1 , TestDataJavaEE.getInstance().getViewOfLookedUpContextNames().size());
        assertEquals(name, TestDataJavaEE.getInstance().getViewOfLookedUpContextNames().iterator().next());
    }


    @Test
    public void testBindingFailClass() throws Exception{

        InitialContext ic = new MockInitialContext();
        String name = "global/service/AClass!aClassThatDoesNotExist";

        AnInterface k = new AClass();
        try {
            ic.bind(name, k);
            fail();
        } catch (NamingException e){
            //expected
        }
    }

    @Test
    public void testBindingOKClass() throws Exception{

        InitialContext ic = new MockInitialContext();
        String name = "global/service/AClass!"+AnInterface.class.getName();

        AnInterface k = new AClass();
        ic.bind(name, k);
    }

    @Test
    public void testBindAndLookup() throws Exception {

            InitialContext ic = new MockInitialContext();
            String name = "global/service/AClass";
            Object obj =  ic.lookup(name);
            assertNull(obj);

            AnInterface k = new AClass();
            ic.bind(name, k);

            Object res = ic.lookup(name);
            assertEquals(k, res);

    }

    @Test
    public void testFailDoubleBind() throws Exception{
        InitialContext ic = new MockInitialContext();
        String name = "global/service/AClass";
        Object obj =  ic.lookup(name);
        assertNull(obj);

        AnInterface k = new AClass();
        ic.bind(name, k);

        try{
            ic.bind(name, k);
            fail();
        } catch (NamingException e){
            //expected
        }
    }


    @Test
    public void testUnbind() throws Exception{

        InitialContext ic = new MockInitialContext();
        String name = "foo/service/AClass";

        AnInterface k = new AClass();
        ic.bind(name, k);

        Object res = ic.lookup(name);
        assertNotNull(res);

        ic.unbind(name);

        res = ic.lookup(name);
        assertNull(res);
    }
}