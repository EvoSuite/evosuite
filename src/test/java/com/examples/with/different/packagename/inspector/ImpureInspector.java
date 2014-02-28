package com.examples.with.different.packagename.inspector;

public class ImpureInspector {

	private int value;

	public int getPureValue() {
		return value;
	}

	public int getImpureValue() {
		value++;
		return value;
	}

	public int getImpureValueFromCall() {
		impureStaticMethod();
		return value;
	}

	public int getPureValueFromCall() {
		pureStaticMethod();
		return value;
	}
	private static int static_int_value;

	public static void impureStaticMethod() {
		int old_static_int_value = static_int_value;
		static_int_value += 1;
		static_int_value = old_static_int_value;
	}

	public static void pureStaticMethod() {
		for (int i = 0; i < 2; i++) {
			int j = i;
			if (j == 0) {
				j = i;
			}
		}
	}
}
