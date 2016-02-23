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
package com.examples.with.different.packagename.idnaming.gnu.trove.list;

/**
 * Simple adapter class implementing {@link TLinkable}, so you don't have to. Example:
 * <pre>
	private class MyObject extends TLinkableAdapter<MyObject> {
		private final String value;

		MyObject( String value ) {
			this.value = value;
		}
		
		public String getValue() {
			return value;
		}
	}
 * </pre>
 */
public abstract class TLinkableAdapter<T extends TLinkable> implements TLinkable<T> {
	private volatile T next;
	private volatile T prev;

	@Override
	public T getNext() {
		return next;
	}

	@Override
	public void setNext( T next ) {
		this.next = next;
	}

	@Override
	public T getPrevious() {
		return prev;
	}

	@Override
	public void setPrevious( T prev ) {
		this.prev = prev;
	}
}
