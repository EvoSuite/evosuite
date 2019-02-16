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




}
