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
package org.evosuite.utils;

/*
 * Copyright (C) 2008 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;

import com.googlecode.gentyref.CaptureType;
import com.googlecode.gentyref.GenericTypeReflector;

/**
 * An immutable implementation of the {@link ParameterizedType} interface. This
 * object allows us to build a reflective {@link Type} objects on demand. This
 * object is used to support serialization and deserialization of classes with
 * an {@code ParameterizedType} field where as least one of the actual type
 * parameters is a {@code TypeVariable}.
 * 
 * <p>
 * Here's an example class:
 * 
 * <pre>
 * class Foo&lt;T&gt; {
 * 	private List&lt;T&gt; someList;
 * 
 * 	Foo(List&lt;T&gt; list) {
 * 		this.someList = list;
 * 	}
 * }
 * </pre>
 * 
 * @author Inderjeet Singh
 * @author Joel Leitch
 */
public class ParameterizedTypeImpl implements ParameterizedType {

	private final Class<?> rawType;
	private final Type[] actualTypeArguments;
	private final Type ownerType;

	public ParameterizedTypeImpl(Class<?> rawType, Type[] actualTypeArguments, Type owner) {
		this.rawType = rawType;
		this.actualTypeArguments = actualTypeArguments;
		this.ownerType = owner;
	}

	@Override
	public Type getRawType() {
		return rawType;
	}

	@Override
	public Type[] getActualTypeArguments() {
		return actualTypeArguments;
	}

	@Override
	public Type getOwnerType() {
		return ownerType;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof ParameterizedType))
			return false;

		ParameterizedType other = (ParameterizedType) obj;
		if (this == other) {
			return true;
		}
		return rawType.equals(other.getRawType())
		        && Arrays.equals(actualTypeArguments, other.getActualTypeArguments())
		        && (ownerType == null ? other.getOwnerType() == null
		                : ownerType.equals(other.getOwnerType()));
	}

	@Override
	public int hashCode() {
		int result = rawType.hashCode() ^ Arrays.hashCode(actualTypeArguments);
		if (ownerType != null)
			result ^= ownerType.hashCode();
		return result;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		String clazz = rawType.getName();

		if (ownerType != null) {
			sb.append(GenericTypeReflector.getTypeName(ownerType)).append('.');

			String prefix = (ownerType instanceof ParameterizedType) ? ((Class<?>) ((ParameterizedType) ownerType).getRawType()).getName() + '$'
			        : ((Class<?>) ownerType).getName() + '$';
			if (clazz.startsWith(prefix))
				clazz = clazz.substring(prefix.length());
		}
		sb.append(clazz);

		if (actualTypeArguments.length != 0) {
			sb.append('<');
			for (int i = 0; i < actualTypeArguments.length; i++) {
				Type arg = actualTypeArguments[i];
				if (i != 0)
					sb.append(", ");
				if (arg instanceof CaptureType) {
					CaptureType captureType = (CaptureType) arg;
					if (captureType.getLowerBounds().length == 0)
						sb.append("?");
					else
						sb.append(captureType.getLowerBounds()[0].toString());
				} else if (arg == null) {
					sb.append("null");
				} else {
					sb.append(GenericTypeReflector.getTypeName(arg));
				}
			}
			sb.append('>');
		}

		return sb.toString();
	}

}
