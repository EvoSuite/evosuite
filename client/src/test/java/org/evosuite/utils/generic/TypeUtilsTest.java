package org.evosuite.utils.generic;

import org.apache.commons.lang3.reflect.TypeUtils;
import org.apache.commons.lang3.reflect.Typed;
import org.junit.Test;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;

import static org.junit.Assert.*;

public class TypeUtilsTest {

    @Test
    public void testParameterizeGenerics(){
        Class<Comparable> rawClass = Comparable.class;
        ParameterizedType parameterize = TypeUtils.parameterize(rawClass, rawClass.getTypeParameters());
        Type rawType = parameterize.getRawType();
        Type[] actualTypeArguments = parameterize.getActualTypeArguments();
        assertEquals(rawClass, rawType);
        assertEquals(1, actualTypeArguments.length);
        Type actualTypeArgument = actualTypeArguments[0];
        assertTrue(actualTypeArgument instanceof TypeVariable);
        TypeVariable<?> typeArgument = (TypeVariable<?>) actualTypeArgument;
        Type[] bounds = typeArgument.getBounds();
        assertEquals(1,bounds.length);
        Type bound = bounds[0];
        assertEquals(Object.class, bound);
    }

    @Test
    public void testParameterizeNoGenerics(){
        Class<Integer> rawClass = Integer.class;
        ParameterizedType parameterize = TypeUtils.parameterize(rawClass, rawClass.getTypeParameters());
        Type rawType = parameterize.getRawType();
        Type[] actualTypeArguments = parameterize.getActualTypeArguments();
        assertEquals(rawClass, rawType);
        assertEquals(0, actualTypeArguments.length);
    }

}