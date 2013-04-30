package org.evosuite.instrumentation;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.evosuite.Properties;
import org.junit.Test;

import com.examples.with.different.packagename.FlagExample1;

public class TestTestabilityTransformation {

	private static final ClassLoader defaultClassloader = TestTestabilityTransformation.class.getClassLoader();
	private static final ClassLoader instrumentingClassloader = new TestabilityTransformationClassLoader();

	// TODO: Not yet working

	@Test
	public void testSimpleFlag() throws ClassNotFoundException, InstantiationException,
	        IllegalAccessException, SecurityException, NoSuchMethodException,
	        IllegalArgumentException, InvocationTargetException {

		Properties.TARGET_CLASS = FlagExample1.class.getCanonicalName();

		Class<?> originalClass = defaultClassloader.loadClass(FlagExample1.class.getCanonicalName());
		Class<?> instrumentedClass = instrumentingClassloader.loadClass(FlagExample1.class.getCanonicalName());

		Object originalInstance = originalClass.newInstance();
		Object instrumentedInstance = instrumentedClass.newInstance();

		Method originalMethod = originalClass.getMethod("testMe",
		                                                new Class<?>[] { int.class });
		Method instrumentedMethod = instrumentedClass.getMethod("testMe",
		                                                        new Class<?>[] { int.class });

		boolean originalResult = (Boolean) originalMethod.invoke(originalInstance, 0);
		boolean instrumentedResult = ((Integer) instrumentedMethod.invoke(instrumentedInstance,
		                                                                  0)) > 0;
		assertEquals(originalResult, instrumentedResult);
	}
}
