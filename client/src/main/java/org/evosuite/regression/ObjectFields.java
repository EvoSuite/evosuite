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
package org.evosuite.regression;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.management.RuntimeErrorException;

import org.apache.commons.lang3.ClassUtils;
import org.evosuite.testcase.execution.Scope;
import org.evosuite.testcase.variable.VariableReference;
import org.evosuite.utils.generic.GenericClass;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of object distance following
 * "Object distance and its application to adaptive random testing of object-oriented programs"
 * by Ilinca Ciupa, Andreas Leitner, Manuel Oriol, Bertrand Meyer (<a
 * href="http://se.ethz.ch/~meyer/publications/testing/object_distance.pdf"
 * >http://se.ethz.ch/~meyer/publications/testing/object_distance.pdf</a>).
 * 
 * We implemented the following changes:
 * <ul>
 * <li>In the paper if a reference field does not match (i.e., is different
 * because the types are different) then R = 10 is used as difference. This does
 * not make sense for several reasons:
 * <ul>
 * <li>The difference is already taken into account in the type distance.</li>
 * <li>If one type adds a field but the other does not, so what? R/2?</li>
 * </ul>
 * Therefore we decided to apply the value R as factor in the type difference to
 * the non-shared fields.</li>
 * <li>In the paper a factor of 1/2 is applied to the recursive distance. We
 * treat the recursive distance simply as any other field distance, thus it is
 * normalized by the number of fields and not by a static factor.</li>
 * <li>There is no distance given for two characters. We defined that to be C =
 * 10.</li>
 * </ul>
 * 
 * @author roessler
 */
public class ObjectFields {

	private static final double B = 10;
	private static final double R = 10;
	private static final double V = 10;
	private static final double C = 10;
	private static int MAX_RECURSION = 1;

	/*
	 * public static double getObjectDistance(Object p, Object q) { double
	 * distance = new ObjectVariables().getObjectDistanceImpl(p, q); assert
	 * distance >= 0.0 : "Result was " + distance; assert
	 * !Double.isNaN(distance); assert !Double.isInfinite(distance); return
	 * distance; }
	 */

	private Scope scope;

	public ObjectFields(Scope s) {
		scope = s;
		// getObjectVariables();
	}

	/*
	 * public static Object getObjectVariables(Object p) { Object distance = new
	 * ObjectVariables().getObjectValue(p); // assert distance >= 0.0 :
	 * "Result was " + distance; // assert !Double.isNaN(distance); // assert
	 * !Double.isInfinite(distance); return distance; }
	 */

	public List<Map<Integer, Map<String, Object>>> getObjectVariables(Object p,
			Class<?> c) {

		List<Map<Integer, Map<String, Object>>> vars = getFieldValues(c, p);
		// logger.warn("getObjectVariables: " + vars);

		return vars;
	}

	private static Collection<Field> getAllFields(Class<?> commonAncestor) {
		Collection<Field> result = new ArrayList<Field>();
		Class<?> ancestor = commonAncestor;

		while (!ancestor.equals(Object.class)) {
			for (Field f : ancestor.getDeclaredFields()) {
				if (Modifier.isFinal(f.getModifiers()))
					continue;
				if (Modifier.isStatic(f.getModifiers()))
					continue;
				result.add(f);
			}

			// result.addAll(Arrays.asList(ancestor.getDeclaredFields()));
			ancestor = ancestor.getSuperclass();
		}
		return result;
	}

	/*
	 * private static Class<?> getCommonAncestor(Object p) { double pInheritCnt
	 * = getTypeDistance(Object.class, p);
	 * 
	 * Class<?> pClass = p.getClass();
	 * 
	 * while (!pClass.equals(qClass)) { if (pInheritCnt > qInheritCnt) { pClass
	 * = pClass.getSuperclass(); pInheritCnt--; } else { qClass =
	 * qClass.getSuperclass(); qInheritCnt--; } } return pClass; }
	 */

	private static boolean getElementaryValue(Boolean p) {
		return p.booleanValue();
	}

	private static char getElementaryValue(Character p) {
		return p.charValue();
	}

	private static double getElementaryValue(Number p) {
		return p.doubleValue();
	}
	
	private static double getElementaryValue(Enum<?> p) {
		return p.ordinal();
	}

	private static String getElementaryValue(String p) {
		return p;
	}

	private static Object getFieldValue(Field field, Object p) {
		try {
			/*Class objClass = p.getClass();
			if(p instanceof java.lang.String){
				((String) p).hashCode();
			}*/
			Class<?> fieldType = field.getType();
			field.setAccessible(true);
			if (fieldType.isPrimitive()) {
				if (fieldType.equals(Boolean.TYPE)) {
					return field.getBoolean(p);
				}
				if (fieldType.equals(Integer.TYPE)) {
					return field.getInt(p);
				}
				if (fieldType.equals(Byte.TYPE)) {
					return field.getByte(p);
				}
				if (fieldType.equals(Short.TYPE)) {
					return field.getShort(p);
				}
				if (fieldType.equals(Long.TYPE)) {
					return field.getLong(p);
				}
				if (fieldType.equals(Double.TYPE)) {
					return field.getDouble(p);
				}
				if (fieldType.equals(Float.TYPE)) {
					return field.getFloat(p);
				}
				if (fieldType.equals(Character.TYPE)) {
					return field.getChar(p);
				}
				throw new UnsupportedOperationException("Primitive type "
						+ fieldType + " not implemented!");
			}
			return field.get(p);
		} catch (IllegalAccessException exc) {
			throw new RuntimeException(exc);
		} catch(OutOfMemoryError e){
			e.printStackTrace();
			if(MAX_RECURSION!=0)
			MAX_RECURSION = 0;
			else
				throw new RuntimeErrorException(e);
			return getFieldValue(field, p);
		}
	}

	/*
	 * private static Integer getHasCode(Object p) { return ((p == null) ? 0 :
	 * p.hashCode()) ; }
	 */
	private static Collection<Field> getNonSharedFields(
			Class<?> commonAncestor, Object p) {
		Collection<Field> result = new ArrayList<Field>();
		Class<?> ancestor = p.getClass();
		while (!ancestor.equals(commonAncestor)) {
			result.addAll(Arrays.asList(ancestor.getDeclaredFields()));
			ancestor = ancestor.getSuperclass();
		}
		return result;
	}

	private static double getTypeDistance(Class<?> commonAncestor, Object p) {
		double result = 0.0;
		Class<?> ancestor = p.getClass();
		while (!ancestor.equals(commonAncestor)) {
			ancestor = ancestor.getSuperclass();
			result++;
		}
		return result;
	}

	private static double getTypeDistance(Class<?> commonAncestor, Object p,
			Object q) {
		double result = getTypeDistance(commonAncestor, p)
				+ getTypeDistance(commonAncestor, q);
		result += getNonSharedFields(commonAncestor, p).size() * R;
		result += getNonSharedFields(commonAncestor, q).size() * R;
		return result;
	}

	private final Map<Integer, Integer> hashRecursionCntMap = new LinkedHashMap<Integer, Integer>();
	private final Map<Integer, Double> resultCache = new LinkedHashMap<Integer, Double>();

	/*
	 * private boolean breakRecursion(Object p) { Integer hashCode =
	 * getHasCode(p); Integer recursionCnt = hashRecursionCntMap.get(hashCode);
	 * if (recursionCnt == null) { recursionCnt = 0; } if (recursionCnt >=
	 * MAX_RECURSION) { return true; } recursionCnt++;
	 * hashRecursionCntMap.put(hashCode, recursionCnt); return false; }
	 */

	/*
	 * private double getCompositeObjectValue(Object p) { Double cachedDistance
	 * = resultCache.get(getHasCode(p)); if (cachedDistance != null) { return
	 * cachedDistance; } if (breakRecursion(p)) { return 0.0; } Class<?>
	 * commonAncestor = getCommonAncestor(p); double distance =
	 * getTypeDistance(commonAncestor, p); //distance +=
	 * getFieldDistance(commonAncestor, p); resultCache.put(getHasCode(p),
	 * distance); return distance; }
	 */

	public Map<Integer, Map<String, Map<String, Object>>> getObjectVariables() {

		//List<Map<Integer, Map<String, Object>>> ov = new ArrayList<Map<Integer, Map<String, Object>>>();

		//Map<Integer, Map<String, Object>> variable_field = new HashMap<Integer, Map<String, Object>>();

		Map<Integer, Map<String, Map<String, Object>>> variable_ref_field = new HashMap<Integer, Map<String, Map<String, Object>>>();

		//Map<String, Object> field_value = new HashMap<String, Object>();

		// Collection<Field> fields = getAllFields(c);
		//List<Object> values = new ArrayList<Object>();
		/*
		 * for (Field field : fields) { if (p.getClass().isPrimitive())
		 * values.add(getObjectValue(getFieldValue(field, p))); else
		 * values.addAll(getObjectValues(getFieldValue(field, p)));
		 * 
		 * }
		 */

		for (VariableReference vref : scope.getVariables()) {

			Object scope_object = scope.getObject(vref);

			if (scope_object == null)
				continue;

			String vref_class = vref.getClassName();

			// logger.warn(x);

			int vref_string = vref.getStPosition();
			Map<String, Object> objectMap = getObjectMap(scope_object);

			Map<String, Map<String, Object>> vrefObjectMap = new HashMap<String, Map<String, Object>>();
			vrefObjectMap.put(vref_class, objectMap);

			// variable_field.put(vref_string, objectMap);
			variable_ref_field.put(vref_string, vrefObjectMap);

			/*
			 * 
			 * Class<?> so_class = scope_object.getClass();
			 * 
			 * int vref_string = vref.getStPosition();
			 * 
			 * if (ClassUtils.isPrimitiveOrWrapper(scope_object.getClass())) {
			 * Map<String, Object> all_vars = new HashMap<String, Object>();
			 * variable_field.put(vref_string, (Map<String, Object>) (all_vars
			 * .put("fake_var", scope_object))); } else { Map<String, Object>
			 * all_vars = getAllVars(scope_object, 0,"");
			 * variable_field.put(vref_string, all_vars); }
			 */

			// boolean is_prim = ClassUtils.isPrimitiveOrWrapper(so_class);

			// assert is_prim != false : so_class.toString();

			/*
			 * logger.warn("scope_ob: " + scope_object.toString() + ", so_class"
			 * + so_class.toString() + ", vref:" + vref_string +
			 * ",primitve? "+is_prim);
			 */

		}

		return variable_ref_field;
	}

	public Map<Integer, Map<String, Object>> getObjectVariables(
			Collection<VariableReference> vrefs) {

		List<Map<Integer, Map<String, Object>>> ov = new ArrayList<Map<Integer, Map<String, Object>>>();

		Map<Integer, Map<String, Object>> variable_field = new HashMap<Integer, Map<String, Object>>();

		Map<Integer, Map<String, Map<String, Object>>> variable_ref_field = new HashMap<Integer, Map<String, Map<String, Object>>>();

		Map<String, Object> field_value = new HashMap<String, Object>();

		// Collection<Field> fields = getAllFields(c);
		List<Object> values = new ArrayList<Object>();
		/*
		 * for (Field field : fields) { if (p.getClass().isPrimitive())
		 * values.add(getObjectValue(getFieldValue(field, p))); else
		 * values.addAll(getObjectValues(getFieldValue(field, p)));
		 * 
		 * }
		 */

		for (VariableReference vref : vrefs) {

			Object scope_object = scope.getObject(vref);

			if (scope_object == null)
				continue;

			String vref_class = vref.getClassName();

			// logger.warn(x);

			int vref_string = vref.getStPosition();
			Map<String, Object> objectMap = getObjectMap(scope_object);
			// Map<String,Map<String,Object>> vrefObjectMap = new
			// HashMap<String,Map<String,Object>>();
			// vrefObjectMap.put(vref_class, objectMap);

			variable_field.put(vref_string, objectMap);
			// variable_ref_field.put(vref_string, vrefObjectMap);

			/*
			 * 
			 * Class<?> so_class = scope_object.getClass();
			 * 
			 * int vref_string = vref.getStPosition();
			 * 
			 * if (ClassUtils.isPrimitiveOrWrapper(scope_object.getClass())) {
			 * Map<String, Object> all_vars = new HashMap<String, Object>();
			 * variable_field.put(vref_string, (Map<String, Object>) (all_vars
			 * .put("fake_var", scope_object))); } else { Map<String, Object>
			 * all_vars = getAllVars(scope_object, 0,"");
			 * variable_field.put(vref_string, all_vars); }
			 */

			// boolean is_prim = ClassUtils.isPrimitiveOrWrapper(so_class);

			// assert is_prim != false : so_class.toString();

			/*
			 * logger.warn("scope_ob: " + scope_object.toString() + ", so_class"
			 * + so_class.toString() + ", vref:" + vref_string +
			 * ",primitve? "+is_prim);
			 */

		}

		return variable_field;
	}

	public static Map<String, Object> getObjectMap(Object o) {

		if (ClassUtils.isPrimitiveOrWrapper(o.getClass()) || (o instanceof String)) {
			Map<String, Object> objectMap = new HashMap<String, Object>();
			objectMap.put("fake_var_"+o.getClass().getName().replace('.', '_'), o);
			return objectMap;
		} else {
			return getAllVars(o, 0, "");

		}

	}

	public static int getDim(Object array) {
		int dim = 0;
		Class cls = array.getClass();
		while (cls.isArray()) {
			dim++;
			cls = cls.getComponentType();
		}
		return dim;
	}

	private static Map<String, Object> getAllVars(Object p, int counter,
			String prefix) {
		// TODO Auto-generated method stub
		// logger.warn("getting: " + ((p instanceof Field)?((Field)
		// p).getType():p.getClass()) + ", count:" + counter);

		Map<String, Object> values = new HashMap<String, Object>();

		// if(((p instanceof Field)?((Field) p).getType():p.getClass()) == null)
		// return values;

		if (p == null)
			return values;

		Collection<Field> fields = getAllFields(p.getClass());

		for (Field field : fields) {
			// String what_happened = "";

			GenericClass gc = new GenericClass(field.getType());

			if (ClassUtils.isPrimitiveOrWrapper(field.getType())
					|| gc.isString()) {
				// what_happened += ", " + field.getType() + " is primitive,";
				if (field.getName().equals("serialVersionUID"))
					continue;
				values.put(prefix + field.getName(),
						getObjectValue(getFieldValue(field, p)));

			} else if (field.getType().equals(Object.class)
					|| counter >= MAX_RECURSION) {
				values.put(prefix + field.getName(),
						(getObjectValue(getFieldValue(field, p)) != null));
				// what_happened += ", " + field.getType() + ",reached end,";
			} else if (field.getType().isArray()) {
				/*
				 * values.put(prefix + field.getName(), Array
				 * .getLength(getObjectValue(getFieldValue(field, p))));
				 */
				// Object[] arr = (Object[]) getFieldValue(field, p);
				/*
				 * for(Object o: arr){ values.putAll(getAllVars(o, counter +
				 * 1,prefix + ((prefix.equals(""))?"":".")+field.getName())); }
				 */

				Object arr = getFieldValue(field, p);
				if (arr == null)
					return values;

				for (int n = 0; n < Array.getLength(arr); n++) {
					// values.putAll(getAllVars(Array.get(arr,n), counter +
					// 1,prefix + ((prefix.isEmpty())?"":".")+field.getName()));
					values.put(
							prefix + field.getName(),
							getAllVars(Array.get(arr, n), counter + 1,
									prefix + ((prefix.isEmpty()) ? "" : ".")
											+ field.getName()));
				}

			} else {
				try {
					field.setAccessible(true);

					// values.putAll(getAllVars(field.get(p), counter + 1,prefix
					// + ((prefix.isEmpty())?"":".")+field.getName()));
					values.put(
							prefix + field.getName(),
							getAllVars(field.get(p), counter + 1,
									prefix + ((prefix.isEmpty()) ? "" : ".")
											+ field.getName()));

				} catch (IllegalArgumentException e) {
					// TODO Auto-generated catch block

					return values;
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block

					return values;
				}
				// what_happened += ", " + field.getType() + ",recursed,";
			}

			// logger.warn(prefix + field.getName() + what_happened);
		}

		/*
		 * if (ClassUtils.isPrimitiveOrWrapper(p.getClass())){
		 * 
		 * return null; } else {
		 * 
		 * }
		 */

		return values;
	}

	private static final Logger logger = LoggerFactory
			.getLogger(ObjectFields.class);

	private Collection<Object> getObjectValues(Object fieldValue) {
		int i = 0;
		List<Object> values = new ArrayList<Object>();
		while (!fieldValue.getClass().isPrimitive() && i < 10) {
			Collection<Field> fields = getAllFields(fieldValue.getClass());
			for (Field field : fields) {
				if (fieldValue.getClass().isPrimitive())
					values.add(getObjectValue(getFieldValue(field, fieldValue)));
				else
					fieldValue = field;

			}

			i++;
		}
		return values;
	}

	private Collection<Object> getAllObjectValues(Object p, int counter) {
		if (!p.getClass().isPrimitive())
			return null;// getObjectValues(p,(counter+1));
		else {

		}
		return null;

	}

	private List<Map<Integer, Map<String, Object>>> getFieldValues(
			Class<?> commonAncestor, Object p) {
		Collection<Field> fields = getAllFields(commonAncestor);
		List<Object> values = new ArrayList<Object>();
		for (Field field : fields) {
			values.add(getObjectValue(getFieldValue(field, p)));
			int counter = 0;
			if (!p.getClass().isPrimitive()) {

			}

			// field.
		}

		/*
		 * List<Map<Integer, Map<String, Object>>>; var.getStPosition() ->
		 * Map<field.getName() -> Object>
		 * 
		 * @Test public void test() { Foo var0 = new Foo(); Bar var1 = foo.b; }
		 * 
		 * [ { 0 -> { "x" -> 0; "b.y" -> 0; } }, { 0 -> { "x" -> 0; "b.y" -> 0;
		 * }, 1 -> { "y" -> 0 }} ]
		 * 
		 * 
		 * class Bar { int y; } class Foo { int x; Bar b; }
		 * 
		 * "x" -> 1; "b.y" -> 2;
		 */
		return null;
		// return values;
	}

	private static Object getObjectValue(Object p) {

		if ((p == null)) {
			return V;
		}
		// What if one is a primitive and the other not?
		// if (!value.getClass().equals(value2.getClass())) ?
		if (p instanceof Number) {
			return getElementaryValue((Number) p);
		}
		if (p instanceof Enum) {
			return getElementaryValue((Enum) p);
		}
		if (p instanceof Boolean) {
			return getElementaryValue((Boolean) p);
		}
		if (p instanceof String) {
			return getElementaryValue((String) p);
		}
		if (p instanceof Character) {
			return getElementaryValue((Character) p);
		}
		return p;// getCompositeObjectValue(p);
	}
}
