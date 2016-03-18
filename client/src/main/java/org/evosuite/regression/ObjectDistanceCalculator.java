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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.evosuite.ga.FitnessFunction;
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
public class ObjectDistanceCalculator {

	private static final double B = 1;
	private static final double R = 10;
	private static final double V = 10;
	private static final double C = 10;
	private static final int MAX_RECURSION = 4;
	
	public static int different_variables = 0;

	public static double getObjectDistance(Object p, Object q) {
		double distance = new ObjectDistanceCalculator().getObjectDistanceImpl(
				p, q);
		// assert distance >= 0.0 : "Result was " + distance;
		// assert !Double.isNaN(distance);
		// assert !Double.isInfinite(distance);
		return distance;
	}

	private static Collection<Field> getAllFields(Class<?> commonAncestor) {
		Collection<Field> result = new ArrayList<Field>();
		Class<?> ancestor = commonAncestor;
		while (!ancestor.equals(Object.class)) {
			result.addAll(Arrays.asList(ancestor.getDeclaredFields()));
			ancestor = ancestor.getSuperclass();
		}
		return result;
	}

	private static Class<?> getCommonAncestor(Object p, Object q) {
		double pInheritCnt = getTypeDistance(Object.class, p);
		double qInheritCnt = getTypeDistance(Object.class, q);

		Class<?> pClass = p.getClass();
		Class<?> qClass = q.getClass();

		while (!pClass.equals(qClass)) {
			if (pInheritCnt > qInheritCnt) {
				pClass = pClass.getSuperclass();
				pInheritCnt--;
			} else {
				qClass = qClass.getSuperclass();
				qInheritCnt--;
			}
		}
		return pClass;
	}

	private static double getElementaryDistance(Boolean p, Boolean q) {
		if (p.equals(q)) {
			return 0;
		}
		return B;
	}

	private static double normalize(double x) {
		return x / (x + 1.0);
	}

	private static double normalize_inverse(double x) {
		return 1.0 / (x + 1.0);
	}

	private static double getElementaryDistance(Character p, Character q) {
		if (p.equals(q)) {
			return 0;
		} else {
			different_variables++;
		}
		
		return normalize(Math.abs(p.charValue() - q.charValue()));
	}

	private static double getElementaryDistance(Number p, Number q) {
		//if(p.equals(-1) && !q.equals(-1))
		//System.out.println("Num1: " + p + " | Num2: " + q);
		if(!p.equals(q))
			different_variables++;
		
		if ((p instanceof Double)
				&& (((Double) p).isNaN() || ((Double) p).isInfinite())) {
			if (p.equals(q))
				return 0;
			else
				return 1;
		}

		if ((p instanceof Float)
				&& (((Float) p).isNaN() || ((Float) p).isInfinite())) {
			if (p.equals(q))
				return 0;
			else
				return 1;
		}
		
		double distance = Math.abs(p.doubleValue() - q.doubleValue());
		
		// If the epsilon is less than 0.01D (as is used for assertion generation)
		// set distance to 0.
		if(p instanceof Double){
			if(distance<0.01)
				distance = 0;
		}
		
		return normalize(distance);
	}

	private static double getElementaryDistance(String p, String q) {
		// Levenshtein distance
		if(!p.equals(q))
			different_variables++;
		int[][] distanceMatrix = new int[p.length() + 1][q.length() + 1];
		for (int idx = 0; idx <= p.length(); idx++) {
			distanceMatrix[idx][0] = idx;
		}
		for (int jdx = 1; jdx <= q.length(); jdx++) {
			distanceMatrix[0][jdx] = jdx;
		}
		for (int idx = 1; idx <= p.length(); idx++) {
			for (int jdx = 1; jdx <= q.length(); jdx++) {
				int cost;
				if (p.charAt(idx - 1) == q.charAt(jdx - 1)) {
					cost = 0;
				} else {
					cost = 1;
				}
				distanceMatrix[idx][jdx] = Math.min(
						distanceMatrix[idx - 1][jdx] + 1, // deletion
						Math.min(distanceMatrix[idx][jdx - 1] + 1, // insertion
								distanceMatrix[idx - 1][jdx - 1] + cost // substitution
						));
				if ((idx > 1) && (jdx > 1)
						&& (p.charAt(idx - 1) == q.charAt(jdx - 2))
						&& (p.charAt(idx - 2) == q.charAt(jdx - 1))) {
					distanceMatrix[idx][jdx] = Math.min(
							distanceMatrix[idx][jdx],
							distanceMatrix[idx - 2][jdx - 2] + cost // transposition
					);
				}
			}
		}
		return normalize(distanceMatrix[p.length()][q.length()]);
	}

	private static Object getFieldValue(Field field, Object p) {
		try {
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
		}
	}

	private static Integer getHasCode(Object p, Object q) {
		return ((p == null) ? 0 : p.hashCode())
				+ ((q == null) ? 0 : q.hashCode());
	}

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

	private boolean breakRecursion(Object p, Object q) {
		Integer hashCode = getHasCode(p, q);
		Integer recursionCnt = hashRecursionCntMap.get(hashCode);
		if (recursionCnt == null) {
			recursionCnt = 0;
		}
		if (recursionCnt >= MAX_RECURSION) {
			return true;
		}
		recursionCnt++;
		hashRecursionCntMap.put(hashCode, recursionCnt);
		return false;
	}

	private double getCompositeObjectDistance(Object p, Object q) {
		Double cachedDistance = resultCache.get(getHasCode(p, q));
		if (cachedDistance != null) {
			return cachedDistance;
		}
		if (breakRecursion(p, q)) {
			return 0.0;
		}
		Class<?> commonAncestor = getCommonAncestor(p, q);
		double distance = getTypeDistance(commonAncestor, p, q);
		distance += getFieldDistance(commonAncestor, p, q);
		resultCache.put(getHasCode(p, q), distance);
		return distance;
	}

	private double getFieldDistance(Class<?> commonAncestor, Object p, Object q) {
		Collection<Field> fields = getAllFields(commonAncestor);
		double sum = 0;
		for (Field field : fields) {
			sum += getObjectDistanceImpl(getFieldValue(field, p),
					getFieldValue(field, q));
		}
		if (sum == 0.0) {
			return sum;
		}
		return sum / fields.size();
	}

	private static double getObjectDistanceImpl(Object p, Object q) {
		
		if (p == q) {
			return 0.0;
		}

		// both are null
		if ((p == null) || (q == null)) {
			return 0;
		}

		// type mismatch
		if (((p instanceof Double) && (!(q instanceof Double)))
				|| ((q instanceof Double) && (!(p instanceof Double))))
			return 1;
		
		
		
		
		
		/*if (((p instanceof Double) && 
				((Double.isNaN((Double) p)) || Double.isInfinite((Double) p)))
				|| ((q instanceof Double) && ((Double.isNaN((Double) q)) 
						|| Double.isInfinite((Double) q)))) {*/
		if ( (p instanceof Double) && (q instanceof Double) )
				{
			
			// One is NaN, other is Infinity
			if (((Double.isNaN((Double) p)) && (Double.isInfinite((Double) q)))
					|| ((Double.isNaN((Double) q)) && (Double
							.isInfinite((Double) p))))
				return 1;
			
			if ( ((Double.isNaN((Double) p)) && (!Double.isNaN((Double) q)) )
					|| ((Double.isNaN((Double) q)) && (!Double.isNaN((Double) p)) ) )
				return 1;
			
			
			if ( ((Double.isInfinite((Double) p)) && (!Double.isInfinite((Double) q)) )
					|| ((Double.isInfinite((Double) q)) && (!Double.isInfinite((Double) p)) ) )
				return 1;
			
			if ( (Double.isInfinite((Double) p)) && (Double.isInfinite((Double) q)) ){
				if(!((Double) p).equals(((Double) q)))
					return 1;
			}
			
			
			
			/*if ((((p instanceof Double) && ((Double.isNaN((Double) p)) || Double
					.isInfinite((Double) p))) && (!((q instanceof Double) && ((Double
					.isNaN((Double) q)) || Double.isInfinite((Double) q)))))
					|| (!(((p instanceof Double) && ((Double.isNaN((Double) p)) || Double
							.isInfinite((Double) p)))) && (((q instanceof Double) && ((Double
							.isNaN((Double) q)) || Double
							.isInfinite((Double) q))))))
				return 1;
			else
				return 0;
				*/
		}
		
		
		if (((p instanceof Float) && (!(q instanceof Float)))
				|| ((q instanceof Float) && (!(p instanceof Float))))
			return 1;
		
		
		if ( (p instanceof Float) && (q instanceof Float) )
		{
	
			// One is NaN, other is Infinity
			if (((Float.isNaN((Float) p)) && (Float.isInfinite((Float) q)))
					|| ((Float.isNaN((Float) q)) && (Float
							.isInfinite((Float) p))))
				return 1;
			
			if ( ((Float.isNaN((Float) p)) && (!Float.isNaN((Float) q)) )
					|| ((Float.isNaN((Float) q)) && (!Float.isNaN((Float) p)) ) )
				return 1;
			
			
			if ( ((Float.isInfinite((Float) p)) && (!Float.isInfinite((Float) q)) )
					|| ((Float.isInfinite((Float) q)) && (!Float.isInfinite((Float) p)) ) )
				return 1;
			
			if ( (Float.isInfinite((Float) p)) && (Float.isInfinite((Float) q)) ){
				if(!((Float) p).equals(((Float) q)))
					return 1;
			}
		
		}
		

		if (!p.getClass().equals(q.getClass()))
			return 0;

		// What if one is a primitive and the other not?
		// if (!value.getClass().equals(value2.getClass())) ?
		if (p instanceof Number) {
			return getElementaryDistance((Number) p, (Number) q);
		}
		if (p instanceof Boolean) {
			return getElementaryDistance((Boolean) p, (Boolean) q);
		}
		if (p instanceof String) {
			return getElementaryDistance((String) p, (String) q);
		}
		if (p instanceof Character) {
			return getElementaryDistance((Character) p, (Character) q);
		}

		if (p instanceof Map) {
			return normalize(getObjectMapDistance((Map<String, Object>) p,
					(Map<String, Object>) q));
		}
		
		// TODO: enums.
		if (p instanceof Enum) {
			return 0;
			/*return getElementaryDistance(((Enum) p).ordinal(),
					((Enum) q).ordinal());*/
		}

		throw new Error("Distance of unknown type!");
	}

	protected static final Logger logger = LoggerFactory
			.getLogger(ObjectDistanceCalculator.class);

	public static double getObjectMapDistance(Map<String, Object> map1,
			Map<String, Object> map2) {
		double distance = 0.0;

		for (String fieldName : map1.keySet()) {
			if (!map2.containsKey(fieldName))
				continue;
			Object value1 = map1.get(fieldName);
			Object value2 = map2.get(fieldName);
			double tmpDistance = 0;
			try{
				tmpDistance = getObjectDistanceImpl(value1, value2);
			}catch(OutOfMemoryError e){
				e.printStackTrace();
			}
			/*if(tmpDistance !=0)
			//if(fieldName.equals("fake_var_java_lang_Double"))
			 System.out.println("field: " + fieldName + ", d: " +
					 tmpDistance+" <");
			*/
			if (Double.valueOf(tmpDistance).isNaN()
					|| Double.valueOf(tmpDistance).isInfinite()) {
				different_variables++;
				tmpDistance = 0;
				
			}

			distance += tmpDistance;
			
			 
		}
		// System.out.println("final dis: " + distance+" +");

		return distance;
	}
}
