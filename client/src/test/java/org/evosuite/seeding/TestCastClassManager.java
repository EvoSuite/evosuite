package org.evosuite.seeding;

import org.evosuite.utils.generic.GenericClass;
import org.evosuite.utils.generic.GenericClassFactory;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsCollectionContaining.*;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.Set;


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