package com.examples.with.different.packagename.testcarver;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class GenericObjectWrapperWithListTest {
	public static class Foo {
		private int x = 0;

		public int getX() {
			return x;
		}

		public void setX(int x) {
			this.x = x;
		}
	}

	@Test
	public void test() {
		int x = 42;
		Foo foo = new Foo();
		foo.setX(x);

		GenericObjectWrapperWithList<Foo> wrapper = new GenericObjectWrapperWithList<Foo>();
		wrapper.add(foo);
		List<Foo> list = wrapper.getList();
		// list.clear();
		wrapper.setList(list);
		Assert.assertFalse(wrapper.getList().isEmpty());
	}
}
