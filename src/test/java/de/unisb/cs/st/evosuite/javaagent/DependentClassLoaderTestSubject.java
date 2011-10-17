package de.unisb.cs.st.evosuite.javaagent;

public class DependentClassLoaderTestSubject {
	private Object something = new Object();
	private int xVal = 0;

	public DependentClassLoaderTestSubject(int xVal) {
		this.xVal = xVal;
	}

	public void doSomethingNow(int value) {
		if (value > 5) {
			something = null;
		}
		if (value > 100) {
			value = value % 100;
		}
		if (xVal < value) {
			xVal = value;
		}
		System.out.println(something.toString());
	}
}
