package com.examples.with.different.packagename.purity;

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
	
	public int recursivePureInspector() {
		return recursivePureFunction(10);
	}
	
	private static int recursivePureFunction(int x) {
		if (x==0)
			return 0;
		else
			return 1 + recursivePureFunction(x-1);
	}
	
	public int recursiveImpureInspector() {
		return recursiveImpureFunction(10);
	}

	private static int static_dummy_value;
	
	private static int recursiveImpureFunction(int x) {
		int old = static_dummy_value;
		static_dummy_value += 1;
		static_dummy_value = old;
		if (x==0)
			return 0;
		else
			return 1 + recursiveImpureFunction(x-1);
	}

}
