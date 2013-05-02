/**
 * 
 */
package com.examples.with.different.packagename.testcarver;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Gordon Fraser
 * 
 */
public class GenericObjectWrapperSequenceTest {

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

		GenericObjectWrapper<Foo> wrapper = new GenericObjectWrapper<Foo>();
		wrapper.set(foo);
		Assert.assertEquals(42, wrapper.get().getX());
	}

}
