package com.examples.with.different.packagename.concolic;

import static org.evosuite.symbolic.Assertions.checkEquals;

public class TestCase82 {

	/**
	 * @param args
	 */
	// char char0 = ConcolicMarker.mark('a', "char0");
	// boolean boolean0 = ConcolicMarker.mark(true, "boolean0");
	// short short0 = ConcolicMarker.mark((short) 1, "short0");
	// byte byte0 = ConcolicMarker.mark((byte) 1, "byte0");
	public static void test(char char0, boolean boolean0, short short0,
			byte byte0) {
		// char boxing
		Character character0 = box_char(char0);
		char char1 = unbox_char(character0);
		checkEquals(char1, 'a');

		// boolean boxing
		Boolean boolean_instance0 = box_boolean(boolean0);
		boolean boolean1 = unbox_boolean(boolean_instance0);
		checkEquals(boolean1, true);

		// short boxing
		Short short_instance0 = box_short(short0);
		short short1 = unbox_short(short_instance0);
		checkEquals(short1, 1);

		// byte boxing
		Byte byte_instance0 = box_byte(byte0);
		byte byte1 = unbox_byte(byte_instance0);
		checkEquals(byte1, 1);

	}

	public static Character box_char(Character i) {
		return i;
	}

	public static char unbox_char(char i) {
		return i;
	}

	public static Boolean box_boolean(Boolean i) {
		return i;
	}

	public static boolean unbox_boolean(boolean i) {
		return i;
	}

	public static Short box_short(Short i) {
		return i;
	}

	public static short unbox_short(short i) {
		return i;
	}

	public static Byte box_byte(Byte i) {
		return i;
	}

	public static byte unbox_byte(byte i) {
		return i;
	}

}
