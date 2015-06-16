package org.evosuite.runtime.javaee.injection;

import org.junit.Assert;
import org.junit.Test;

import javax.annotation.PostConstruct;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.UserTransaction;

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

    @Test
    public void testHasPostConstruct(){
        Assert.assertTrue(Injector.hasPostConstruct(Foo.class));
        Assert.assertFalse(Injector.hasPostConstruct(String.class));
    }

    @Test
    public void testInjection_EntityManager(){

        Foo foo = new Foo();
        Assert.assertNull(foo.getPersistence());
        Assert.assertNull(foo.getEM());

        Assert.assertTrue(Injector.hasEntityManager(Foo.class));
        Injector.injectEntityManager(foo, Foo.class);

        Assert.assertNull(foo.getPersistence());//this should had been skipped, as invalid type
        Assert.assertNotNull(foo.getEM());
    }


    @Test
    public void testInjection_Event(){
        Foo foo = new Foo();
        Assert.assertNull(foo.getEvent());

        Assert.assertTrue(Injector.hasEvent(Foo.class));
        Injector.injectEvent(foo, Foo.class);

        Assert.assertNotNull(foo.getEvent());
    }

    @Test
    public void testInjection_UserTransaction(){
        Foo foo = new Foo();
        Assert.assertNull(foo.getUserTransaction());

        Assert.assertTrue(Injector.hasUserTransaction(Foo.class));
        Injector.injectUserTransaction(foo, Foo.class);

        Assert.assertNotNull(foo.getUserTransaction());
    }


    private class Foo {

        private Object noTag;

        @Inject
        private Event event;

        @Inject
        private UserTransaction userTransaction;

        @Inject
        private Object injectField;

        @PersistenceContext
        private Object persistence;

        @PersistenceContext
        private EntityManager em;

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

        public EntityManager getEM(){
            return em;
        }

        public UserTransaction getUserTransaction() {
            return userTransaction;
        }

        public Event getEvent() {
            return event;
        }
    }
}