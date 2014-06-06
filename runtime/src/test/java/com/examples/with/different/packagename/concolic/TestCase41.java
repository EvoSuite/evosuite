package com.examples.with.different.packagename.concolic;

import java.io.ByteArrayOutputStream;


public class TestCase41 {

	/**
	 * @param args
	 */
	public static void test(int int0, int int1, int int3, int int4) {
		MyLinkedList linkedList0 = new MyLinkedList();
		ByteArrayOutputStream byteArrayOutputStream0 = new ByteArrayOutputStream();
		linkedList0.add(byteArrayOutputStream0);
		linkedList0.add(int0);
		Integer integer0 = (Integer) linkedList0.get(int1);
		int int2 = linkedList0.size();
		ByteArrayOutputStream byteArrayOutputStream1 = (ByteArrayOutputStream) linkedList0
				.get(int3);
		int int5 = linkedList0.size();
		ByteArrayOutputStream byteArrayOutputStream2 = (ByteArrayOutputStream) linkedList0
				.get(int4);
		Double double0 = new Double((double) integer0);
		ByteArrayOutputStream byteArrayOutputStream3 = new ByteArrayOutputStream();
		linkedList0.unreacheable();
		linkedList0.add(double0);
		int int6 = linkedList0.size();
		int int7 = linkedList0.size();
		Object object0 = linkedList0.get(int7);
	}

}
