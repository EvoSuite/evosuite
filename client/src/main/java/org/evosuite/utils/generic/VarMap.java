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
/**
 * 
 */
package org.evosuite.utils.generic;

import org.evosuite.runtime.util.Inputs;
import org.evosuite.utils.ParameterizedTypeImpl;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Mapping between type variables and actual parameters.
 * 
 */
public class VarMap {

	private final Map<TypeVariable<?>, Type> map = new LinkedHashMap<>();

	/**
	 * Creates an empty VarMap
	 */
	public VarMap() {
	}

	public void add(TypeVariable<?> variable, Type value) {
		map.put(variable, value);
	}

	public void addAll(TypeVariable<?>[] variables, Type[] values) throws IllegalArgumentException{
		Inputs.checkNull(variables,values);
		if(variables.length != values.length) {
			throw new IllegalArgumentException("Array length mismatch");
		}

		for (int i = 0; i < variables.length; i++) {
			add(variables[i], values[i]);
		}
	}

	public void addAll(Map<TypeVariable<?>, GenericClass> variables) throws IllegalArgumentException{
		Inputs.checkNull(variables);
		for (Entry<TypeVariable<?>, GenericClass> entry : variables.entrySet()) {
			add(entry.getKey(), entry.getValue().getType());
		}
	}


	public Type map(Type type) throws IllegalArgumentException{
		Inputs.checkNull(type);

		if (type instanceof Class) {
			return type;
		} else if (type instanceof TypeVariable) {
			// TypeVariables may also come from generic methods!
			// assert map.containsKey(type);
			if (map.containsKey(type))
				return map.get(type);
			else {
				//FIXME: (wrong) tmp workaround, as WildcardTypeImpl does crash EvoSuite
				//return Object.class;
				// TODO: Bounds should be mapped, but might be recursive so we just use unbounded for now
				return new WildcardTypeImpl(new Type[]{Object.class}, new Type[]{});
			}
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
			throw new IllegalArgumentException("not implemented: mapping " + type.getClass()
			        + " (" + type + ")");
		}
	}

	public Type[] map(Type[] types) throws IllegalArgumentException{
		Inputs.checkNull(types);
		Type[] result = new Type[types.length];
		for (int i = 0; i < types.length; i++) {
			result[i] = map(types[i]);
		}
		return result;
	}
}
