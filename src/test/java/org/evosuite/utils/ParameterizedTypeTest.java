/**
 * 
 */
package org.evosuite.utils;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.evosuite.setup.TestClusterGenerator;
import org.evosuite.testcase.TestCodeVisitor;
import org.junit.Assert;
import org.junit.Test;

import com.googlecode.gentyref.GenericTypeReflector;
import com.googlecode.gentyref.TypeToken;

/**
 * @author Gordon Fraser
 * 
 */
public class ParameterizedTypeTest {

	@Test
	public void testSimple() {
		ParameterizedType pType = new ParameterizedTypeImpl(List.class,
		        new Type[] { String.class }, null);
		Type listOfString = new TypeToken<List<String>>() {
		}.getType();
		Assert.assertEquals(pType, listOfString);
	}


	@Test
	public void testSimple5() {
		GenericClass mapClass = new GenericClass(Map.class);
		List<GenericClass> parameterTypes = new ArrayList<GenericClass>();
		parameterTypes.add(new GenericClass(String.class));
		parameterTypes.add(new GenericClass(String.class));
		mapClass.setParameterTypes(parameterTypes);

		Assert.assertEquals(2, mapClass.getNumParameters());
		List<Type> types = mapClass.getParameterTypes();
		Assert.assertEquals(String.class, types.get(0));
		Assert.assertEquals(String.class, types.get(1));
		Assert.assertEquals("java.util.Map<java.lang.String, java.lang.String>",
		                    mapClass.getTypeName());
	}

	@Test
	public void testSimple6() {
		GenericClass mapClass = new GenericClass(Map.class);
		List<GenericClass> parameterTypes = new ArrayList<GenericClass>();
		parameterTypes.add(new GenericClass(String.class));
		parameterTypes.add(new GenericClass(String.class));
		mapClass.setParameterTypes(parameterTypes);
		TestCodeVisitor visitor = new TestCodeVisitor();
		String typeName = visitor.getTypeName(mapClass.getType());

		Assert.assertEquals("Map<String, String>", typeName);
	}

}
