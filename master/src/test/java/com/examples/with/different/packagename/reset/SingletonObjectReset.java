package com.examples.with.different.packagename.reset;

public class SingletonObjectReset {

	public static class Counter {
		private int count;

		private Counter() {
			count = 0;
		}

		public void inc() {
			count++;
		}

		public int getCount() {
			return count;
		}
	}

	private static final Counter counter = new Counter();

	public int getCount() {
		return counter.getCount();
	}

	public void inc() {
		counter.inc();
	}

}
