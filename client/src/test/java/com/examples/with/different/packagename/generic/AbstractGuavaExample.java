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
package com.examples.with.different.packagename.generic;

/**
 * @author Gordon Fraser
 * 
 */
public abstract class AbstractGuavaExample<T> {

	public <S extends T> Wrapper<S> wrap(S reference) {
		return new Wrapper<S>(this, reference);
	}

	public static AbstractGuavaExample<Object> identity() {
		return null;
	}

	public static class Wrapper<T> {
		private final AbstractGuavaExample<? super T> equivalence;
		private final T reference;

		private Wrapper(AbstractGuavaExample<? super T> equivalence, T reference) {
			this.equivalence = equivalence;
			this.reference = reference;
		}
	}
}
