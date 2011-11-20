package de.unisb.cs.st.evosuite.utils;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class ListenableList<E> extends SimpleListenable<Void> implements List<E> {
	private static class ObservableListIterator<E> extends SimpleListenable<Void> implements ListIterator<E> {
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

		@Override
		public void receiveEvent(Void event) {
			fireEvent(null);
		}
	};

	private final List<E> delegate;

	public ListenableList(List<E> delegate) {
		super();
		this.delegate = delegate;
	}

	@Override
	public boolean add(E e) {
		boolean result = delegate.add(e);
		fireEvent(null);
		return result;
	}

	@Override
	public void add(int index, E element) {
		delegate.add(index, element);
		fireEvent(null);
	}

	@Override
	public boolean addAll(Collection<? extends E> c) {
		boolean result = delegate.addAll(c);
		fireEvent(null);
		return result;
	}

	@Override
	public boolean addAll(int index, Collection<? extends E> c) {
		boolean result = delegate.addAll(index, c);
		fireEvent(null);
		return result;
	}

	@Override
	public void clear() {
		delegate.clear();
		fireEvent(null);
	}

	@Override
	public boolean contains(Object o) {
		return delegate.contains(o);
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return delegate.containsAll(c);
	}

	@Override
	public boolean equals(Object o) {
		return delegate.equals(o);
	}

	@Override
	public E get(int index) {
		return delegate.get(index);
	}

	@Override
	public int hashCode() {
		return delegate.hashCode();
	}

	@Override
	public int indexOf(Object o) {
		return delegate.indexOf(o);
	}

	@Override
	public boolean isEmpty() {
		return delegate.isEmpty();
	}

	@Override
	public Iterator<E> iterator() {
		return listIterator();
	}

	@Override
	public int lastIndexOf(Object o) {
		return delegate.lastIndexOf(o);
	}

	@Override
	public ListIterator<E> listIterator() {
		ObservableListIterator<E> result = new ObservableListIterator<E>(delegate.listIterator());
		result.addListener(listener);
		return result;
	}

	@Override
	public ListIterator<E> listIterator(int index) {
		ObservableListIterator<E> result = new ObservableListIterator<E>(delegate.listIterator(index));
		result.addListener(listener);
		return result;
	}

	@Override
	public E remove(int index) {
		E result = delegate.remove(index);
		fireEvent(null);
		return result;
	}

	@Override
	public boolean remove(Object o) {
		boolean result = delegate.remove(o);
		fireEvent(null);
		return result;
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		boolean result = delegate.removeAll(c);
		fireEvent(null);
		return result;
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		boolean result = delegate.retainAll(c);
		fireEvent(null);
		return result;
	}

	@Override
	public E set(int index, E element) {
		E result = delegate.set(index, element);
		fireEvent(null);
		return result;
	}

	@Override
	public int size() {
		return delegate.size();
	}

	@Override
	public List<E> subList(int fromIndex, int toIndex) {
		ListenableList<E> result = new ListenableList<E>(delegate.subList(fromIndex, toIndex));
		result.addListener(listener);
		return result;
	}

	@Override
	public Object[] toArray() {
		return delegate.toArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		return delegate.toArray(a);
	}
}
