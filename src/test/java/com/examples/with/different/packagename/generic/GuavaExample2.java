/**
 * 
 */
package com.examples.with.different.packagename.generic;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @author Gordon Fraser
 * 
 */
public class GuavaExample2<B> extends HashMap<Class<? extends B>, B> {

	private static final long serialVersionUID = 2299279734390251599L;

	private GuavaExample2(Class<? extends B> key, B element) {
		put(key, element);
	}

	private GuavaExample2() {

	}

	public static <B> Builder<B> builder() {
		return new Builder<B>();
	}

	public static final class Builder<B> {

		private final Map<Class<? extends B>, B> map = new HashMap<Class<? extends B>, B>();

		public <T extends B> Builder<B> add(Class<T> key, T value) {
			map.put(key, value);
			return this;
		}

		public GuavaExample2<B> build() {
			if (map.isEmpty())
				return new GuavaExample2<B>();
			else {
				Entry<Class<? extends B>, B> entry = map.entrySet().iterator().next();
				return new GuavaExample2<B>(entry.getKey(), entry.getValue());
			}
		}
	}

}
