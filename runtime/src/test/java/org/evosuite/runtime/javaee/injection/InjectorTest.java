/**
 * Copyright (C) 2010-2016 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3.0 of the License, or
 * (at your option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.runtime.javaee.injection;

import org.evosuite.runtime.FalsePositiveException;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.enterprise.event.Event;
import javax.faces.bean.ManagedProperty;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceUnit;
import javax.transaction.UserTransaction;
import javax.ws.rs.core.Context;
import javax.xml.ws.WebServiceRef;

import java.lang.reflect.Field;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

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

        Injector.executePostConstruct(foo, Foo.class);

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

    @Test
    public void testInjection_EMFactory(){
        Foo foo = new Foo();
        Assert.assertNull(foo.getFactory());

        Assert.assertTrue(Injector.hasEntityManagerFactory(Foo.class));
        Injector.injectEntityManagerFactory(foo, Foo.class);

        Assert.assertNotNull(foo.getFactory());
    }

    @Test
    public void test_getGeneralFieldsToInject_Foo(){
        List<Field> list = Injector.getGeneralFieldsToInject(Foo.class);
        Assert.assertEquals(5, list.size());
        Set<String> names = new LinkedHashSet<>();
        for(Field f : list){
            names.add(f.getName());
        }
        Assert.assertTrue(names.contains("aString"));
        Assert.assertTrue(names.contains("injectField"));
        Assert.assertTrue(names.contains("persistence"));
        Assert.assertTrue(names.contains("springWired"));
        Assert.assertTrue(names.contains("aResource"));
    }

    @Test
    public void test_getGeneralFieldsToInject_Bar(){
        List<Field> list = Injector.getGeneralFieldsToInject(Bar.class);
        Assert.assertEquals(4, list.size());
        Set<String> names = new LinkedHashSet<>();
        for(Field f : list){
            names.add(f.getName());
        }
        Assert.assertTrue(names.contains("ejb"));
        Assert.assertTrue(names.contains("webServiceRef"));
        Assert.assertTrue(names.contains("managedProperty"));
        Assert.assertTrue(names.contains("context"));
    }


    @Test
    public void test_getGeneralFieldsToInject_subclass_differentField(){
        List<Field> list = Injector.getGeneralFieldsToInject(SubclassDifferentField.class);
        Assert.assertEquals(1, list.size());
        Assert.assertEquals("aDifferentString", list.get(0).getName());
    }

    @Test
    public void test_getGeneralFieldsToInject_subclass_sameField(){
        List<Field> list = Injector.getGeneralFieldsToInject(SubclassSameField.class);
        Assert.assertEquals(1 , list.size());
        Assert.assertEquals("aString", list.get(0).getName());
    }


    @Test
    public void testValidateBean_A_invalid(){
        A a = new A();
        try {
            Injector.validateBean(a, A.class);
            fail();
        } catch (FalsePositiveException e){
            //OK
        }
    }

    @Test
    public void testValidateBean_A_ok(){
        A a = new A();
        try {
            Injector.inject(a,A.class,"a","foo");
            Injector.validateBean(a, A.class);
        } catch (FalsePositiveException e){
            fail();
        }
    }


    @Test
    public void testValidateBean_B_invalid_AB(){
        B b = new B();
        try {
            Injector.validateBean(b, B.class);
            fail();
        } catch (FalsePositiveException e){
            //OK
        }
    }

    @Test
    public void testValidateBean_B_invalid_A(){
        B b = new B();
        try {
            Injector.inject(b,B.class,"b","bar");
            Injector.validateBean(b, B.class);
            fail();
        } catch (FalsePositiveException e){
            //OK
        }
    }


    @Test
    public void testValidateBean_B_invalid_B(){
        B b = new B();
        try {
            Injector.inject(b,A.class,"a","foo");
            Injector.validateBean(b, B.class);
            fail();
        } catch (FalsePositiveException e){
            //OK
        }
    }


    @Test
    public void testValidateBean_B_ok(){
        B b = new B();
        try {
            Injector.inject(b,A.class,"a","foo");
            Injector.inject(b,B.class,"b","bar");
            Injector.validateBean(b, B.class);
        } catch (FalsePositiveException e){
            fail();
        }
    }

    //---------------------------------------------


    private static class SubclassDifferentField extends Foo{
        @Inject
        private String aDifferentString;
    }

    private static class SubclassSameField extends Foo{
        @Inject
        private String aString;
    }


    private static class A {
        @Autowired
        private String a;
    }

    private static class B extends A{
        @Resource
        private String b;
    }

    
    private static class Bar {
        
        @EJB
        private Object ejb;
        
        @WebServiceRef
        private Object webServiceRef;
        
        @ManagedProperty(value = "")
        private Object managedProperty;

        @Context
        private Object context;

        private Object noTag;
    }

    private static class Foo {

        private Object noTag;

        @PersistenceUnit
        private EntityManagerFactory factory;

        @Autowired
        private String springWired;

        @Resource
        private String aResource;

        @Inject
        private Event event;

        @Inject
        private UserTransaction userTransaction;

        @Inject
        private String aString;

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

        public EntityManagerFactory getFactory() {
            return factory;
        }
    }
}