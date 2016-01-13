package org.evosuite.coverage.epa;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ListIterator;

import org.junit.Test;

import com.examples.with.different.packagename.epa.ListItr;
import com.examples.with.different.packagename.epa.MyArrayList;

public class TestListItr {

	@Test
	public void createListItr() {
		int int0 = 0;
		MyArrayList myArrayList0 = new MyArrayList(0);
		ListItr listItr0 = new ListItr(myArrayList0, 0);
		listItr0.add(null);
		int int1 = listItr0.nextIndex();
		int int2 = 0;
		List list0 = myArrayList0.subList(int0, 0);
		Object object0 = listItr0.previous();
		listItr0.set(null);
		myArrayList0.retainAll(list0);
		int int3 = listItr0.previousIndex();

	}

	@Test
	public void createArrayList() {
		ArrayList<Object> arrayList0 = new ArrayList<Object>(0);
		ListIterator<Object> listItr0 = arrayList0.listIterator(0);
		listItr0.add(null);
		listItr0.nextIndex();
		List<Object> list0 = arrayList0.subList(0, 0);
		listItr0.previous();
		listItr0.set(null);
		arrayList0.retainAll(list0);
		listItr0.previousIndex();
	}

	@Test
	public void tooManyResources() {
		Collection<Integer> collection0 = null;
		MyArrayList myArrayList0 = new MyArrayList();
		Object object0 = null;
		Object object1 = new Object();
		boolean boolean0 = myArrayList0.add(object1);
		MyArrayList myArrayList1 = new MyArrayList((Collection) myArrayList0);
		int int0 = 0;
		ListItr listItr0 = new ListItr(myArrayList1, int0);
		boolean boolean1 = myArrayList1.add((Object) myArrayList0);
		boolean boolean2 = myArrayList1.add(object1);
		boolean boolean3 = myArrayList0.add((Object) myArrayList1);
		int int1 = listItr0.nextIndex();
		boolean boolean4 = listItr0.hasNext();
		int int2 = (-1421);
		boolean boolean5 = myArrayList1.retainAll(myArrayList0);
		ListItr listItr1 = new ListItr(myArrayList1, int2);
		Object object2 = listItr1.next();
		boolean boolean6 = listItr0.hasPrevious();
		boolean boolean7 = listItr1.hasPrevious();
		int int3 = listItr1.nextIndex();
		listItr0.remove();

	}

	@Test
	public void tooManyResources2() {
		ArrayList<Object> myArrayList0 = new ArrayList<Object>();
		ArrayList<Object> myArrayList1 = new ArrayList<Object>();
		myArrayList1.add(myArrayList0);
		myArrayList0.add(myArrayList1);
		myArrayList1.retainAll(myArrayList0);

	}

	@Test
	public void tooManyResources3() {
		ArrayList<Object> myArrayList0 = new ArrayList<Object>();
		myArrayList0.add(myArrayList0);
		myArrayList0.hashCode();
	}

}
