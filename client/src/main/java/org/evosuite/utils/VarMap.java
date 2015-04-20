/**
 * 
 */
package org.evosuite.utils;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Mapping between type variables and actual parameters.
 * 
 * @author Wouter Coekaerts <wouter@coekaerts.be>
 */
class VarMap {
	public final Map<TypeVariable<?>, Type> map = new HashMap<TypeVariable<?>, Type>();

	/**
	 * Creates an empty VarMap
	 */
	VarMap() {
	}

	void add(TypeVariable<?> variable, Type value) {
		map.put(variable, value);
	}

	void addAll(TypeVariable<?>[] variables, Type[] values) {
		assert variables.length == values.length;
		for (int i = 0; i < variables.length; i++) {
			map.put(variables[i], values[i]);
		}
	}

	void addAll(Map<TypeVariable<?>, GenericClass> variables) {
		for (Entry<TypeVariable<?>, GenericClass> entry : variables.entrySet()) {
			map.put(entry.getKey(), entry.getValue().getType());
		}
	}

	VarMap(TypeVariable<?>[] variables, Type[] values) {
		addAll(variables, values);
	}

	Type map(Type type) {
		if (type instanceof Class) {
			return type;
		} else if (type instanceof TypeVariable) {
			// TypeVariables may also come from generic methods!
			// assert map.containsKey(type);
			if (map.containsKey(type))
				return map.get(type);
			else
				// TODO: Bounds should be mapped, but might be recursive so we just use unbounded for now
				return new WildcardTypeImpl(new Type[] { Object.class }, new Type[] {});

		} else if (type instanceof ParameterizedType) {
			ParameterizedType pType = (ParameterizedType) type;
			return new ParameterizedTypeImpl((Class<?>) pType.getRawType(),
			        map(pType.getActualTypeArguments()),
			        pType.getOwnerType() == null ? pType.getOwnerType()
			                : map(pType.getOwnerType()));
		} else if (type instanceof WildcardType) {
			WildcardType wType = (WildcardType) type;
			return new WildcardTypeImpl(map(wType.getUpperBounds()),
			        map(wType.getLowerBounds()));
		} else if (type instanceof GenericArrayType) {
			return GenericArrayTypeImpl.createArrayType(map(((GenericArrayType) type).getGenericComponentType()));
		} else {
			throw new RuntimeException("not implemented: mapping " + type.getClass()
			        + " (" + type + ")");
		}
	}

	Type[] map(Type[] types) {
		Type[] result = new Type[types.length];
		for (int i = 0; i < types.length; i++) {
			result[i] = map(types[i]);
		}
		return result;
	}
}
