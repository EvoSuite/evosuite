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
package com.examples.with.different.packagename.idnaming.gnu.trove.strategy;

/**
 * A {@link gnu.trove.strategy.HashingStrategy} that does identity comparisons
 * (<tt>==</tt>) and uses {@link System#identityHashCode(Object)} for hashCode generation.
 */
public class IdentityHashingStrategy<K> implements HashingStrategy<K> {
	static final long serialVersionUID = -5188534454583764904L;
	
	public int computeHashCode( K object ) {
		return System.identityHashCode( object );
	}

	public boolean equals( K o1, K o2 ) {
		return o1 == o2;
	}
}
