/**
 * 
 */
package org.evosuite.utils;

import java.lang.reflect.Method;
import java.util.List;

import org.evosuite.ga.ConstructionFailedException;
import org.junit.Assert;
import org.junit.Test;

import com.googlecode.gentyref.TypeToken;

/**
 * @author Gordon Fraser
 * 
 */
public class TestGenericAccessibleObject {

	@Test
	public void testGenericMethod() throws SecurityException, NoSuchMethodException {
		Class<?> targetClass = com.examples.with.different.packagename.generic.GenericMethod.class;
		Method targetMethod = targetClass.getMethod("coverMe",
		                                            new Class<?>[] { Object.class });
		GenericMethod genericMethod = new GenericMethod(targetMethod, targetClass);
		Assert.assertFalse(genericMethod.getOwnerClass().hasTypeVariables());

		List<GenericClass> parameters = genericMethod.getParameterClasses();
		Assert.assertFalse(parameters.get(0).hasTypeVariables());
		Assert.assertTrue(parameters.get(0).hasWildcardTypes());

		GenericMethod instantiatedMethod = genericMethod.getGenericInstantiation();
		parameters = instantiatedMethod.getParameterClasses();
		Assert.assertFalse(parameters.get(0).hasTypeVariables());
		Assert.assertFalse(parameters.get(0).hasWildcardTypes());
	}

	@Test
	public void testGenericMethodWithBounds() throws SecurityException,
	        NoSuchMethodException {
		Class<?> targetClass = com.examples.with.different.packagename.generic.GenericMethodWithBounds.class;
		Method targetMethod = targetClass.getMethod("is",
		                                            new Class<?>[] { Comparable.class });
		GenericMethod genericMethod = new GenericMethod(targetMethod, targetClass);
		Assert.assertFalse(genericMethod.getOwnerClass().hasTypeVariables());

		List<GenericClass> parameters = genericMethod.getParameterClasses();
		Assert.assertFalse(parameters.get(0).hasTypeVariables());
		Assert.assertTrue(parameters.get(0).hasWildcardTypes());
		Assert.assertTrue(genericMethod.getGeneratedClass().hasWildcardTypes());

		GenericMethod instantiatedMethod = genericMethod.getGenericInstantiation();
		parameters = instantiatedMethod.getParameterClasses();
		Assert.assertFalse(parameters.get(0).hasTypeVariables());
		Assert.assertFalse(parameters.get(0).hasWildcardTypes());
		Assert.assertFalse(instantiatedMethod.getGeneratedClass().hasWildcardTypes());
	}

	@Test
	public void testGenericMethodAlternativeBounds() throws NoSuchMethodException,
	        RuntimeException, ClassNotFoundException {
		Class<?> targetClass = com.examples.with.different.packagename.generic.GenericMethodAlternativeBounds.class;
		Method targetMethod = targetClass.getMethod("create",
		                                            new Class<?>[] { Class.class });
		GenericMethod genericMethod = new GenericMethod(targetMethod, targetClass);
		Assert.assertFalse(genericMethod.getOwnerClass().hasTypeVariables());

		List<GenericClass> parameters = genericMethod.getParameterClasses();
		Assert.assertFalse(parameters.get(0).hasTypeVariables());
		Assert.assertTrue(parameters.get(0).hasWildcardTypes());
		Assert.assertTrue(genericMethod.getGeneratedClass().hasWildcardTypes());

		// Cannot instantiate because it requires inheritance tree to set up
		// TODO
		// GenericMethod instantiatedMethod = genericMethod.getGenericInstantiation();
		// parameters = instantiatedMethod.getParameterClasses();
		// Assert.assertFalse(parameters.get(0).hasTypeVariables());
		// Assert.assertFalse(parameters.get(0).hasWildcardTypes());
		// Assert.assertFalse(instantiatedMethod.getGeneratedClass().hasWildcardTypes());
	}

	@Test
	public void testGenericClassWithGenericMethodAndSubclass() throws SecurityException,
	        NoSuchMethodException {
		Class<?> targetClass = com.examples.with.different.packagename.generic.GenericClassWithGenericMethodAndSubclass.class;
		Method targetMethod = targetClass.getMethod("wrap",
		                                            new Class<?>[] { Object.class });
		GenericMethod genericMethod = new GenericMethod(targetMethod, targetClass);
		Assert.assertTrue(genericMethod.getOwnerClass().hasTypeVariables());
		System.out.println(genericMethod.toString());
		System.out.println(genericMethod.getOwnerClass().toString());
		System.out.println(genericMethod.getGeneratedClass().toString());

		List<GenericClass> parameters = genericMethod.getParameterClasses();
		Assert.assertFalse(parameters.get(0).hasTypeVariables());
		Assert.assertTrue(parameters.get(0).hasWildcardTypes());
		Assert.assertTrue(genericMethod.getGeneratedClass().hasWildcardTypes());

		GenericMethod instantiatedMethod = genericMethod.getGenericInstantiation();
		parameters = instantiatedMethod.getParameterClasses();
		Assert.assertFalse(parameters.get(0).hasTypeVariables());
		Assert.assertFalse(parameters.get(0).hasWildcardTypes());
		Assert.assertFalse(instantiatedMethod.getGeneratedClass().hasWildcardTypes());
	}

	@Test
	public void testGenericRawParameter() throws SecurityException, NoSuchMethodException {
		Class<?> targetClass = com.examples.with.different.packagename.generic.GenericParameters8.class;
		Method targetMethod = targetClass.getMethod("testMe",
		                                            new Class<?>[] { List.class });
		GenericMethod genericMethod = new GenericMethod(targetMethod, targetClass);
		System.out.println(genericMethod.toString());
		System.out.println(genericMethod.getOwnerClass().toString());
		System.out.println(genericMethod.getGeneratedClass().toString());
		Assert.assertFalse(genericMethod.getOwnerClass().hasTypeVariables());

		List<GenericClass> parameters = genericMethod.getParameterClasses();
		Assert.assertTrue(parameters.get(0).hasTypeVariables());
		Assert.assertFalse(parameters.get(0).hasWildcardTypes());
		Assert.assertFalse(genericMethod.getGeneratedClass().hasWildcardTypes());

		/*
		GenericMethod instantiatedMethod = genericMethod.getGenericInstantiation();
		parameters = instantiatedMethod.getParameterClasses();
		System.out.println(instantiatedMethod.toString());
		System.out.println(instantiatedMethod.getOwnerClass().toString());
		System.out.println(instantiatedMethod.getGeneratedClass().toString());
		System.out.println(parameters.toString());
		Assert.assertFalse(parameters.get(0).hasTypeVariables());
		Assert.assertFalse(parameters.get(0).hasWildcardTypes());
		Assert.assertFalse(instantiatedMethod.getGeneratedClass().hasWildcardTypes());
		*/
	}

	@Test
	public void testLinkedList() throws SecurityException, NoSuchMethodException {
		Class<?> targetClass = java.util.LinkedList.class;
		Method targetMethod = targetClass.getMethod("get", new Class<?>[] { int.class });
		GenericMethod genericMethod = new GenericMethod(targetMethod, targetClass);
		System.out.println(genericMethod.getGeneratedClass().toString());
		Assert.assertTrue(genericMethod.getGeneratedClass().hasWildcardOrTypeVariables());

		GenericMethod instantiatedMethod = genericMethod.getGenericInstantiation();
		System.out.println(instantiatedMethod.getGeneratedClass().toString());
		Assert.assertFalse(instantiatedMethod.getGeneratedClass().hasWildcardOrTypeVariables());
	}

	@Test
	public void testGuavaExample3() throws SecurityException, NoSuchMethodException,
	        ConstructionFailedException {
		Class<?> targetClass = com.examples.with.different.packagename.generic.GuavaExample3.class;

		GenericClass genericInstantiation = new GenericClass(
		        new TypeToken<com.examples.with.different.packagename.generic.GuavaExample3<String, String, Object>>() {
		        }.getType());

		Method targetMethod = targetClass.getMethod("create",
		                                            new Class<?>[] { com.examples.with.different.packagename.generic.GuavaExample3.class });
		GenericMethod genericMethod = new GenericMethod(targetMethod, targetClass);
		System.out.println(genericMethod.getGeneratedClass().toString());
		Assert.assertTrue(genericMethod.getGeneratedClass().hasWildcardOrTypeVariables());

		System.out.println("------------------");
		GenericMethod instantiatedMethod = genericMethod.getGenericInstantiation(genericInstantiation);
		System.out.println(instantiatedMethod.getGeneratedClass().toString());
		Assert.assertFalse(instantiatedMethod.getGeneratedClass().hasWildcardOrTypeVariables());
		Assert.assertEquals(genericInstantiation, instantiatedMethod.getGeneratedClass());
	}
}
