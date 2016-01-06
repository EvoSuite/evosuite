package com.examples.with.different.packagename.epa;

import java.util.AbstractList;
import java.util.Arrays;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.RandomAccess;

public class ArrayList extends AbstractList<Object>
		implements List<Object>, RandomAccess, Cloneable, java.io.Serializable {

	public static final long serialVersionUID = 8683452581122892189L;

	public transient Object[] elementData;

	public int size;

	public ArrayList(int initialCapacity) {
		super();
		if (initialCapacity < 0)
			throw new IllegalArgumentException("Illegal Capacity: " + initialCapacity);
		this.elementData = new Object[initialCapacity];
	}

	public ArrayList() {
		this(10);
	}

	public ArrayList(Collection c) {
		elementData = c.toArray();
		size = elementData.length;
		// c.toArray might (incorrectly) not return Object[] (see 6260652)
		if (elementData.getClass() != Object[].class)
			elementData = Arrays.copyOf(elementData, size, Object[].class);
	}

	public void trimToSize() {
		modCount++;
		int oldCapacity = elementData.length;
		if (size < oldCapacity) {
			elementData = Arrays.copyOf(elementData, size);
		}
	}

	public void ensureCapacity(int minCapacity) {
		modCount++;
		int oldCapacity = elementData.length;
		if (minCapacity > oldCapacity) {
			Object oldData[] = elementData;
			int newCapacity = (oldCapacity * 3) / 2 + 1;
			if (newCapacity < minCapacity)
				newCapacity = minCapacity;
			// minCapacity is usually close to size, so this is a win:
			elementData = Arrays.copyOf(elementData, newCapacity);
		}
	}

	public int size() {
		return size;
	}

	public boolean isEmpty() {
		return size == 0;
	}

	public boolean contains(Object o) {
		return indexOf(o) >= 0;
	}

	public int indexOf(Object o) {
		if (o == null) {
			for (int i = 0; i < size; i++)
				if (elementData[i] == null)
					return i;
		} else {
			for (int i = 0; i < size; i++)
				if (o.equals(elementData[i]))
					return i;
		}
		return -1;
	}

	public int lastIndexOf(Object o) {
		if (o == null) {
			for (int i = size - 1; i >= 0; i--)
				if (elementData[i] == null)
					return i;
		} else {
			for (int i = size - 1; i >= 0; i--)
				if (o.equals(elementData[i]))
					return i;
		}
		return -1;
	}

	public Object clone() {
		try {
			ArrayList v = (ArrayList) super.clone();
			v.elementData = Arrays.copyOf(elementData, size);
			v.modCount = 0;
			return v;
		} catch (CloneNotSupportedException e) {
			// this shouldn't happen, since we are Cloneable
			throw new InternalError();
		}
	}

	public Object[] toArray() {
		return Arrays.copyOf(elementData, size);
	}

	public Object[] toArray(Object[] a) {
		if (a.length < size)
			// Make a new array of a's runtime type, but my contents:
			return (Object[]) Arrays.copyOf(elementData, size, a.getClass());
		System.arraycopy(elementData, 0, a, 0, size);
		if (a.length > size)
			a[size] = null;
		return a;
	}

	Object elementData(int index) {
		return (Object) elementData[index];
	}

	public Object get(int index) {
		rangeCheck(index);

		return elementData(index);
	}

	public Object set(int index, Object element) {
		rangeCheck(index);

		Object oldValue = elementData(index);
		elementData[index] = element;
		return oldValue;
	}

	public boolean add(Object e) {
		ensureCapacity(size + 1); // Increments modCount!!
		elementData[size++] = e;
		return true;
	}

	public void add(int index, Object element) {
		rangeCheckForAdd(index);

		ensureCapacity(size + 1); // Increments modCount!!
		System.arraycopy(elementData, index, elementData, index + 1, size - index);
		elementData[index] = element;
		size++;
	}

	public Object remove(int index) {
		rangeCheck(index);

		modCount++;
		Object oldValue = elementData(index);

		int numMoved = size - index - 1;
		if (numMoved > 0)
			System.arraycopy(elementData, index + 1, elementData, index, numMoved);
		elementData[--size] = null; // Let gc do its work

		return oldValue;
	}

	public boolean remove(Object o) {
		if (o == null) {
			for (int index = 0; index < size; index++)
				if (elementData[index] == null) {
					fastRemove(index);
					return true;
				}
		} else {
			for (int index = 0; index < size; index++)
				if (o.equals(elementData[index])) {
					fastRemove(index);
					return true;
				}
		}
		return false;
	}

	private void fastRemove(int index) {
		modCount++;
		int numMoved = size - index - 1;
		if (numMoved > 0)
			System.arraycopy(elementData, index + 1, elementData, index, numMoved);
		elementData[--size] = null; // Let gc do its work
	}

	public void clear() {
		modCount++;

		// Let gc do its work
		for (int i = 0; i < size; i++)
			elementData[i] = null;

		size = 0;
	}

	public boolean addAll(Collection c) {
		Object[] a = c.toArray();
		int numNew = a.length;
		ensureCapacity(size + numNew); // Increments modCount
		System.arraycopy(a, 0, elementData, size, numNew);
		size += numNew;
		return numNew != 0;
	}

	public boolean addAll(int index, Collection c) {
		rangeCheckForAdd(index);

		Object[] a = c.toArray();
		int numNew = a.length;
		ensureCapacity(size + numNew); // Increments modCount

		int numMoved = size - index;
		if (numMoved > 0)
			System.arraycopy(elementData, index, elementData, index + numNew, numMoved);

		System.arraycopy(a, 0, elementData, index, numNew);
		size += numNew;
		return numNew != 0;
	}

	protected void removeRange(int fromIndex, int toIndex) {
		modCount++;
		int numMoved = size - toIndex;
		System.arraycopy(elementData, toIndex, elementData, fromIndex, numMoved);

		// Let gc do its work
		int newSize = size - (toIndex - fromIndex);
		while (size != newSize)
			elementData[--size] = null;
	}

	private void rangeCheck(int index) {
		if (index >= size)
			throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
	}

	private void rangeCheckForAdd(int index) {
		if (index > size || index < 0)
			throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
	}

	private String outOfBoundsMsg(int index) {
		return "Index: " + index + ", Size: " + size;
	}

	public boolean removeAll(Collection c) {
		return batchRemove(c, false);
	}

	public boolean retainAll(Collection c) {
		return batchRemove(c, true);
	}

	private boolean batchRemove(Collection c, boolean complement) {
		final Object[] elementData = this.elementData;
		int r = 0, w = 0;
		boolean modified = false;
		try {
			for (; r < size; r++)
				if (c.contains(elementData[r]) == complement)
					elementData[w++] = elementData[r];
		} finally {
			// Preserve behavioral compatibility with AbstractCollection,
			// even if c.contains() throws.
			if (r != size) {
				System.arraycopy(elementData, r, elementData, w, size - r);
				w += size - r;
			}
			if (w != size) {
				for (int i = w; i < size; i++)
					elementData[i] = null;
				modCount += size - w;
				size = w;
				modified = true;
			}
		}
		return modified;
	}

	private void writeObject(java.io.ObjectOutputStream s) throws java.io.IOException {
		// Write out element count, and any hidden stuff
		int expectedModCount = modCount;
		s.defaultWriteObject();

		// Write out array length
		s.writeInt(elementData.length);

		// Write out all elements in the proper order.
		for (int i = 0; i < size; i++)
			s.writeObject(elementData[i]);

		if (modCount != expectedModCount) {
			throw new ConcurrentModificationException();
		}

	}

	private void readObject(java.io.ObjectInputStream s) throws java.io.IOException, ClassNotFoundException {
		// Read in size, and any hidden stuff
		s.defaultReadObject();

		// Read in array length and allocate array
		int arrayLength = s.readInt();
		Object[] a = elementData = new Object[arrayLength];

		// Read in all elements in the proper order.
		for (int i = 0; i < size; i++)
			a[i] = s.readObject();
	}

	public ListIterator<Object> listIterator(int index) {
		if (index < 0 || index > size)
			throw new IndexOutOfBoundsException("Index: " + index);
		return new ListItr(this, index);
	}

	public ListIterator<Object> listIterator() {
		return new ListItr(this, 0);
	}

	public Iterator<Object> iterator() {
		return new Itr();
	}

	private class Itr implements Iterator<Object> {
		int cursor; // index of next element to return
		int lastRet = -1; // index of last element returned; -1 if no such
		int expectedModCount = modCount;

		public boolean hasNext() {
			return cursor != size;
		}

		public Object next() {
			checkForComodification();
			int i = cursor;
			if (i >= size)
				throw new NoSuchElementException();
			Object[] elementData = ArrayList.this.elementData;
			if (i >= elementData.length)
				throw new ConcurrentModificationException();
			cursor = i + 1;
			return (Object) elementData[lastRet = i];
		}

		public void remove() {
			if (lastRet < 0)
				throw new IllegalStateException();
			checkForComodification();

			try {
				ArrayList.this.remove(lastRet);
				cursor = lastRet;
				lastRet = -1;
				expectedModCount = modCount;
			} catch (IndexOutOfBoundsException ex) {
				throw new ConcurrentModificationException();
			}
		}

		final void checkForComodification() {
			if (modCount != expectedModCount)
				throw new ConcurrentModificationException();
		}
	}

	public List subList(int fromIndex, int toIndex) {
		subListRangeCheck(fromIndex, toIndex, size);
		return new SubList(this, 0, fromIndex, toIndex);
	}

	static void subListRangeCheck(int fromIndex, int toIndex, int size) {
		if (fromIndex < 0)
			throw new IndexOutOfBoundsException("fromIndex = " + fromIndex);
		if (toIndex > size)
			throw new IndexOutOfBoundsException("toIndex = " + toIndex);
		if (fromIndex > toIndex)
			throw new IllegalArgumentException("fromIndex(" + fromIndex + ") > toIndex(" + toIndex + ")");
	}

	private class SubList extends AbstractList<Object>implements RandomAccess {
		private final AbstractList<Object> parent;
		private final int parentOffset;
		private final int offset;
		private int size;

		private void myRemoveRange(AbstractList<Object> list, int fromIndex, int toIndex) {
			ListIterator<Object> it = list.listIterator(fromIndex);
			for (int i = 0, n = toIndex - fromIndex; i < n; i++) {
				it.next();
				it.remove();
			}
		}

		SubList(AbstractList<Object> parent, int offset, int fromIndex, int toIndex) {
			this.parent = parent;
			this.parentOffset = fromIndex;
			this.offset = offset + fromIndex;
			this.size = toIndex - fromIndex;
			this.modCount = ArrayList.this.modCount;
		}

		public Object set(int index, Object e) {
			rangeCheck(index);
			checkForComodification();
			Object oldValue = ArrayList.this.elementData(offset + index);
			Object[] ed = ArrayList.this.elementData;
			ed[offset + index] = e;
			return oldValue;
		}

		public Object get(int index) {
			rangeCheck(index);
			checkForComodification();
			return ArrayList.this.elementData(offset + index);
		}

		public int size() {
			checkForComodification();
			return this.size;
		}

		public void add(int index, Object e) {
			rangeCheckForAdd(index);
			checkForComodification();
			parent.add(parentOffset + index, e);
			// this.modCount = parent.modCount;
			this.modCount = 0;
			this.size++;
		}

		public Object remove(int index) {
			rangeCheck(index);
			checkForComodification();
			Object result = parent.remove(parentOffset + index);
			// this.modCount = parent.modCount;
			this.modCount = 0;
			this.size--;
			return result;
		}

		protected void removeRange(int fromIndex, int toIndex) {
			checkForComodification();
			myRemoveRange(parent, parentOffset + fromIndex, parentOffset + toIndex);
			// this.modCount = parent.modCount;
			this.modCount = 0;
			this.size -= toIndex - fromIndex;
		}

		public boolean addAll(Collection c) {
			return addAll(this.size, c);
		}

		public boolean addAll(int index, Collection c) {
			rangeCheckForAdd(index);
			int cSize = c.size();
			if (cSize == 0)
				return false;

			checkForComodification();
			parent.addAll(parentOffset + index, c);
			// this.modCount = parent.modCount;
			this.modCount = 0;
			this.size += cSize;
			return true;
		}

		public Iterator iterator() {
			return listIterator();
		}

		public ListIterator listIterator(final int index) {
			checkForComodification();
			rangeCheckForAdd(index);

			return new ListIterator() {
				int cursor = index;
				int lastRet = -1;
				int expectedModCount = ArrayList.this.modCount;

				public boolean hasNext() {
					return cursor != SubList.this.size;
				}

				public Object next() {
					checkForComodification();
					int i = cursor;
					if (i >= SubList.this.size)
						throw new NoSuchElementException();
					Object[] elementData = ArrayList.this.elementData;
					if (offset + i >= elementData.length)
						throw new ConcurrentModificationException();
					cursor = i + 1;
					return (Object) elementData[offset + (lastRet = i)];
				}

				public boolean hasPrevious() {
					return cursor != 0;
				}

				public Object previous() {
					checkForComodification();
					int i = cursor - 1;
					if (i < 0)
						throw new NoSuchElementException();
					Object[] elementData = ArrayList.this.elementData;
					if (offset + i >= elementData.length)
						throw new ConcurrentModificationException();
					cursor = i;
					return (Object) elementData[offset + (lastRet = i)];
				}

				public int nextIndex() {
					return cursor;
				}

				public int previousIndex() {
					return cursor - 1;
				}

				public void remove() {
					if (lastRet < 0)
						throw new IllegalStateException();
					checkForComodification();

					try {
						SubList.this.remove(lastRet);
						cursor = lastRet;
						lastRet = -1;
						expectedModCount = ArrayList.this.modCount;
					} catch (IndexOutOfBoundsException ex) {
						throw new ConcurrentModificationException();
					}
				}

				public void set(Object e) {
					if (lastRet < 0)
						throw new IllegalStateException();
					checkForComodification();

					try {
						ArrayList.this.set(offset + lastRet, e);
					} catch (IndexOutOfBoundsException ex) {
						throw new ConcurrentModificationException();
					}
				}

				public void add(Object e) {
					checkForComodification();

					try {
						int i = cursor;
						SubList.this.add(i, e);
						cursor = i + 1;
						lastRet = -1;
						expectedModCount = ArrayList.this.modCount;
					} catch (IndexOutOfBoundsException ex) {
						throw new ConcurrentModificationException();
					}
				}

				final void checkForComodification() {
					if (expectedModCount != ArrayList.this.modCount)
						throw new ConcurrentModificationException();
				}
			};
		}

		public List subList(int fromIndex, int toIndex) {
			subListRangeCheck(fromIndex, toIndex, size);
			return new SubList(this, offset, fromIndex, toIndex);
		}

		private void rangeCheck(int index) {
			if (index < 0 || index >= this.size)
				throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
		}

		private void rangeCheckForAdd(int index) {
			if (index < 0 || index > this.size)
				throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
		}

		private String outOfBoundsMsg(int index) {
			return "Index: " + index + ", Size: " + this.size;
		}

		private void checkForComodification() {
			if (ArrayList.this.modCount != this.modCount)
				throw new ConcurrentModificationException();
		}
	}

	public int getModCount() {
		return modCount;
	}
}
