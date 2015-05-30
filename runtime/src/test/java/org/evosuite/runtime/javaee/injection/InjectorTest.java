package org.evosuite.runtime.javaee.injection;

import org.junit.Assert;
import org.junit.Test;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.persistence.PersistenceContext;

import static org.junit.Assert.*;

/**
 * Created by Andrea Arcuri on 30/05/15.
 */
public class InjectorTest {

    @Test
    public void testInject_noTag() throws Exception {

        Foo foo = new Foo();

        try {
            Injector.inject(foo, Foo.class, "noTag", new Integer(1));
            Assert.fail();
        } catch (Exception e){
            //expected
        }
        Assert.assertNull(foo.getNoTag());
    }

    @Test
    public void testInject_inject() throws Exception {

        Foo foo = new Foo();

        Injector.inject(foo, Foo.class, "injectField", new Integer(1));

        Assert.assertNotNull(foo.getInjectField());
    }

    @Test
    public void testInject_persistence() throws Exception {

        Foo foo = new Foo();

        Injector.inject(foo, Foo.class, "persistence", new Integer(1));

        Assert.assertNotNull(foo.getPersistence());
    }

    @Test
    public void testExecutePostConstruct() throws Exception {

        Foo foo = new Foo();
        Assert.assertFalse(foo.isInit());

        Injector.executePostConstruct(foo);

        Assert.assertTrue(foo.isInit());
    }


    private class Foo {

        private Object noTag;

        @Inject
        private Object injectField;

        @PersistenceContext
        private Object persistence;

        private boolean init = false;

        @PostConstruct
        private void doInit(){
            init = true;
        }

        public boolean isInit(){
            return init;
        }

        public Object getNoTag() {
            return noTag;
        }

        public Object getInjectField() {
            return injectField;
        }

        public Object getPersistence() {
            return persistence;
        }
    }
}