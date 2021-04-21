package org.evosuite.seeding;

import ch.qos.logback.classic.Level;
import org.evosuite.utils.generic.GenericClass;
import org.evosuite.utils.generic.GenericClassFactory;
import org.junit.Test;

import java.lang.reflect.*;
import java.util.*;
import java.util.function.Function;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.hamcrest.core.IsEqual.equalTo;


public class TestCastClassManager {

    @Test
    public void testSelectClass(){
        GenericClass<?> intClass = GenericClassFactory.get(int.class);
        List<GenericClass<?>> ts = Collections.singletonList(intClass);
        GenericClass<?> genericClass = CastClassManager.selectClass(ts);
        assertThat(genericClass, equalTo(intClass));
    }

    @Test
    public void testAddCastClass(){
        CastClassManager instance = CastClassManager.getInstance();
        instance.addCastClass("java.lang.Integer", 5);
        Set<GenericClass<?>> castClasses = instance.getCastClasses();
        assertThat(castClasses, hasItem(GenericClassFactory.get(Integer.class)));

        instance.clear();
        Type t = Integer.class;
        instance.addCastClass(t, 5);
        castClasses = instance.getCastClasses();
        assertThat(castClasses, hasItem(GenericClassFactory.get(Integer.class)));

        instance.clear();
        GenericClass<?> gc = GenericClassFactory.get(t);
        instance.addCastClass(gc, 5);
        castClasses = instance.getCastClasses();
        assertThat(castClasses, hasItem(GenericClassFactory.get(Integer.class)));
    }
}