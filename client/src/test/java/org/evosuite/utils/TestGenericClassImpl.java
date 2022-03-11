/*
 * Copyright (C) 2010-2018 Gordon Fraser, Andrea Arcuri and EvoSuite
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
package org.evosuite.utils;

import com.examples.with.different.packagename.generic.AbstractGuavaExample;
import com.examples.with.different.packagename.generic.GuavaExample5;
import com.googlecode.gentyref.GenericTypeReflector;
import com.googlecode.gentyref.TypeToken;
import org.apache.commons.lang3.reflect.TypeUtils;
import org.evosuite.ga.ConstructionFailedException;
import org.evosuite.instrumentation.InstrumentingClassLoader;
import org.evosuite.utils.generic.GenericClass;
import org.evosuite.utils.generic.GenericClassFactory;
import org.evosuite.utils.generic.WildcardTypeImpl;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;

import static org.junit.Assert.assertEquals;

public class TestGenericClassImpl {

    @Test
    public void testWildcardClassloader() {
        GenericClass<?> clazz = GenericClassFactory.get(Class.class).getWithWildcardTypes();
        assertEquals("java.lang.Class<?>", clazz.getTypeName());
        clazz.changeClassLoader(TestGenericClassImpl.class.getClassLoader());
        assertEquals("java.lang.Class<?>", clazz.getTypeName());
    }

    @Test
    public void testAssignablePrimitives() {
        GenericClass<?> clazz1 = GenericClassFactory.get(int.class);
        GenericClass<?> clazz2 = GenericClassFactory.get(int.class);
        Assert.assertTrue(clazz1.isAssignableTo(clazz2));
        Assert.assertTrue(clazz1.isAssignableFrom(clazz2));
    }

    @Test
    public void testAssignableObject() {
        GenericClass<?> clazz1 = GenericClassFactory.get(Object.class);
        GenericClass<?> clazz2 = GenericClassFactory.get(Object.class);
        Assert.assertTrue(clazz1.isAssignableTo(clazz2));
    }

    @Test
    public void testAssignableIntegerObject() {
        GenericClass<?> clazz1 = GenericClassFactory.get(Integer.class);
        GenericClass<?> clazz2 = GenericClassFactory.get(Object.class);
        Assert.assertTrue(clazz1.isAssignableTo(clazz2));
        Assert.assertFalse(clazz1.isAssignableFrom(clazz2));
    }

    @Test
    public void testAssignableIntegerNumber() {
        GenericClass<?> clazz1 = GenericClassFactory.get(Integer.class);
        GenericClass<?> clazz2 = GenericClassFactory.get(Number.class);
        Assert.assertTrue(clazz1.isAssignableTo(clazz2));
        Assert.assertFalse(clazz1.isAssignableFrom(clazz2));
    }

    @Test
    public void testAssignableIntInteger() {
        GenericClass<?> clazz1 = GenericClassFactory.get(Integer.class);
        GenericClass<?> clazz2 = GenericClassFactory.get(int.class);
        Assert.assertTrue(clazz1.isAssignableTo(clazz2));
        Assert.assertTrue(clazz1.isAssignableFrom(clazz2));
    }

    @Test
    public void testAssignableClass() {
        GenericClass<?> clazzTypeVar = GenericClassFactory.get(Class.class);
        GenericClass<?> clazzWildcard = clazzTypeVar.getWithWildcardTypes();

        ParameterizedType type = new ParameterizedTypeImpl(Class.class,
                new Type[]{Integer.class}, null);
        GenericClass<?> clazzConcrete = GenericClassFactory.get(type);

        Assert.assertFalse(clazzWildcard.isAssignableTo(clazzConcrete));
        Assert.assertFalse(clazzWildcard.isAssignableTo(clazzTypeVar));
        Assert.assertTrue(clazzWildcard.isAssignableTo(clazzWildcard));

        Assert.assertFalse(clazzTypeVar.isAssignableTo(clazzConcrete));
        Assert.assertTrue(clazzTypeVar.isAssignableTo(clazzTypeVar));
        Assert.assertTrue(clazzTypeVar.isAssignableTo(clazzWildcard));

        Assert.assertTrue(clazzConcrete.isAssignableTo(clazzConcrete));
        Assert.assertFalse(clazzConcrete.isAssignableTo(clazzTypeVar));
        Assert.assertTrue(clazzConcrete.isAssignableTo(clazzWildcard));
    }

    private static class A {
    }

    @SuppressWarnings({"unused"})
    @Test
    public void test01() throws Throwable {

        /*
         * This test case come from compilation issue found during SBST'13 competition:
         *
         * String string0 = vM0.run(vM0.stack);
         *
         * SUT at:  http://www.massapi.com/source/jabref-2.6/src/java/net/sf/jabref/bst/VM.java.html
         *
         * Snippet of interest:
         *
         * 1) Stack<Object> stack = new Stack<Object>();
         * 2)  public String run(Collection<BibtexEntry> bibtex) {
         */

        Collection<?> col0 = new Stack<>();
        Collection<A> col1 = new Stack();
        Collection col2 = new Stack();
        Collection col3 = new Stack<>();

        /*
         *  following does not compile
         *
         *  Collection<A> col = new Stack<Object>();
         *
         *  but it can be generated by EvoSuite
         */

        GenericClass<?> stack = GenericClassFactory.get(Stack.class).getWithWildcardTypes();
        GenericClass<?> collection = GenericClassFactory.get(Collection.class).getWithWildcardTypes();
        Assert.assertTrue(stack.isAssignableTo(collection));

        GenericClass<?> objectStack = GenericClassFactory.get(col0.getClass());
        Assert.assertTrue(objectStack.isAssignableTo(collection));

        Type typeColA = new TypeToken<Collection<A>>() {
        }.getType();
        Type typeStack = new TypeToken<Stack>() {
        }.getType();
        Type typeObjectStack = new TypeToken<Stack<Object>>() {
        }.getType();

        GenericClass<?> classColA = GenericClassFactory.get(typeColA);
        GenericClass<?> classStack = GenericClassFactory.get(typeStack).getWithWildcardTypes();
        GenericClass<?> classObjectStack = GenericClassFactory.get(typeObjectStack);

        Assert.assertFalse(classStack.isAssignableTo(classColA));
        Assert.assertFalse(classObjectStack.isAssignableTo(classColA));
        Assert.assertFalse(classColA.isAssignableFrom(classObjectStack));
    }

    @Test
    public void test1() {
        Type listOfString = new TypeToken<List<String>>() {
        }.getType();
        Type listOfInteger = new TypeToken<List<Integer>>() {
        }.getType();

        GenericClass<?> listOfStringClass = GenericClassFactory.get(listOfString);
        GenericClass<?> listOfIntegerClass = GenericClassFactory.get(listOfInteger);

        Assert.assertFalse(listOfStringClass.isAssignableFrom(listOfIntegerClass));
        Assert.assertFalse(listOfStringClass.isAssignableTo(listOfIntegerClass));
    }


    @Test
    public void test2() {
        Type listOfString = new TypeToken<List<String>>() {
        }.getType();
        Type plainList = new TypeToken<List>() {
        }.getType();
        Type objectList = new TypeToken<List<Object>>() {
        }.getType();

        GenericClass<?> listOfStringClass = GenericClassFactory.get(listOfString);
        GenericClass<?> plainListClass = GenericClassFactory.get(plainList).getWithWildcardTypes();
        GenericClass<?> objectListClass = GenericClassFactory.get(objectList);

        /*
         * Note:
         *
         * 		List<String> l = new LinkedList<Object>();
         *
         *  does not compile
         */

        Assert.assertFalse(listOfStringClass.isAssignableTo(objectListClass));

        Assert.assertFalse(listOfStringClass.isAssignableFrom(plainListClass));
        Assert.assertTrue(listOfStringClass.isAssignableTo(plainListClass));
    }

    @Test
    public void test3() {
        Type listOfInteger = new TypeToken<List<Integer>>() {
        }.getType();
        Type listOfSerializable = new TypeToken<List<Serializable>>() {
        }.getType();

        GenericClass<?> listOfIntegerClass = GenericClassFactory.get(listOfInteger);
        GenericClass<?> listOfSerializableClass = GenericClassFactory.get(listOfSerializable);

        Assert.assertFalse(listOfIntegerClass.isAssignableFrom(listOfSerializableClass));
        Assert.assertFalse(listOfSerializableClass.isAssignableFrom(listOfIntegerClass));

        Assert.assertTrue(listOfIntegerClass.isAssignableFrom(listOfIntegerClass));
        Assert.assertTrue(listOfSerializableClass.isAssignableFrom(listOfSerializableClass));
    }

    private class NumberBoundary<T extends Number> {
    }

    private class ComparableBoundary<T extends Comparable<T>> {
    }

    private class RefinedComparableBoundary<T extends java.util.Date> extends
            ComparableBoundary<java.util.Date> {
    }

    @Test
    public void testTypeVariableBoundariesNumber() {
        TypeVariable<?> numberTypeVariable = NumberBoundary.class.getTypeParameters()[0];

        GenericClass<?> listOfIntegerClass = GenericClassFactory.get(Integer.class);
        GenericClass<?> listOfSerializableClass = GenericClassFactory.get(Serializable.class);

        Assert.assertTrue(listOfIntegerClass.satisfiesBoundaries(numberTypeVariable));
        Assert.assertFalse(listOfSerializableClass.satisfiesBoundaries(numberTypeVariable));
    }

    @Test
    public void testTypeVariableBoundariesComparable() {
        TypeVariable<?> comparableTypeVariable = ComparableBoundary.class.getTypeParameters()[0];

        GenericClass<?> listOfIntegerClass = GenericClassFactory.get(Integer.class);
        GenericClass<?> listOfSerializableClass = GenericClassFactory.get(Serializable.class);

        Assert.assertTrue(listOfIntegerClass.satisfiesBoundaries(comparableTypeVariable));
        Assert.assertFalse(listOfSerializableClass.satisfiesBoundaries(comparableTypeVariable));
    }

    @Test
    public void testGuavaExample() {
        Type abstractGuavaExampleString = new TypeToken<AbstractGuavaExample<String>>() {
        }.getType();
        Type guavaExample5 = new TypeToken<GuavaExample5<String>>() {
        }.getType();

        GenericClass<?> abstractClass = GenericClassFactory.get(abstractGuavaExampleString);
        GenericClass<?> concreteClass = GenericClassFactory.get(guavaExample5);

        Assert.assertTrue(TypeUtils.isAssignable(concreteClass.getType(), abstractClass.getType()));
        Assert.assertTrue("Cannot assign " + concreteClass + " to " + abstractClass, abstractClass.isAssignableFrom(concreteClass));
        Assert.assertTrue(concreteClass.isAssignableTo(abstractClass));
    }

    @Test
    public void testTypeVariableBoundariesRefined() {
        TypeVariable<?> dateTypeVariable = RefinedComparableBoundary.class.getTypeParameters()[0];
        TypeVariable<?> comparableTypeVariable = ComparableBoundary.class.getTypeParameters()[0];

        GenericClass<?> listOfIntegerClass = GenericClassFactory.get(Integer.class);
        GenericClass<?> listOfComparableClass = GenericClassFactory.get(Comparable.class);
        GenericClass<?> listOfDateClass = GenericClassFactory.get(java.util.Date.class);
        GenericClass<?> listOfSqlDateClass = GenericClassFactory.get(java.sql.Date.class);

        Assert.assertFalse(listOfIntegerClass.satisfiesBoundaries(dateTypeVariable));
        Assert.assertFalse(listOfComparableClass.satisfiesBoundaries(dateTypeVariable));
        Assert.assertTrue(listOfDateClass.satisfiesBoundaries(dateTypeVariable));
        Assert.assertTrue(listOfSqlDateClass.satisfiesBoundaries(dateTypeVariable));

        Assert.assertTrue(listOfIntegerClass.satisfiesBoundaries(comparableTypeVariable));
        //		Assert.assertTrue(listOfComparableClass.satisfiesBoundaries(comparableTypeVariable));
        Assert.assertTrue(listOfDateClass.satisfiesBoundaries(comparableTypeVariable));
        // Assert.assertTrue(listOfSqlDateClass.satisfiesBoundaries(comparableTypeVariable));
    }

    @Test
    public void testWildcardObjectBoundaries() {

        WildcardType objectType = new WildcardTypeImpl(new Type[]{Object.class},
                new Type[]{});

        GenericClass<?> integerClass = GenericClassFactory.get(Integer.class);
        GenericClass<?> comparableClass = GenericClassFactory.get(Comparable.class);
        GenericClass<?> dateClass = GenericClassFactory.get(java.util.Date.class);
        GenericClass<?> sqlDateClass = GenericClassFactory.get(java.sql.Date.class);

        Assert.assertTrue(integerClass.satisfiesBoundaries(objectType));
        Assert.assertTrue(comparableClass.satisfiesBoundaries(objectType));
        Assert.assertTrue(dateClass.satisfiesBoundaries(objectType));
        Assert.assertTrue(sqlDateClass.satisfiesBoundaries(objectType));
    }

    @Test
    public void testWildcardNumberBoundaries() {

        WildcardType objectType = new WildcardTypeImpl(new Type[]{Number.class},
                new Type[]{});

        GenericClass<?> integerClass = GenericClassFactory.get(Integer.class);
        GenericClass<?> comparableClass = GenericClassFactory.get(Comparable.class);
        GenericClass<?> dateClass = GenericClassFactory.get(java.util.Date.class);
        GenericClass<?> sqlDateClass = GenericClassFactory.get(java.sql.Date.class);

        Assert.assertTrue(integerClass.satisfiesBoundaries(objectType));
        Assert.assertFalse(comparableClass.satisfiesBoundaries(objectType));
        Assert.assertFalse(dateClass.satisfiesBoundaries(objectType));
        Assert.assertFalse(sqlDateClass.satisfiesBoundaries(objectType));
    }

    @Test
    public void testWildcardIntegerBoundaries() {

        WildcardType objectType = new WildcardTypeImpl(new Type[]{Integer.class},
                new Type[]{});

        GenericClass<?> integerClass = GenericClassFactory.get(Integer.class);
        GenericClass<?> comparableClass = GenericClassFactory.get(Comparable.class);
        GenericClass<?> dateClass = GenericClassFactory.get(java.util.Date.class);
        GenericClass<?> sqlDateClass = GenericClassFactory.get(java.sql.Date.class);

        Assert.assertTrue(integerClass.satisfiesBoundaries(objectType));
        Assert.assertFalse(comparableClass.satisfiesBoundaries(objectType));
        Assert.assertFalse(dateClass.satisfiesBoundaries(objectType));
        Assert.assertFalse(sqlDateClass.satisfiesBoundaries(objectType));
    }

    @Test
    public void testWildcardComparableBoundaries() {

        WildcardType objectType = new WildcardTypeImpl(new Type[]{Comparable.class},
                new Type[]{});

        GenericClass<?> integerClass = GenericClassFactory.get(Integer.class);
        GenericClass<?> comparableClass = GenericClassFactory.get(Comparable.class);
        GenericClass<?> dateClass = GenericClassFactory.get(java.util.Date.class);
        GenericClass<?> sqlDateClass = GenericClassFactory.get(java.sql.Date.class);

        Assert.assertTrue(integerClass.satisfiesBoundaries(objectType));
        Assert.assertTrue(comparableClass.satisfiesBoundaries(objectType));
        Assert.assertTrue(dateClass.satisfiesBoundaries(objectType));
        Assert.assertTrue(sqlDateClass.satisfiesBoundaries(objectType));
    }

    @Test
    public void testWildcardDateBoundaries() {

        WildcardType objectType = new WildcardTypeImpl(
                new Type[]{java.util.Date.class}, new Type[]{});

        GenericClass<?> integerClass = GenericClassFactory.get(Integer.class);
        GenericClass<?> comparableClass = GenericClassFactory.get(Comparable.class);
        GenericClass<?> dateClass = GenericClassFactory.get(java.util.Date.class);
        GenericClass<?> sqlDateClass = GenericClassFactory.get(java.sql.Date.class);

        Assert.assertFalse(integerClass.satisfiesBoundaries(objectType));
        Assert.assertFalse(comparableClass.satisfiesBoundaries(objectType));
        Assert.assertTrue(dateClass.satisfiesBoundaries(objectType));
        Assert.assertTrue(sqlDateClass.satisfiesBoundaries(objectType));
    }

    @Test
    public void testWildcardSqlDateBoundaries() {

        WildcardType objectType = new WildcardTypeImpl(
                new Type[]{java.sql.Date.class}, new Type[]{});

        GenericClass<?> integerClass = GenericClassFactory.get(Integer.class);
        GenericClass<?> comparableClass = GenericClassFactory.get(Comparable.class);
        GenericClass<?> dateClass = GenericClassFactory.get(java.util.Date.class);
        GenericClass<?> sqlDateClass = GenericClassFactory.get(java.sql.Date.class);

        Assert.assertFalse(integerClass.satisfiesBoundaries(objectType));
        Assert.assertFalse(comparableClass.satisfiesBoundaries(objectType));
        Assert.assertFalse(dateClass.satisfiesBoundaries(objectType));
        Assert.assertTrue(sqlDateClass.satisfiesBoundaries(objectType));
    }

    @Test
    public void testWildcardDateSuperBoundaries() {

        WildcardType objectType = new WildcardTypeImpl(new Type[]{Object.class},
                new Type[]{java.util.Date.class});

        GenericClass<?> integerClass = GenericClassFactory.get(Integer.class);
        GenericClass<?> comparableClass = GenericClassFactory.get(Comparable.class);
        GenericClass<?> dateClass = GenericClassFactory.get(java.util.Date.class);
        GenericClass<?> sqlDateClass = GenericClassFactory.get(java.sql.Date.class);

        Assert.assertFalse(integerClass.satisfiesBoundaries(objectType));
        Assert.assertFalse(comparableClass.satisfiesBoundaries(objectType));
        Assert.assertTrue(dateClass.satisfiesBoundaries(objectType));
        Assert.assertFalse(sqlDateClass.satisfiesBoundaries(objectType));
    }

    @Test
    public void testWildcardDateBothBoundaries() {

        WildcardType objectType = new WildcardTypeImpl(
                new Type[]{java.util.Date.class}, new Type[]{java.util.Date.class});

        GenericClass<?> integerClass = GenericClassFactory.get(Integer.class);
        GenericClass<?> comparableClass = GenericClassFactory.get(Comparable.class);
        GenericClass<?> dateClass = GenericClassFactory.get(java.util.Date.class);
        GenericClass<?> sqlDateClass = GenericClassFactory.get(java.sql.Date.class);

        Assert.assertFalse(integerClass.satisfiesBoundaries(objectType));
        Assert.assertFalse(comparableClass.satisfiesBoundaries(objectType));
        Assert.assertTrue(dateClass.satisfiesBoundaries(objectType));
        // Does not satisfy lower bound, so needs to be false
        Assert.assertFalse(sqlDateClass.satisfiesBoundaries(objectType));
    }

    @Test
    public void testWildcardDateBothBoundaries2() {

        WildcardType objectType = new WildcardTypeImpl(new Type[]{Comparable.class},
                new Type[]{java.util.Date.class});

        GenericClass<?> integerClass = GenericClassFactory.get(Integer.class);
        GenericClass<?> comparableClass = GenericClassFactory.get(Comparable.class);
        GenericClass<?> dateClass = GenericClassFactory.get(java.util.Date.class);
        GenericClass<?> sqlDateClass = GenericClassFactory.get(java.sql.Date.class);

        Assert.assertFalse(integerClass.satisfiesBoundaries(objectType));
        Assert.assertFalse(comparableClass.satisfiesBoundaries(objectType));
        Assert.assertTrue(dateClass.satisfiesBoundaries(objectType));
        // Does not satisfy lower boundary
        Assert.assertFalse(sqlDateClass.satisfiesBoundaries(objectType));
    }

    @Test
    public void testWildcardInvalidBoundaries() {

        WildcardType objectType = new WildcardTypeImpl(new Type[]{Number.class},
                new Type[]{java.util.Date.class});

        GenericClass<?> integerClass = GenericClassFactory.get(Integer.class);
        GenericClass<?> comparableClass = GenericClassFactory.get(Comparable.class);
        GenericClass<?> dateClass = GenericClassFactory.get(java.util.Date.class);
        GenericClass<?> sqlDateClass = GenericClassFactory.get(java.sql.Date.class);

        Assert.assertFalse(integerClass.satisfiesBoundaries(objectType));
        Assert.assertFalse(comparableClass.satisfiesBoundaries(objectType));
        Assert.assertFalse(dateClass.satisfiesBoundaries(objectType));
        Assert.assertFalse(sqlDateClass.satisfiesBoundaries(objectType));
    }

    @Test
    public void testGenericSuperclassWildcards() {
        GenericClass<?> listOfInteger = GenericClassFactory.get(new TypeToken<List<Integer>>() {
        }.getType());
        GenericClass<?> listOfWildcard = GenericClassFactory.get(new TypeToken<List<?>>() {
        }.getType());

        Assert.assertTrue(listOfWildcard.isGenericSuperTypeOf(listOfInteger));
        Assert.assertFalse(listOfInteger.isGenericSuperTypeOf(listOfWildcard));
        Assert.assertTrue(listOfInteger.hasGenericSuperType(listOfWildcard));
        Assert.assertFalse(listOfWildcard.hasGenericSuperType(listOfInteger));

        GenericClass<?> mapOfInteger = GenericClassFactory.get(
                new TypeToken<Map<Integer, String>>() {
                }.getType());
        GenericClass<?> mapOfWildcard = GenericClassFactory.get(new TypeToken<Map<?, ?>>() {
        }.getType());
        Assert.assertTrue(mapOfWildcard.isGenericSuperTypeOf(mapOfInteger));
        Assert.assertFalse(mapOfInteger.isGenericSuperTypeOf(mapOfWildcard));
        Assert.assertTrue(mapOfInteger.hasGenericSuperType(mapOfWildcard));
        Assert.assertFalse(mapOfWildcard.hasGenericSuperType(mapOfInteger));
    }

    @Test
    public void testGenericSuperclassConcreteList() {
        GenericClass<?> listOfInteger = GenericClassFactory.get(new TypeToken<List<Integer>>() {
        }.getType());
        GenericClass<?> linkedlistOfInteger = GenericClassFactory.get(
                new TypeToken<LinkedList<Integer>>() {
                }.getType());

        Assert.assertTrue(linkedlistOfInteger.canBeInstantiatedTo(listOfInteger));
        Assert.assertFalse(listOfInteger.canBeInstantiatedTo(linkedlistOfInteger));
    }

    @Test
    public void testGenericSuperclassToWildcardList() {
        GenericClass<?> listOfWildcard = GenericClassFactory.get(new TypeToken<List<Integer>>() {
        }.getType()).getWithWildcardTypes();
        GenericClass<?> linkedlistOfInteger = GenericClassFactory.get(
                new TypeToken<LinkedList<Integer>>() {
                }.getType());

        Assert.assertTrue(linkedlistOfInteger.canBeInstantiatedTo(listOfWildcard));
        Assert.assertFalse(listOfWildcard.canBeInstantiatedTo(linkedlistOfInteger));
    }

    @Test
    public void testGenericSuperclassFromWildcardList() {
        GenericClass<?> listOfInteger = GenericClassFactory.get(new TypeToken<List<Integer>>() {
        }.getType());
        GenericClass<?> linkedlistOfWildcard = GenericClassFactory.get(
                new TypeToken<LinkedList<Integer>>() {
                }.getType()).getWithWildcardTypes();

        Assert.assertTrue(linkedlistOfWildcard.canBeInstantiatedTo(listOfInteger));
        Assert.assertFalse(listOfInteger.canBeInstantiatedTo(linkedlistOfWildcard));
    }


    @Test
    public void testGenericSuperclassToTypeVariableList() {
        GenericClass<?> listOfTypeVariable = GenericClassFactory.get(new TypeToken<List>() {
        }.getType());
        GenericClass<?> linkedlistOfInteger = GenericClassFactory.get(
                new TypeToken<LinkedList<Integer>>() {
                }.getType());

        Assert.assertTrue(linkedlistOfInteger.canBeInstantiatedTo(listOfTypeVariable));
        Assert.assertFalse(listOfTypeVariable.canBeInstantiatedTo(linkedlistOfInteger));
    }


    @Test
    public void testGenericSuperclassFromTypeVariableList() {
        GenericClass<?> listOfInteger = GenericClassFactory.get(new TypeToken<List<Integer>>() {
        }.getType());
        GenericClass<?> linkedlistOfTypeVariable = GenericClassFactory.get(
                new TypeToken<LinkedList>() {
                }.getType());

        Assert.assertTrue(linkedlistOfTypeVariable.canBeInstantiatedTo(listOfInteger));
        Assert.assertFalse(listOfInteger.canBeInstantiatedTo(linkedlistOfTypeVariable));
    }

    @Test
    public void testPrimitiveWrapper() {
        GenericClass<?> integerClass = GenericClassFactory.get(Integer.class);
        GenericClass<?> intClass = GenericClassFactory.get(int.class);

        Assert.assertTrue(integerClass.canBeInstantiatedTo(intClass));
        Assert.assertFalse(intClass.canBeInstantiatedTo(integerClass));
    }


    @Test
    public void testGenericInstantiationIntegerList() throws ConstructionFailedException {
        GenericClass<?> listOfInteger = GenericClassFactory.get(new TypeToken<List<Integer>>() {
        }.getType());
        GenericClass<?> linkedlistOfTypeVariable = GenericClassFactory.get(
                new TypeToken<LinkedList>() {
                }.getType());

        GenericClass<?> instantiatedClass = linkedlistOfTypeVariable.getWithParametersFromSuperclass(listOfInteger);
        //GenericClass instantiatedClass = linkedlistOfTypeVariable.getGenericInstantiation(listOfInteger.getTypeVariableMap());
        Assert.assertEquals(Integer.class, instantiatedClass.getParameterTypes().get(0));
    }


    @Test
    public void testGenericInstantiationMapSubclass() throws ConstructionFailedException {
        GenericClass<?> mapOfStringAndWildcard = GenericClassFactory.get(
                new TypeToken<Map<String, ?>>() {
                }.getType());
        GenericClass<?> hashMapClass = GenericClassFactory.get(new TypeToken<HashMap>() {
        }.getType());

        GenericClass<?> instantiatedClass = hashMapClass.getWithParametersFromSuperclass(mapOfStringAndWildcard);
        //GenericClass instantiatedClass = linkedlistOfTypeVariable.getGenericInstantiation(listOfInteger.getTypeVariableMap());
        System.out.println(instantiatedClass.toString());
        Assert.assertEquals(String.class, instantiatedClass.getParameterTypes().get(0));
    }

    @Ignore
    @Test
    public void testGenericInstantiationMapType() throws ConstructionFailedException {
        GenericClass<?> genericClass = GenericClassFactory.get(
                com.examples.with.different.packagename.generic.GenericParameterExtendingGenericBounds.class);
        GenericClass<?> instantiatedClass = genericClass.getGenericInstantiation();
        //GenericClass instantiatedClass = linkedlistOfTypeVariable.getGenericInstantiation(listOfInteger.getTypeVariableMap());
        System.out.println(instantiatedClass.toString());
        Type parameterType = instantiatedClass.getParameterTypes().get(0);
        Assert.assertTrue(TypeUtils.isAssignable(parameterType, Map.class));
        Assert.assertTrue(parameterType instanceof ParameterizedType);

        ParameterizedType parameterizedType = (ParameterizedType) parameterType;
        Assert.assertEquals(String.class, parameterizedType.getActualTypeArguments()[0]);
    }

    @Test
    public void testIterableAndList() throws ConstructionFailedException {
        GenericClass<?> iterableIntegerClass = GenericClassFactory.get(
                new TypeToken<java.lang.Iterable<Integer>>() {
                }.getType());
        GenericClass<?> arrayListClass = GenericClassFactory.get(java.util.ArrayList.class);

        Assert.assertTrue(arrayListClass.canBeInstantiatedTo(iterableIntegerClass));
        Assert.assertFalse(iterableIntegerClass.canBeInstantiatedTo(arrayListClass));

        GenericClass<?> instantiatedList = arrayListClass.getWithParametersFromSuperclass(iterableIntegerClass);

        Type parameterType = instantiatedList.getParameterTypes().get(0);
        Assert.assertEquals(Integer.class, GenericTypeReflector.erase(parameterType));
    }


    @Test
    public void testIterableAndListBoundaries() {
        Map<TypeVariable<?>, Type> typeMap = new HashMap<>();
        final GenericClass<?> iterableIntegerClass = GenericClassFactory.get(
                new TypeToken<java.lang.Iterable<Integer>>() {
                }.getType());

        TypeVariable<?> var = new TypeVariable() {

            public AnnotatedType[] getAnnotatedBounds() {
                return null;
            }

            @Override
            public Type[] getBounds() {
                return new Type[]{iterableIntegerClass.getType()};
            }

            @Override
            public GenericDeclaration getGenericDeclaration() {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public String getName() {
                return "Test";
            }

            /* (non-Javadoc)
             * @see java.lang.Object#toString()
             */
            @Override
            public String toString() {
                return "Dummy Variable";
            }

            public <T extends Annotation> T getAnnotation(
                    Class<T> annotationClass) {
                // TODO Auto-generated method stub
                return null;
            }

            public Annotation[] getAnnotations() {
                // TODO Auto-generated method stub
                return null;
            }

            public Annotation[] getDeclaredAnnotations() {
                // TODO Auto-generated method stub
                return null;
            }

            //public void getAnnotatedBounds() {
            // TODO Auto-generated method stub
            //}

        };

        typeMap.put(var, iterableIntegerClass.getType());

        GenericClass<?> arrayListClass = GenericClassFactory.get(
                new TypeToken<java.util.ArrayList<String>>() {
                }.getType());

        Assert.assertFalse(arrayListClass.satisfiesBoundaries(var, typeMap));

        arrayListClass = GenericClassFactory.get(
                new TypeToken<java.util.ArrayList<Integer>>() {
                }.getType());

        Assert.assertTrue(arrayListClass.satisfiesBoundaries(var, typeMap));

    }

    @Test
    public void testSatisfiesTypeVariableInSubtype() {
        GenericClass<?> iterableIntegerClass = GenericClassFactory.get(
                new TypeToken<com.examples.with.different.packagename.generic.GuavaExample4<java.lang.Iterable<Integer>>>() {
                }.getType());
        ParameterizedType iterableInteger = (ParameterizedType) iterableIntegerClass.getParameterTypes().get(0);
        TypeVariable<?> typeVariable = ((Class<?>) iterableInteger.getRawType()).getTypeParameters()[0];

        TypeVariable<?> iterableTypeVariable = iterableIntegerClass.getTypeVariables().get(0);

        GenericClass<?> listOfIntegerClass = GenericClassFactory.get(
                com.examples.with.different.packagename.generic.GuavaExample4.class);

        // Object bound
        Assert.assertTrue(iterableIntegerClass.satisfiesBoundaries(typeVariable));
        Assert.assertTrue(listOfIntegerClass.satisfiesBoundaries(typeVariable));

        // Iterable bound
        Assert.assertTrue(iterableIntegerClass.satisfiesBoundaries(iterableTypeVariable));
        Assert.assertTrue(listOfIntegerClass.satisfiesBoundaries(iterableTypeVariable));

    }

    @Test
    public void reloadArrayClass() {
        GenericClass<?> arrayClass = GenericClassFactory.get(Object[].class);
        ClassLoader loader = new InstrumentingClassLoader();
        arrayClass.changeClassLoader(loader);
        Class<?> rawClass = arrayClass.getRawClass();
        Assert.assertTrue(rawClass.isArray());

    }

    @Test
    public void reloadNonArrayClass() {
        GenericClass<?> arrayClass = GenericClassFactory.get(Integer.class);
        ClassLoader loader = new InstrumentingClassLoader();
        arrayClass.changeClassLoader(loader);
        Class<?> rawClass = arrayClass.getRawClass();
        Assert.assertFalse(rawClass.isArray());
    }

    @Test
    public void testWildcardInstantiation() throws ConstructionFailedException {

        GenericClass<?> integerWildcardListClass = GenericClassFactory.get(
                new TypeToken<java.util.List<? extends Integer>>() {
                }.getType());

        GenericClass<?> integerListClass = GenericClassFactory.get(
                new TypeToken<java.util.List<Integer>>() {
                }.getType());
        GenericClass<?> objectListClass = GenericClassFactory.get(
                new TypeToken<java.util.List<Object>>() {
                }.getType());

        Assert.assertTrue(integerWildcardListClass.isAssignableFrom(integerListClass));
        Assert.assertFalse(integerWildcardListClass.isAssignableFrom(objectListClass));

        GenericClass<?> integerWildcardListInstantiation = integerWildcardListClass.getGenericInstantiation();
        Assert.assertTrue(integerWildcardListClass.isAssignableFrom(integerWildcardListInstantiation));
    }

    @Test
    public void testWildcardWithSuperIntegerBoundaryInstantiation() throws ConstructionFailedException {

        GenericClass<?> integerWildcardListClass = GenericClassFactory.get(
                new TypeToken<java.util.List<? super Integer>>() {
                }.getType());

        GenericClass<?> integerListClass = GenericClassFactory.get(
                new TypeToken<java.util.List<Integer>>() {
                }.getType());
        GenericClass<?> numberListClass = GenericClassFactory.get(
                new TypeToken<java.util.List<Number>>() {
                }.getType());
        GenericClass<?> objectListClass = GenericClassFactory.get(
                new TypeToken<java.util.List<Object>>() {
                }.getType());

        Assert.assertTrue(integerWildcardListClass.isAssignableFrom(integerListClass));
        Assert.assertTrue(integerWildcardListClass.isAssignableFrom(numberListClass));
        Assert.assertTrue(integerWildcardListClass.isAssignableFrom(objectListClass));

        GenericClass<?> integerWildcardListInstantiation = integerWildcardListClass.getGenericInstantiation();
        Assert.assertTrue(integerWildcardListClass.isAssignableFrom(integerWildcardListInstantiation));
    }

    @Test
    public void testWildcardWithSuperNumberBoundaryInstantiation() throws ConstructionFailedException {

        GenericClass<?> numberWildcardListClass = GenericClassFactory.get(
                new TypeToken<java.util.List<? super Number>>() {
                }.getType());

        GenericClass<?> integerListClass = GenericClassFactory.get(
                new TypeToken<java.util.List<Integer>>() {
                }.getType());
        GenericClass<?> numberListClass = GenericClassFactory.get(
                new TypeToken<java.util.List<Number>>() {
                }.getType());
        GenericClass<?> objectListClass = GenericClassFactory.get(
                new TypeToken<java.util.List<Object>>() {
                }.getType());

        Assert.assertFalse(numberWildcardListClass.isAssignableFrom(integerListClass));
        Assert.assertTrue(numberWildcardListClass.isAssignableFrom(numberListClass));
        Assert.assertTrue(numberWildcardListClass.isAssignableFrom(objectListClass));

        GenericClass<?> integerWildcardListInstantiation = numberWildcardListClass.getGenericInstantiation();
        System.out.println(integerWildcardListInstantiation.toString());
        Assert.assertTrue(numberWildcardListClass.isAssignableFrom(integerWildcardListInstantiation));
    }
}
