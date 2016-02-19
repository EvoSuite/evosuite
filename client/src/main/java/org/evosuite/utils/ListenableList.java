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
 * Copyright (C) 2011,2012 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Public License for more details.
 *
 * You should have received a copy of the GNU Public License along with
 * EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 *
 * @author Gordon Fraser
 */
package org.evosuite.utils;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class ListenableList<E> extends SimpleListenable<Void> implements List<E>,
        Serializable {

	private static final long serialVersionUID = 1L;

	private static class ObservableListIterator<E> extends SimpleListenable<Void>
	        implements ListIterator<E> {

		private static final long serialVersionUID = 1L;

		private final ListIterator<E> delegate;

		public ObservableListIterator(ListIterator<E> delegate) {
			super();
			this.delegate = delegate;
		}

		@Override
		public void add(E e) {
			delegate.add(e);
			fireEvent(null);
		}

		@Override
		public boolean hasNext() {
			return delegate.hasNext();
		}

		@Override
		public boolean hasPrevious() {
			return delegate.hasPrevious();
		}

		@Override
		public E next() {
			return delegate.next();
		}

		@Override
		public int nextIndex() {
			return delegate.nextIndex();
		}

		@Override
		public E previous() {
			return delegate.previous();
		}

		@Override
		public int previousIndex() {
			return delegate.previousIndex();
		}

		@Override
		public void remove() {
			delegate.remove();
			fireEvent(null);
		}

		@Override
		public void set(E e) {
			delegate.set(e);
			fireEvent(null);
		}
	}

	private final Listener<Void> listener = new Listener<Void>() {

		private static final long serialVersionUID = 1L;

		@Override
		public void receiveEvent(Void event) {
			fireEvent(null);
		}
	};

	private final List<E> delegate;

	/**
	 * <p>Constructor for ListenableList.</p>
	 *
	 * @param delegate a {@link java.util.List} object.
	 */
	public ListenableList(List<E> delegate) {
		super();
		this.delegate = delegate;
	}

	/** {@inheritDoc} */
	@Override
	public boolean add(E e) {
		boolean result = delegate.add(e);
		fireEvent(null);
		return result;
	}

	/** {@inheritDoc} */
	@Override
	public void add(int index, E element) {
		delegate.add(index, element);
		fireEvent(null);
	}

	/** {@inheritDoc} */
	@Override
	public boolean addAll(Collection<? extends E> c) {
		boolean result = delegate.addAll(c);
		fireEvent(null);
		return result;
	}

	/** {@inheritDoc} */
	@Override
	public boolean addAll(int index, Collection<? extends E> c) {
		boolean result = delegate.addAll(index, c);
		fireEvent(null);
		return result;
	}

	/** {@inheritDoc} */
	@Override
	public void clear() {
		delegate.clear();
		fireEvent(null);
	}

	/** {@inheritDoc} */
	@Override
	public boolean contains(Object o) {
		return delegate.contains(o);
	}

	/** {@inheritDoc} */
	@Override
	public boolean containsAll(Collection<?> c) {
		return delegate.containsAll(c);
	}

	/** {@inheritDoc} */
	@Override
	public boolean equals(Object o) {
		return delegate.equals(o);
	}

	/** {@inheritDoc} */
	@Override
	public E get(int index) {
		return delegate.get(index);
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		return delegate.hashCode();
	}

	/** {@inheritDoc} */
	@Override
	public int indexOf(Object o) {
		return delegate.indexOf(o);
	}

	/** {@inheritDoc} */
	@Override
	public boolean isEmpty() {
		return delegate.isEmpty();
	}

	/** {@inheritDoc} */
	@Override
	public Iterator<E> iterator() {
		return listIterator();
	}

	/** {@inheritDoc} */
	@Override
	public int lastIndexOf(Object o) {
		return delegate.lastIndexOf(o);
	}

	/** {@inheritDoc} */
	@Override
	public ListIterator<E> listIterator() {
		ObservableListIterator<E> result = new ObservableListIterator<E>(
		        delegate.listIterator());
		result.addListener(listener);
		return result;
	}

	/** {@inheritDoc} */
	@Override
	public ListIterator<E> listIterator(int index) {
		ObservableListIterator<E> result = new ObservableListIterator<E>(
		        delegate.listIterator(index));
		result.addListener(listener);
		return result;
	}

	/** {@inheritDoc} */
	@Override
	public E remove(int index) {
		E result = delegate.remove(index);
		fireEvent(null);
		return result;
	}

	/** {@inheritDoc} */
	@Override
	public boolean remove(Object o) {
		boolean result = delegate.remove(o);
		fireEvent(null);
		return result;
	}

	/** {@inheritDoc} */
	@Override
	public boolean removeAll(Collection<?> c) {
		boolean result = delegate.removeAll(c);
		fireEvent(null);
		return result;
	}

	/** {@inheritDoc} */
	@Override
	public boolean retainAll(Collection<?> c) {
		boolean result = delegate.retainAll(c);
		fireEvent(null);
		return result;
	}

	/** {@inheritDoc} */
	@Override
	public E set(int index, E element) {
		E result = delegate.set(index, element);
		fireEvent(null);
		return result;
	}

	/** {@inheritDoc} */
	@Override
	public int size() {
		return delegate.size();
	}

	/** {@inheritDoc} */
	@Override
	public List<E> subList(int fromIndex, int toIndex) {
		ListenableList<E> result = new ListenableList<E>(delegate.subList(fromIndex,
		                                                                  toIndex));
		result.addListener(listener);
		return result;
	}

	/** {@inheritDoc} */
	@Override
	public Object[] toArray() {
		return delegate.toArray();
	}

	/** {@inheritDoc} */
	@Override
	public <T> T[] toArray(T[] a) {
		return delegate.toArray(a);
	}
}
