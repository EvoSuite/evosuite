package org.evosuite.utils.generic;

import org.apache.commons.lang3.reflect.TypeUtils;
import org.junit.Test;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class NewGenericClassImplTest {


    /**
     * Dummy class to understand what this class actually does
     */

    static class OneGenericUnboundParameter<T> {
        private T t;

        public OneGenericUnboundParameter(T t) {
            this.t = t;
        }
    }

    static class OneGenericBoundParameter<T extends Comparable<T>> {
        private T t;

        public OneGenericBoundParameter(T t) {
            this.t = t;
        }
    }

    static class DoubleBoundedParameter<T extends Comparable<T> & Cloneable> {
        private T t;

        public DoubleBoundedParameter(T t) {
            this.t = t;
        }
    }

    static class InnerGenerics<T extends OneGenericBoundParameter<?>> {
        T t;

        public InnerGenerics(T t) {
            this.t = t;
        }
    }

    static class TwoGenericUnboundParameters<A, B> {

        private final A a;
        private final B b;

        public TwoGenericUnboundParameters(A a, B b) {
            this.a = a;
            this.b = b;
        }
    }


    @Test
    public void testOneGenericUnboundParameter() {
        NewGenericClassImpl genericClass = new NewGenericClassImpl(OneGenericUnboundParameter.class);
        Type type = genericClass.getType();
        assertTrue(type instanceof ParameterizedType);
        Type[] actualTypeArguments = ((ParameterizedType) type).getActualTypeArguments();
        assertEquals(1, actualTypeArguments.length);
        Type actualTypeArgument = actualTypeArguments[0];
        assertTrue(actualTypeArgument instanceof TypeVariable);
        Type[] bounds = ((TypeVariable<?>) actualTypeArgument).getBounds();
        assertEquals(1, bounds.length);
        Type bound = bounds[0];
        assertEquals(Object.class.getName(), bound.getTypeName());
    }

    @Test
    public void testOneGenericBoundParameter() {
        NewGenericClassImpl genericClass = new NewGenericClassImpl(OneGenericBoundParameter.class);
        Type type = genericClass.getType();
        assertTrue(type instanceof ParameterizedType);
        Type[] actualTypeArguments = ((ParameterizedType) type).getActualTypeArguments();
        assertEquals(1, actualTypeArguments.length);
        Type actualTypeArgument = actualTypeArguments[0];
        assertTrue(actualTypeArgument instanceof TypeVariable);
        Type[] bounds = ((TypeVariable<?>) actualTypeArgument).getBounds();
        assertEquals(1, bounds.length);
        Type bound = bounds[0];
        assertEquals("java.lang.Comparable<T>", bound.getTypeName());
    }

    @Test
    public void testDoubleBoundedParameter() {
        NewGenericClassImpl genericClass = new NewGenericClassImpl(DoubleBoundedParameter.class);
        Type type = genericClass.getType();
        assertTrue(type instanceof ParameterizedType);
        Type[] actualTypeArguments = ((ParameterizedType) type).getActualTypeArguments();
        assertEquals(1, actualTypeArguments.length);
        Type actualTypeArgument = actualTypeArguments[0];
        assertTrue(actualTypeArgument instanceof TypeVariable);
        Type[] bounds = ((TypeVariable<?>) actualTypeArgument).getBounds();
        assertEquals(2, bounds.length);
        Type firstBound = bounds[0];
        Type secondBound = bounds[1];
        assertEquals("java.lang.Comparable<T>", firstBound.getTypeName());
        assertEquals(Cloneable.class.getName(), secondBound.getTypeName());
    }

    @Test
    public void testInnerGenerics() {
        NewGenericClassImpl genericClass = new NewGenericClassImpl(InnerGenerics.class);
        Type type = genericClass.getType();
        // Check if the returned value is a Parameterized Type
        assertTrue(type instanceof ParameterizedType);
        Type[] actualTypeArguments = ((ParameterizedType) type).getActualTypeArguments();
        // Check if exactly one Parameter is present
        assertEquals(1, actualTypeArguments.length);
        Type actualTypeArgument = actualTypeArguments[0];
        // The name of the parameter is T
        assertEquals("T", actualTypeArgument.getTypeName());
        assertTrue(actualTypeArgument instanceof TypeVariable);
        Type[] bounds = ((TypeVariable<?>) actualTypeArgument).getBounds();
        // Only one bound exists for T (extends OneGenericBoundParameter<?>)
        assertEquals(1, bounds.length);
        Type upperBound = bounds[0];
        // Checking the bounds of T
        assertEquals("org.evosuite.utils.generic.NewGenericClassImplTest$OneGenericBoundParameter<?>",
                upperBound.getTypeName());
        // The upper bound has a parameter as well.
        assertTrue(upperBound instanceof ParameterizedType);
        Type[] actualTypeArguments1 = ((ParameterizedType) upperBound).getActualTypeArguments();
        // Upper bound must have exactly one Parameter (a Wildcard)
        assertEquals(1, actualTypeArguments1.length);
        Type type1 = actualTypeArguments1[0];
        // Check if the parameter for the upper bound is a Wildcard.
        assertTrue(type1 instanceof WildcardType);
    }

    @Test
    public void testTwoGenericUnboundParameters() {
        NewGenericClassImpl genericClass = new NewGenericClassImpl(TwoGenericUnboundParameters.class);
        Type type = genericClass.getType();
        // Check if the returned value is a Parameterized Type
        assertTrue(type instanceof ParameterizedType);
        Type[] actualTypeArguments = ((ParameterizedType) type).getActualTypeArguments();
        // Check if exactly two Parameters are present
        assertEquals(2, actualTypeArguments.length);
        Type typeArgumentA = actualTypeArguments[0];
        Type typeArgumentB = actualTypeArguments[1];
        // The names of the parameters must be A and B
        assertEquals("A", typeArgumentA.getTypeName());
        assertEquals("B", typeArgumentB.getTypeName());
        assertTrue(typeArgumentA instanceof TypeVariable);
        assertTrue(typeArgumentB instanceof TypeVariable);
        Type[] boundsA = ((TypeVariable<?>) typeArgumentA).getBounds();
        Type boundA = boundsA[0];
        Type[] boundsB = ((TypeVariable<?>) typeArgumentB).getBounds();
        Type boundB = boundsB[0];
        // no Bounds
        assertEquals(Object.class.getName(), boundA.getTypeName());
        assertEquals(Object.class.getName(), boundB.getTypeName());
    }

    @Test
    public void testEraseParameterizedTypes() {
        List<Class<?>> classes = Arrays.asList(InnerGenerics.class, OneGenericUnboundParameter.class,
                OneGenericBoundParameter.class);
        classes.forEach(this::eraseTestCase);
    }

    void eraseTestCase(Class<?> cut) {
        NewGenericClassImpl genericClass = new NewGenericClassImpl(cut);
        Type type = genericClass.getType();
        Class<?> expected = TypeUtils.getRawType(type, type);
        Class<?> actual = GenericClassUtils.erase(type);
        assertEquals(expected, actual);
    }

    @Test
    public void testEraseTypeVariable() {
        NewGenericClassImpl genericClass = new NewGenericClassImpl(InnerGenerics.class);
        Type type = genericClass.getType();
        assertTrue(type instanceof ParameterizedType);
        Type actualTypeArgument = ((ParameterizedType) type).getActualTypeArguments()[0];
        Class<?> erase = GenericClassUtils.erase(actualTypeArgument);
        assertEquals(erase, OneGenericBoundParameter.class);
        genericClass = new NewGenericClassImpl(OneGenericUnboundParameter.class);
        type = genericClass.getType();
        assertTrue(type instanceof ParameterizedType);
        actualTypeArgument = ((ParameterizedType) type).getActualTypeArguments()[0];
        erase = GenericClassUtils.erase(actualTypeArgument);
        assertEquals(erase, Object.class);
    }
}