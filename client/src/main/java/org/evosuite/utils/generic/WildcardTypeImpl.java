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
package org.evosuite.utils.generic;

import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.Arrays;

import com.googlecode.gentyref.GenericTypeReflector;

public class WildcardTypeImpl implements WildcardType {

	private Type[] upperBounds;
	private Type[] lowerBounds;

	public WildcardTypeImpl(Type[] upperBounds, Type[] lowerBounds) {
		if (upperBounds.length == 0)
			throw new IllegalArgumentException(
			        "There must be at least one upper bound. For an unbound wildcard, the upper bound must be Object");
		this.upperBounds = upperBounds;
		this.lowerBounds = lowerBounds;
	}

	@Override
	public Type[] getUpperBounds() {
		return upperBounds.clone();
	}

	@Override
	public Type[] getLowerBounds() {
		return lowerBounds.clone();
	}

	public void setUpperBounds(Type[] bounds) {
		this.upperBounds = bounds;
	}

	public void setLowerBounds(Type[] bounds) {
		this.lowerBounds = bounds;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof WildcardType))
			return false;
		WildcardType other = (WildcardType) obj;
		return Arrays.equals(lowerBounds, other.getLowerBounds())
		        && Arrays.equals(upperBounds, other.getUpperBounds());
	}

	@Override
	public int hashCode() {
		return Arrays.hashCode(lowerBounds) ^ Arrays.hashCode(upperBounds);
	}

	@Override
	public String toString() {
		if (lowerBounds.length > 0) {
			return "? super " + GenericTypeReflector.getTypeName(lowerBounds[0]);
		} else if (upperBounds[0] == Object.class) {
			return "?";
		} else {
			return "? extends " + GenericTypeReflector.getTypeName(upperBounds[0]);
		}
	}

}
