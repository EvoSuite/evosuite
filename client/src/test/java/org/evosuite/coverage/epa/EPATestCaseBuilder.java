package org.evosuite.coverage.epa;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.evosuite.testcase.DefaultTestCase;
import org.evosuite.testcase.statements.ConstructorStatement;
import org.evosuite.testcase.statements.MethodStatement;
import org.evosuite.testcase.variable.VariableReference;
import org.evosuite.utils.generic.GenericClass;
import org.evosuite.utils.generic.GenericConstructor;
import org.evosuite.utils.generic.GenericMethod;

public class EPATestCaseBuilder {

	private Map<Class<?>, GenericClass> classes = new HashMap<Class<?>, GenericClass>();

	private final DefaultTestCase tc = new DefaultTestCase();

	public VariableReference addConstructorStatement(Class<?> clazz) throws NoSuchMethodException, SecurityException {
		GenericClass genericClass = getGenericClass(clazz);
		GenericConstructor arrayListGenericConstructor = new GenericConstructor(
				genericClass.getRawClass().getConstructor(), genericClass);
		ConstructorStatement stmt0 = new ConstructorStatement(tc, arrayListGenericConstructor,
				Collections.<VariableReference> emptyList());
		VariableReference ret_val = tc.addStatement(stmt0);
		return ret_val;
	}

	private GenericClass getGenericClass(Class<?> clazz) {
		if (!classes.containsKey(clazz)) {
			GenericClass genericClass = new GenericClass(clazz);
			classes.put(clazz, genericClass);
			return genericClass;
		} else {
			return classes.get(clazz);
		}
	}

	/**
	 * Creates a call to a non-static method
	 * 
	 * @param receiver
	 * @param method
	 * @param parameters
	 * @return
	 */
	public VariableReference addMethodStatement(VariableReference receiver, Method method,
			VariableReference... parameters) {
		Class<?> declaringClass = method.getDeclaringClass();
		GenericClass genericClass = getGenericClass(declaringClass);
		GenericMethod genericMethod = new GenericMethod(method, genericClass);
		MethodStatement stmt = new MethodStatement(tc, genericMethod, receiver, Arrays.asList(parameters));
		VariableReference ret_val = tc.addStatement(stmt);
		return ret_val;
	}

	/**
	 * Creates a call to a static method
	 * 
	 * @param static_method
	 * @param parameters
	 * @return
	 */
	public VariableReference addMethodStatement(Method static_method, VariableReference... parameters) {
		Class<?> declaringClass = static_method.getDeclaringClass();
		GenericClass genericClass = getGenericClass(declaringClass);
		GenericMethod genericMethod = new GenericMethod(static_method, genericClass);
		if (!genericMethod.isStatic()) {
			throw new IllegalArgumentException("Method should be static!");
		}
		MethodStatement stmt = new MethodStatement(tc, genericMethod, null, Arrays.asList(parameters));
		VariableReference ret_val = tc.addStatement(stmt);
		return ret_val;
	}

	public DefaultTestCase toTestCase() {
		return tc;
	}

}
