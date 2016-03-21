package com.examples.with.different.packagename.epa;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ConcurrentModificationException;
import java.util.ListIterator;
import java.util.NoSuchElementException;

/**
 * An optimized version of AbstractList.ListItr
 */
// @ClassDefinition(builder="(let [a (doto
// (ar.com.maba.tesis.arrayList.ArrayList.) (.add 1) (.add 2) (.add 3))]
// (.listIterator a))",
// invariant = "(and " +
// "(or (= lastRet -1) (>= lastRet 0)) " +
// "(and (<= 0 cursor) (<= cursor (.size this$0))))")
public class ListItr implements ListIterator<Object> {
	/**
	 *
	 */
	private final MyArrayList arrayList;
	private int cursor; // index of next element to return
	private int lastRet; // index of last element returned; -1 if no such
	private int expectedModCount;
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	public @interface EpaState {
		public String name();
	}
	
	@Retention(RetentionPolicy.RUNTIME)
	@Target({ ElementType.CONSTRUCTOR, ElementType.METHOD })
	public @interface EpaAction {
		public String name();
	}

	@EpaAction(name="ListItr()")
	public ListItr(MyArrayList arrayList, int index) {

		if (index < 0 || index > arrayList.size())
			throw new IndexOutOfBoundsException("Index: " + index);

		this.arrayList = new MyArrayList(arrayList);
		this.lastRet = -1;
		this.expectedModCount = this.arrayList.getModCount();
		this.cursor = index;
	}

	@EpaAction(name="hasPrevious()")
	public boolean hasPrevious() {
		final boolean b = cursor != 0;
		return b;
	}

	@EpaAction(name="nextIndex()")
	public int nextIndex() {
		return cursor;
	}

	// @Pre("(> cursor 0)")
	public int previousIndex() {
		final int i = cursor - 1;
		return i;
	}

	@EpaAction(name="hasNext()")
	@Override
	public boolean hasNext() {
		boolean superHasNext = super_hasNext();
		return superHasNext;
	}

	@EpaAction(name="next()")
	@Override
	// @Pre("(< cursor (eval(.size this$0)))")
	public Object next() {
		final Object superNext = super_next();
		return superNext;
	}

	@Override
	// @Pre("(>= lastRet 0)")
	public void remove() {
		super_remove();
	}

	// @SuppressWarnings("unchecked")
	// @Pre("(> cursor 0)")
	public Object previous() {
		checkForComodification();
		int i = cursor - 1;
		if (i < 0)
			throw new NoSuchElementException();
		Object[] elementData = arrayList.getElementData();
		if (i >= elementData.length)
			throw new ConcurrentModificationException();
		cursor = i;

		Object object = (Object) elementData[lastRet = i];
		return object;
	}

	// @Pre("(and (>= lastRet 0) (> p0 10))")
	private void setNumber(Integer integer) {
		set((Object) integer);
	}

	@EpaAction(name="set()")
	public void set(Object e) {
		if (lastRet < 0)
			throw new IllegalStateException();
		checkForComodification();

		try {
			arrayList.set(lastRet, e);
		} catch (IndexOutOfBoundsException ex) {
			throw new ConcurrentModificationException();
		}
	}

	// @Pre("(> p0 0)")
	private void addNumber(Integer integer) {
		add((Object) integer);
	}

	@EpaAction(name="add()")
	public void add(Object e) {
		checkForComodification();

		try {
			int i = cursor;
			arrayList.add(i, e);
			cursor = i + 1;
			lastRet = -1;
			expectedModCount = this.arrayList.getModCount();
		} catch (IndexOutOfBoundsException ex) {
			throw new ConcurrentModificationException();
		}
	}

	private boolean super_hasNext() {
		final boolean b = cursor != this.arrayList.size();
		return b;
	}

	private Object super_next() {
		checkForComodification();
		int i = cursor;
		if (i >= this.arrayList.size())
			throw new NoSuchElementException();
		Object[] elementData = arrayList.getElementData();
		if (i >= elementData.length)
			throw new ConcurrentModificationException();
		cursor = i + 1;
		final Object o = (Object) elementData[lastRet = i];
		return o;
	}

	private void super_remove() {
		if (lastRet < 0)
			throw new IllegalStateException();
		checkForComodification();

		try {
			arrayList.remove(lastRet);
			cursor = lastRet;
			lastRet = -1;
			expectedModCount = this.arrayList.getModCount();
		} catch (IndexOutOfBoundsException ex) {
			throw new ConcurrentModificationException();
		}
	}

	private final void checkForComodification() {
		if (this.arrayList.getModCount() != expectedModCount)
			throw new ConcurrentModificationException();
	}

	private boolean isAddEnabled() {
		// add is always enabled
		return true;
	}

	private boolean isNextEnabled() {
		return cursor < this.arrayList.size();
	}

	private boolean isPreviousEnabled() {
		return cursor - 1 >= 0;
	}

	private boolean isRemoveEnabled() {
		return lastRet >= 0;
	}

	private boolean isSetEnabled() {
		return lastRet >= 0;
	}

	@EpaState(name="S127")
	private boolean sasaS127() {
		return isAddEnabled() && isNextEnabled() && isPreviousEnabled() && !isRemoveEnabled() && !isSetEnabled();
	}

	@EpaState(name="S95")
	private boolean sasaS95() {
		return isNextEnabled() && isAddEnabled() && !isPreviousEnabled() && !isRemoveEnabled() && !isSetEnabled();
	}

	@EpaState(name="S119")
	private boolean sasaS119() {
		return isAddEnabled() && !isNextEnabled() && isPreviousEnabled() && !isRemoveEnabled() && !isSetEnabled();
	}

	@EpaState(name="S87")
	private boolean sasaS87() {
		return isAddEnabled() && !isNextEnabled() && !isPreviousEnabled() && !isRemoveEnabled() && !isSetEnabled();
	}

	@EpaState(name="S511")
	private boolean sasaS511() {
		return isAddEnabled() && isNextEnabled() && isPreviousEnabled() && isRemoveEnabled() && isSetEnabled();
	}

	@EpaState(name="S503")
	private boolean sasaS503() {
		return isAddEnabled() && !isNextEnabled() && isPreviousEnabled() && isRemoveEnabled() && isSetEnabled();
	}

	@EpaState(name="S479")
	private boolean sasaS479() {
		return isAddEnabled() && isNextEnabled() && !isPreviousEnabled() && isRemoveEnabled() && isSetEnabled();
	}
}
